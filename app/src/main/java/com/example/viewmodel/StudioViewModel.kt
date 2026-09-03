package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ads.AdManager
import com.example.ads.PremiumManager
import com.example.ads.TapsellAdManager
import com.example.ai.AIImageEngine
import com.example.ai.AIImageEngineImpl
import com.example.ai.AIResult
import com.example.core.FileUtils
import com.example.core.LanguageManager
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import com.example.editor.AdjustmentsState
import com.example.editor.BeautyState
import com.example.editor.BodyState
import com.example.editor.CropAspect
import com.example.editor.EditorStateSnapshot
import com.example.editor.FilterType
import com.example.editor.ImageProcessor
import com.example.editor.StickerOverlay
import com.example.editor.TextOverlay
import com.example.editor.TransformState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayDeque

enum class StudioScreen {
    HOME,
    AI_STUDIO,
    EDIT,
    BEAUTY,
    BODY,
    FILTERS,
    TOOLS,
    EXPORT,
    SETTINGS,
    ABOUT,
    OBJECT_REMOVER,
    COLLAGE,
    LANGUAGE_SELECT
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    val languageManager = LanguageManager(application)
    val aiEngine: AIImageEngine = AIImageEngineImpl()
    val adManager: AdManager = TapsellAdManager(application)
    val premiumManager = PremiumManager()

    private val repository: ProjectRepository =
        ProjectRepository(AppDatabase.getInstance(application).projectDao())

    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active screen navigation
    private val _currentScreen = MutableStateFlow(
        if (languageManager.isFirstLaunch) StudioScreen.LANGUAGE_SELECT else StudioScreen.HOME
    )
    val currentScreen: StateFlow<StudioScreen> = _currentScreen.asStateFlow()

    // Active project metadata
    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    private val _projectTitle = MutableStateFlow("Photo_Studio_Edit")
    val projectTitle: StateFlow<String> = _projectTitle.asStateFlow()

    // Bitmaps
    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap: StateFlow<Bitmap?> = _originalBitmap.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    // Hold to compare state (shows original when true)
    private val _isComparing = MutableStateFlow(false)
    val isComparing: StateFlow<Boolean> = _isComparing.asStateFlow()

    // Editor snapshot state
    private val _editorState = MutableStateFlow(EditorStateSnapshot())
    val editorState: StateFlow<EditorStateSnapshot> = _editorState.asStateFlow()

    // History stack for Undo / Redo (bounded to 20 to avoid excessive memory)
    private val maxHistorySize = 20
    private val undoStack = ArrayDeque<EditorStateSnapshot>()
    private val redoStack = ArrayDeque<EditorStateSnapshot>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // UI feedback / Loading states
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _dialogMessage = MutableStateFlow<Pair<String, String>?>(null)
    val dialogMessage: StateFlow<Pair<String, String>?> = _dialogMessage.asStateFlow()

    // AI prompt and suggestions
    val aiPrompt = MutableStateFlow("")

    private var renderJob: Job? = null

    init {
        // Load default studio sample on init if needed or wait for user photo
    }

    fun navigateTo(screen: StudioScreen) {
        _currentScreen.value = screen
    }

    fun dismissDialog() {
        _dialogMessage.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun setComparing(comparing: Boolean) {
        _isComparing.value = comparing
    }

    fun loadSamplePhoto() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val sampleBitmap = BitmapFactory.decodeResource(
                    getApplication<Application>().resources,
                    R.drawable.hero_studio_sample
                )
                if (sampleBitmap != null) {
                    setNewBitmap(sampleBitmap, "Studio_Sample")
                    _currentScreen.value = StudioScreen.EDIT
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to load sample: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun loadPhotoFromUri(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val bitmap = ImageProcessor.loadOptimizedBitmap(getApplication(), uri)
                if (bitmap != null) {
                    setNewBitmap(bitmap, "Photo_${System.currentTimeMillis() % 10000}")
                    _currentScreen.value = StudioScreen.EDIT
                } else {
                    _statusMessage.value = "Could not load image format"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error loading image: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun setNewBitmap(bitmap: Bitmap, title: String) {
        _originalBitmap.value = bitmap
        _previewBitmap.value = bitmap
        _projectTitle.value = title
        _currentProjectId.value = null
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _editorState.value = EditorStateSnapshot()
    }

    // --- State mutation with history push ---
    private fun pushState(newState: EditorStateSnapshot) {
        if (undoStack.size >= maxHistorySize) {
            undoStack.removeLast()
        }
        undoStack.push(_editorState.value)
        redoStack.clear()
        _canUndo.value = true
        _canRedo.value = false
        _editorState.value = newState
        scheduleRender()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.pop()
            redoStack.push(_editorState.value)
            _editorState.value = previous
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
            scheduleRender()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.pop()
            undoStack.push(_editorState.value)
            _editorState.value = next
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            scheduleRender()
        }
    }

    fun resetAllAdjustments() {
        pushState(_editorState.value.copy(adjustments = AdjustmentsState()))
    }

    fun resetAdjustment(type: String) {
        val current = _editorState.value.adjustments
        val updated = when (type.lowercase()) {
            "brightness" -> current.copy(brightness = 0f)
            "contrast" -> current.copy(contrast = 0f)
            "saturation" -> current.copy(saturation = 0f)
            "exposure" -> current.copy(exposure = 0f)
            "highlights" -> current.copy(highlights = 0f)
            "shadows" -> current.copy(shadows = 0f)
            "temperature" -> current.copy(temperature = 0f)
            "tint" -> current.copy(tint = 0f)
            "sharpness" -> current.copy(sharpness = 0f)
            "blur" -> current.copy(blur = 0f)
            "vignette" -> current.copy(vignette = 0f)
            "fade" -> current.copy(fade = 0f)
            "grain" -> current.copy(grain = 0f)
            else -> current
        }
        pushState(_editorState.value.copy(adjustments = updated))
    }

    fun resetBody() {
        pushState(_editorState.value.copy(body = BodyState()))
    }

    fun resetBeauty() {
        pushState(_editorState.value.copy(beauty = BeautyState()))
    }

    fun resetAll() {
        pushState(EditorStateSnapshot())
    }

    // Adjustments
    fun updateAdjustments(block: AdjustmentsState.() -> AdjustmentsState) {
        val newAdj = _editorState.value.adjustments.block()
        _editorState.value = _editorState.value.copy(adjustments = newAdj)
        scheduleRender()
    }

    // Filters
    fun setFilter(type: FilterType, intensity: Float = 0.85f) {
        pushState(_editorState.value.copy(filterType = type, filterIntensity = intensity))
    }

    fun setFilterIntensity(intensity: Float) {
        _editorState.value = _editorState.value.copy(filterIntensity = intensity)
        scheduleRender()
    }

    // Transforms
    fun rotate90() {
        val current = _editorState.value.transform.rotation
        val newRotation = (current + 90) % 360
        pushState(_editorState.value.copy(transform = _editorState.value.transform.copy(rotation = newRotation)))
    }

    fun rotateMinus90() {
        val current = _editorState.value.transform.rotation
        val newRotation = (current - 90 + 360) % 360
        pushState(_editorState.value.copy(transform = _editorState.value.transform.copy(rotation = newRotation)))
    }

    fun flipHorizontal() {
        val current = _editorState.value.transform.flipH
        pushState(_editorState.value.copy(transform = _editorState.value.transform.copy(flipH = !current)))
    }

    fun flipVertical() {
        val current = _editorState.value.transform.flipV
        pushState(_editorState.value.copy(transform = _editorState.value.transform.copy(flipV = !current)))
    }

    fun setStraightenAngle(angle: Float) {
        _editorState.value = _editorState.value.copy(
            transform = _editorState.value.transform.copy(straightenAngle = angle)
        )
        scheduleRender()
    }

    fun setCropAspect(aspect: CropAspect) {
        pushState(_editorState.value.copy(transform = _editorState.value.transform.copy(cropAspect = aspect)))
    }

    fun applyCrop(leftRatio: Float, topRatio: Float, rightRatio: Float, bottomRatio: Float) {
        val current = _previewBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val cropped = ImageProcessor.cropBitmap(current, leftRatio, topRatio, rightRatio, bottomRatio)
                setNewBitmap(cropped, _projectTitle.value + "_Crop")
                _statusMessage.value = "Crop applied"
            } catch (e: Exception) {
                _statusMessage.value = "Crop failed: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // Beauty
    fun updateBeauty(block: BeautyState.() -> BeautyState) {
        val newBeauty = _editorState.value.beauty.block()
        _editorState.value = _editorState.value.copy(beauty = newBeauty)
        scheduleRender()
    }

    // Body
    fun updateBody(block: BodyState.() -> BodyState) {
        val newBody = _editorState.value.body.block()
        _editorState.value = _editorState.value.copy(body = newBody)
        scheduleRender()
    }

    // Text & Stickers
    fun addTextOverlay(text: String, color: Int = android.graphics.Color.WHITE) {
        if (text.isBlank()) return
        val current = _editorState.value.textOverlays
        val newOverlay = TextOverlay(text = text, color = color, yRatio = 0.5f + (current.size * 0.1f))
        pushState(_editorState.value.copy(textOverlays = current + newOverlay))
    }

    fun updateTextOverlay(id: Long, block: TextOverlay.() -> TextOverlay) {
        val current = _editorState.value.textOverlays
        val updated = current.map { if (it.id == id) it.block() else it }
        _editorState.value = _editorState.value.copy(textOverlays = updated)
        scheduleRender()
    }

    fun removeTextOverlay(id: Long) {
        val current = _editorState.value.textOverlays
        pushState(_editorState.value.copy(textOverlays = current.filterNot { it.id == id }))
    }

    fun addSticker(emoji: String) {
        val current = _editorState.value.stickerOverlays
        val newSticker = StickerOverlay(emojiOrIcon = emoji, xRatio = 0.5f, yRatio = 0.5f)
        pushState(_editorState.value.copy(stickerOverlays = current + newSticker))
    }

    // Render Preview
    private fun scheduleRender() {
        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            delay(30) // Smooth debounce for slider gestures
            val original = _originalBitmap.value ?: return@launch
            val rendered = ImageProcessor.renderPreview(original, _editorState.value)
            _previewBitmap.value = rendered
        }
    }

    // --- AI Operations ---
    fun runAutoEnhance() {
        val original = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            when (val result = aiEngine.autoEnhance(original)) {
                is AIResult.Success -> {
                    _originalBitmap.value = result.data
                    _statusMessage.value = "AI Auto Enhance Applied (${result.executionTimeMs}ms)"
                    scheduleRender()
                }
                is AIResult.NotConfigured -> {
                    _dialogMessage.value = Pair("AI Auto Enhance", result.message)
                }
                is AIResult.Error -> {
                    _statusMessage.value = result.message
                }
            }
            _isProcessing.value = false
        }
    }

    fun runAIGeneratePrompt(prompt: String) {
        val original = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            when (val result = aiEngine.editWithPrompt(original, prompt)) {
                is AIResult.Success -> {
                    _originalBitmap.value = result.data
                    scheduleRender()
                }
                is AIResult.NotConfigured -> {
                    _dialogMessage.value = Pair("AI Engine Configuration", result.message)
                }
                is AIResult.Error -> {
                    _statusMessage.value = result.message
                }
            }
            _isProcessing.value = false
        }
    }

    fun runRemoveObject(mask: Bitmap? = null) {
        val original = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            val effectiveMask = mask ?: Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ALPHA_8)
            when (val result = aiEngine.removeObject(original, effectiveMask)) {
                is AIResult.Success -> {
                    _originalBitmap.value = result.data
                    scheduleRender()
                }
                is AIResult.NotConfigured -> {
                    _dialogMessage.value = Pair("Object Remover AI Backend", result.message)
                }
                is AIResult.Error -> {
                    _statusMessage.value = result.message
                }
            }
            _isProcessing.value = false
        }
    }

    fun runOutfitChange(styleName: String) {
        val original = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            when (val result = aiEngine.modifyClothing(original, styleName)) {
                is AIResult.Success -> {
                    _originalBitmap.value = result.data
                    scheduleRender()
                }
                is AIResult.NotConfigured -> {
                    _dialogMessage.value = Pair("AI Outfit Styling", result.message)
                }
                is AIResult.Error -> {
                    _statusMessage.value = result.message
                }
            }
            _isProcessing.value = false
        }
    }

    fun runAIFilterGenerate(stylePrompt: String) {
        val original = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            when (val result = aiEngine.generateStyleFilter(original, stylePrompt)) {
                is AIResult.Success -> {
                    _originalBitmap.value = result.data
                    scheduleRender()
                }
                is AIResult.NotConfigured -> {
                    _dialogMessage.value = Pair("AI Filter Synthesis", result.message)
                }
                is AIResult.Error -> {
                    _statusMessage.value = result.message
                }
            }
            _isProcessing.value = false
        }
    }

    // --- Projects Management ---
    fun saveCurrentProject() {
        val bitmap = _previewBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val filename = "proj_${System.currentTimeMillis()}.jpg"
                val path = FileUtils.saveBitmapToInternalStorage(getApplication(), bitmap, filename)
                val entity = ProjectEntity(
                    id = _currentProjectId.value ?: 0,
                    title = _projectTitle.value,
                    imagePath = path,
                    width = bitmap.width,
                    height = bitmap.height,
                    filterName = _editorState.value.filterType.name,
                    brightness = _editorState.value.adjustments.brightness,
                    contrast = _editorState.value.adjustments.contrast,
                    saturation = _editorState.value.adjustments.saturation
                )
                val newId = repository.saveProject(entity)
                _currentProjectId.value = newId
                _statusMessage.value = "Project saved to local storage"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to save project: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun openProject(project: ProjectEntity) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val file = File(project.imagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        _currentProjectId.value = project.id
                        setNewBitmap(bitmap, project.title)
                        _currentScreen.value = StudioScreen.EDIT
                    } else {
                        _statusMessage.value = "Could not decode project photo"
                    }
                } else {
                    _statusMessage.value = "Project file no longer exists"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error opening project: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun renameProject(id: Long, newTitle: String) {
        viewModelScope.launch {
            repository.renameProject(id, newTitle)
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun clearAllProjects() {
        viewModelScope.launch {
            repository.clearAllProjects()
            _statusMessage.value = "All recent projects cleared"
        }
    }

    // Direct Natural-Language AI image editing fulfilling user specification
    fun editImageWithAI(prompt: String) {
        val original = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = aiEngine.editImage(original, prompt)
            result.onSuccess { edited ->
                _originalBitmap.value = edited
                scheduleRender()
                _statusMessage.value = "AI transformation completed"
            }.onFailure { err ->
                _dialogMessage.value = Pair("AI Studio Engine", err.message ?: "AI backend not configured")
            }
            _isProcessing.value = false
        }
    }

    // --- Export & Share ---
    fun exportToGallery(
        format: String = "JPG",
        quality: Int = 92,
        resolution: String = "Original"
    ) {
        val bitmap = _previewBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val targetDim = when (resolution.uppercase()) {
                    "4K" -> 3840
                    "2K" -> 2560
                    "1080P" -> 1920
                    else -> 0 // Original
                }
                val exportBitmap = if (targetDim > 0) {
                    ImageProcessor.scaleToResolution(bitmap, targetDim)
                } else {
                    bitmap
                }

                val result = FileUtils.saveBitmapToGallery(getApplication(), exportBitmap, format, quality)
                result.onSuccess {
                    _statusMessage.value = "Saved successfully to Gallery ($resolution, $format)!"
                    // Auto-save project entry to local Room database (Requirement 15)
                    saveCurrentProject()
                }.onFailure { err ->
                    _statusMessage.value = "Export failed: ${err.message}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Export error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun shareCurrentPhoto() {
        val bitmap = _previewBitmap.value ?: return
        viewModelScope.launch {
            FileUtils.shareBitmap(getApplication(), bitmap)
        }
    }

    // --- Ads ---
    fun watchRewardedAd(activity: Activity?, onUnlocked: () -> Unit) {
        viewModelScope.launch {
            adManager.showRewardedAd(
                activity = activity,
                onRewardEarned = {
                    onUnlocked()
                    _statusMessage.value = "Reward unlocked successfully!"
                },
                onAdClosed = {}
            )
        }
    }
}
