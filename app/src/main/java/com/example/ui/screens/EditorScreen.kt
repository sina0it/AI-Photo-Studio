package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.editor.CropAspect
import com.example.editor.FilterType
import com.example.ui.components.PhotoCanvas
import com.example.ui.components.ToolSlider
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel
import kotlin.math.roundToInt

@Composable
fun EditorScreen(
    viewModel: StudioViewModel
) {
    val originalBitmap by viewModel.originalBitmap.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val isComparing by viewModel.isComparing.collectAsState()
    val editorState by viewModel.editorState.collectAsState()
    val adj = editorState.adjustments
    val beauty = editorState.beauty
    val transform = editorState.transform

    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Pair(stringResource(R.string.nav_adjust), Icons.Default.Tune),
        Pair(stringResource(R.string.nav_crop), Icons.Default.Crop),
        Pair(stringResource(R.string.nav_transform), Icons.Default.Straighten),
        Pair(stringResource(R.string.nav_filters), Icons.Default.Filter),
        Pair(stringResource(R.string.nav_beauty), Icons.Default.Face),
        Pair(stringResource(R.string.nav_ai_studio), Icons.Default.AutoAwesome),
        Pair(stringResource(R.string.nav_text), Icons.Default.TextFields),
        Pair(stringResource(R.string.export_title), Icons.Default.Download)
    )

    // Crop state margins
    var cropLeft by remember { mutableFloatStateOf(0.05f) }
    var cropTop by remember { mutableFloatStateOf(0.05f) }
    var cropRight by remember { mutableFloatStateOf(0.95f) }
    var cropBottom by remember { mutableFloatStateOf(0.95f) }
    var showGridOverlay by remember { mutableStateOf(true) }

    // Text state
    var newTextContent by remember { mutableStateOf("") }
    var selectedTextId by remember { mutableStateOf<Long?>(null) }

    // AI prompt
    var localAiPrompt by remember { mutableStateOf("") }

    // Export options inside editor
    var exportFormat by remember { mutableStateOf("JPG") }
    var exportQuality by remember { mutableIntStateOf(92) }
    var exportResolution by remember { mutableStateOf("Original") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.loadPhotoFromUri(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("editor_screen")
    ) {
        // Main photo canvas area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            PhotoCanvas(
                originalBitmap = originalBitmap,
                previewBitmap = previewBitmap,
                isComparing = isComparing,
                onImportRequested = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            // Rule-of-thirds grid overlay when in Crop mode
            if (activeTab == 1 && showGridOverlay && previewBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                        .drawBehind {
                            val w = size.width
                            val h = size.height
                            val gridColor = Color.White.copy(alpha = 0.45f)
                            // Vertical 1/3 and 2/3 lines
                            drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), 1.dp.toPx())
                            drawLine(gridColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), 1.dp.toPx())
                            // Horizontal 1/3 and 2/3 lines
                            drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), 1.dp.toPx())
                            drawLine(gridColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), 1.dp.toPx())
                        }
                )
            }
        }

        // Active control panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Secondary action bar (Reset / Quick actions)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tabs[activeTab].first.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (activeTab) {
                            0 -> {
                                OutlinedButton(
                                    onClick = { viewModel.resetAllAdjustments() },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("reset_all_adjustments_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.action_reset), fontSize = 11.sp)
                                }
                            }
                            1 -> {
                                Button(
                                    onClick = {
                                        viewModel.applyCrop(cropLeft, cropTop, cropRight, cropBottom)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("apply_crop_btn")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.btn_apply_crop), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            4 -> {
                                OutlinedButton(
                                    onClick = { viewModel.resetBeauty() },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("reset_beauty_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.action_reset), fontSize = 11.sp)
                                }
                            }
                            else -> {}
                        }
                    }
                }

                // Interactive Content Area per tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    when (activeTab) {
                        0 -> AdjustmentsTabContent(viewModel, adj)
                        1 -> CropTabContent(
                            editorState = editorState,
                            onAspectSelected = { aspect ->
                                viewModel.setCropAspect(aspect)
                                // Adjust ratios to match aspect
                                when (aspect) {
                                    CropAspect.ORIGINAL -> { cropLeft = 0.05f; cropTop = 0.05f; cropRight = 0.95f; cropBottom = 0.95f }
                                    CropAspect.SQUARE -> { cropLeft = 0.1f; cropTop = 0.1f; cropRight = 0.9f; cropBottom = 0.9f }
                                    CropAspect.PORTRAIT_4_5 -> { cropLeft = 0.15f; cropTop = 0.05f; cropRight = 0.85f; cropBottom = 0.95f }
                                    CropAspect.LANDSCAPE_16_9 -> { cropLeft = 0.05f; cropTop = 0.22f; cropRight = 0.95f; cropBottom = 0.78f }
                                    CropAspect.STORY_9_16 -> { cropLeft = 0.22f; cropTop = 0.05f; cropRight = 0.78f; cropBottom = 0.95f }
                                    CropAspect.PHOTO_3_4 -> { cropLeft = 0.12f; cropTop = 0.05f; cropRight = 0.88f; cropBottom = 0.95f }
                                    CropAspect.LANDSCAPE_4_3 -> { cropLeft = 0.05f; cropTop = 0.15f; cropRight = 0.95f; cropBottom = 0.85f }
                                    CropAspect.FREE -> { cropLeft = 0.05f; cropTop = 0.05f; cropRight = 0.95f; cropBottom = 0.95f }
                                }
                            },
                            showGrid = showGridOverlay,
                            onToggleGrid = { showGridOverlay = !showGridOverlay },
                            onRotateClockwise = { viewModel.rotate90() },
                            onRotateCounterClockwise = { viewModel.rotateMinus90() },
                            onResetCrop = {
                                cropLeft = 0.05f; cropTop = 0.05f; cropRight = 0.95f; cropBottom = 0.95f
                                viewModel.setCropAspect(CropAspect.FREE)
                            }
                        )
                        2 -> TransformTabContent(viewModel, transform)
                        3 -> FiltersTabContent(viewModel, editorState)
                        4 -> BeautyTabContent(viewModel, beauty)
                        5 -> AIStudioTabContent(
                            viewModel = viewModel,
                            prompt = localAiPrompt,
                            onPromptChange = { localAiPrompt = it }
                        )
                        6 -> TextTabContent(
                            viewModel = viewModel,
                            editorState = editorState,
                            newText = newTextContent,
                            onNewTextChange = { newTextContent = it },
                            selectedId = selectedTextId,
                            onSelectId = { selectedTextId = it }
                        )
                        7 -> ExportTabContent(
                            viewModel = viewModel,
                            format = exportFormat,
                            onFormatChange = { exportFormat = it },
                            quality = exportQuality,
                            onQualityChange = { exportQuality = it },
                            resolution = exportResolution,
                            onResolutionChange = { exportResolution = it }
                        )
                    }
                }

                // Primary Bottom Navigation Bar (8 professional tabs)
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    edgePadding = 8.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_bottom_tab_row")
                ) {
                    tabs.forEachIndexed { index, pair ->
                        val isSelected = activeTab == index
                        Tab(
                            selected = isSelected,
                            onClick = { activeTab = index },
                            icon = {
                                Icon(
                                    imageVector = pair.second,
                                    contentDescription = pair.first,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = pair.first,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("editor_tab_${pair.first.lowercase()}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentsTabContent(
    viewModel: StudioViewModel,
    adj: com.example.editor.AdjustmentsState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ToolSlider(
            label = stringResource(R.string.tool_brightness),
            value = adj.brightness,
            onValueChange = { viewModel.updateAdjustments { copy(brightness = it) } },
            onReset = { viewModel.resetAdjustment("brightness") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_contrast),
            value = adj.contrast,
            onValueChange = { viewModel.updateAdjustments { copy(contrast = it) } },
            onReset = { viewModel.resetAdjustment("contrast") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_saturation),
            value = adj.saturation,
            onValueChange = { viewModel.updateAdjustments { copy(saturation = it) } },
            onReset = { viewModel.resetAdjustment("saturation") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_exposure),
            value = adj.exposure,
            onValueChange = { viewModel.updateAdjustments { copy(exposure = it) } },
            onReset = { viewModel.resetAdjustment("exposure") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_highlights),
            value = adj.highlights,
            onValueChange = { viewModel.updateAdjustments { copy(highlights = it) } },
            onReset = { viewModel.resetAdjustment("highlights") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_shadows),
            value = adj.shadows,
            onValueChange = { viewModel.updateAdjustments { copy(shadows = it) } },
            onReset = { viewModel.resetAdjustment("shadows") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_temperature),
            value = adj.temperature,
            onValueChange = { viewModel.updateAdjustments { copy(temperature = it) } },
            onReset = { viewModel.resetAdjustment("temperature") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_tint),
            value = adj.tint,
            onValueChange = { viewModel.updateAdjustments { copy(tint = it) } },
            onReset = { viewModel.resetAdjustment("tint") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_sharpness),
            value = adj.sharpness,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateAdjustments { copy(sharpness = it) } },
            onReset = { viewModel.resetAdjustment("sharpness") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_blur),
            value = adj.blur,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateAdjustments { copy(blur = it) } },
            onReset = { viewModel.resetAdjustment("blur") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_vignette),
            value = adj.vignette,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateAdjustments { copy(vignette = it) } },
            onReset = { viewModel.resetAdjustment("vignette") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_fade),
            value = adj.fade,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateAdjustments { copy(fade = it) } },
            onReset = { viewModel.resetAdjustment("fade") }
        )
        ToolSlider(
            label = stringResource(R.string.tool_grain),
            value = adj.grain,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateAdjustments { copy(grain = it) } },
            onReset = { viewModel.resetAdjustment("grain") }
        )
    }
}

@Composable
private fun CropTabContent(
    editorState: com.example.editor.EditorStateSnapshot,
    onAspectSelected: (CropAspect) -> Unit,
    showGrid: Boolean,
    onToggleGrid: () -> Unit,
    onRotateClockwise: () -> Unit,
    onRotateCounterClockwise: () -> Unit,
    onResetCrop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Aspect presets chips
        Text(
            text = "ASPECT RATIO PRESETS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CropAspect.entries.forEach { aspect ->
                val isSelected = editorState.transform.cropAspect == aspect
                FilterChip(
                    selected = isSelected,
                    onClick = { onAspectSelected(aspect) },
                    label = { Text(aspect.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("crop_aspect_${aspect.name.lowercase()}")
                )
            }
        }

        // Quick rotate inside crop & Grid toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRotateCounterClockwise,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("-90°", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = onRotateClockwise,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+90°", fontSize = 11.sp)
            }

            FilterChip(
                selected = showGrid,
                onClick = onToggleGrid,
                label = { Text("Grid", fontSize = 11.sp) }
            )

            OutlinedButton(
                onClick = onResetCrop
            ) {
                Text(stringResource(R.string.action_reset), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun TransformTabContent(
    viewModel: StudioViewModel,
    transform: com.example.editor.TransformState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 90 degree rotate and flips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.rotateMinus90() },
                modifier = Modifier.weight(1f).testTag("transform_rot_minus90")
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("-90°", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = { viewModel.rotate90() },
                modifier = Modifier.weight(1f).testTag("transform_rot_plus90")
            ) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+90°", fontSize = 11.sp)
            }

            FilterChip(
                selected = transform.flipH,
                onClick = { viewModel.flipHorizontal() },
                label = { Text("Flip H", fontSize = 11.sp) },
                modifier = Modifier.testTag("transform_flip_h")
            )

            FilterChip(
                selected = transform.flipV,
                onClick = { viewModel.flipVertical() },
                label = { Text("Flip V", fontSize = 11.sp) },
                modifier = Modifier.testTag("transform_flip_v")
            )
        }

        // Straighten slider (-45 to +45)
        ToolSlider(
            label = stringResource(R.string.transform_straighten),
            value = transform.straightenAngle,
            valueRange = -45f..45f,
            unit = "°",
            onValueChange = { viewModel.setStraightenAngle(it) },
            onReset = { viewModel.setStraightenAngle(0f) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Rotation: ${transform.rotation}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = {
                    viewModel.setStraightenAngle(0f)
                    if (transform.rotation != 0) viewModel.rotate90() // quick cycle or reset
                }
            ) {
                Text(stringResource(R.string.action_reset), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun FiltersTabContent(
    viewModel: StudioViewModel,
    editorState: com.example.editor.EditorStateSnapshot
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Intensity slider
        ToolSlider(
            label = "Filter Intensity",
            value = editorState.filterIntensity * 100f,
            valueRange = 0f..100f,
            defaultValue = 85f,
            unit = "%",
            onValueChange = { viewModel.setFilterIntensity(it / 100f) }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal list of filters
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(FilterType.entries) { filter ->
                val isSelected = editorState.filterType == filter
                Card(
                    modifier = Modifier
                        .size(width = 82.dp, height = 96.dp)
                        .clickable { viewModel.setFilter(filter, editorState.filterIntensity) }
                        .testTag("filter_card_${filter.name.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Color swatch preview
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (filter) {
                                        FilterType.NONE -> Color(0xFF6B7280)
                                        FilterType.FILM -> Color(0xFF92400E)
                                        FilterType.VINTAGE -> Color(0xFFB45309)
                                        FilterType.BW -> Color(0xFF374151)
                                        FilterType.CINEMATIC -> Color(0xFF1E3A8A)
                                        FilterType.PORTRAIT -> Color(0xFFFDE68A)
                                        FilterType.WARM -> Color(0xFFEA580C)
                                        FilterType.COOL -> Color(0xFF0284C7)
                                        FilterType.FASHION -> Color(0xFFEC4899)
                                        FilterType.NIGHT -> Color(0xFF7C3AED)
                                        FilterType.TRAVEL -> Color(0xFF0D9488)
                                        FilterType.NATURE -> Color(0xFF10B981)
                                        FilterType.HDR -> Color(0xFFF59E0B)
                                        FilterType.RETRO -> Color(0xFFD97706)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = stringResource(filter.displayNameRes),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BeautyTabContent(
    viewModel: StudioViewModel,
    beauty: com.example.editor.BeautyState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp)
    ) {
        ToolSlider(
            label = "Smooth Skin",
            value = beauty.smoothing,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateBeauty { copy(smoothing = it) } },
            onReset = { viewModel.updateBeauty { copy(smoothing = 0f) } }
        )
        ToolSlider(
            label = "Skin Tone Warmth",
            value = beauty.skinTone,
            valueRange = -100f..100f,
            onValueChange = { viewModel.updateBeauty { copy(skinTone = it) } },
            onReset = { viewModel.updateBeauty { copy(skinTone = 0f) } }
        )
        ToolSlider(
            label = "Teeth Whitening",
            value = beauty.teeth,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateBeauty { copy(teeth = it) } },
            onReset = { viewModel.updateBeauty { copy(teeth = 0f) } }
        )
        ToolSlider(
            label = "Eye Brightening",
            value = beauty.eyeBrightness,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateBeauty { copy(eyeBrightness = it) } },
            onReset = { viewModel.updateBeauty { copy(eyeBrightness = 0f) } }
        )
        ToolSlider(
            label = "Studio Face Lighting",
            value = beauty.faceLighting,
            valueRange = 0f..100f,
            onValueChange = { viewModel.updateBeauty { copy(faceLighting = it) } },
            onReset = { viewModel.updateBeauty { copy(faceLighting = 0f) } }
        )

        // Realistic processing notice
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.ai_beauty_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AIStudioTabContent(
    viewModel: StudioViewModel,
    prompt: String,
    onPromptChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.aiEngine.statusDescription,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Natural Language AI Prompt input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = { Text("e.g., cinematic lighting, 80s film look", fontSize = 12.sp) },
                modifier = Modifier.weight(1f).testTag("ai_prompt_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Button(
                onClick = { viewModel.editImageWithAI(prompt) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier.testTag("run_ai_edit_btn")
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Quick AI actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { viewModel.runAutoEnhance() },
                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) },
                label = { Text("Auto Enhance (Local)", fontSize = 11.sp) },
                modifier = Modifier.testTag("ai_auto_enhance_btn")
            )

            FilterChip(
                selected = false,
                onClick = { viewModel.navigateTo(StudioScreen.OBJECT_REMOVER) },
                label = { Text("Remove Object", fontSize = 11.sp) },
                modifier = Modifier.testTag("ai_remove_obj_btn")
            )

            FilterChip(
                selected = false,
                onClick = { viewModel.runOutfitChange("Casual Chic") },
                label = { Text("AI Outfit", fontSize = 11.sp) }
            )

            FilterChip(
                selected = false,
                onClick = { viewModel.runAIFilterGenerate("cyberpunk neon sunset") },
                label = { Text("AI Filter Gen", fontSize = 11.sp) }
            )
        }
    }
}

@Composable
private fun TextTabContent(
    viewModel: StudioViewModel,
    editorState: com.example.editor.EditorStateSnapshot,
    newText: String,
    onNewTextChange: (String) -> Unit,
    selectedId: Long?,
    onSelectId: (Long?) -> Unit
) {
    val texts = editorState.textOverlays
    val activeOverlay = texts.find { it.id == selectedId } ?: texts.lastOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Add new text field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newText,
                onValueChange = onNewTextChange,
                placeholder = { Text(stringResource(R.string.text_hint), fontSize = 12.sp) },
                modifier = Modifier.weight(1f).testTag("text_input_field"),
                singleLine = true
            )

            Button(
                onClick = {
                    if (newText.isNotBlank()) {
                        viewModel.addTextOverlay(newText)
                        onNewTextChange("")
                    }
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier.testTag("add_text_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.add_text), fontSize = 11.sp)
            }
        }

        // Active text styling controls
        if (activeOverlay != null) {
            Text(
                text = "Styling: \"${activeOverlay.text}\"",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Font styles (Bold, Italic, Alignments)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = activeOverlay.isBold,
                    onClick = { viewModel.updateTextOverlay(activeOverlay.id) { copy(isBold = !isBold) } },
                    label = { Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(16.dp)) }
                )

                FilterChip(
                    selected = activeOverlay.isItalic,
                    onClick = { viewModel.updateTextOverlay(activeOverlay.id) { copy(isItalic = !isItalic) } },
                    label = { Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(16.dp)) }
                )

                FilterChip(
                    selected = activeOverlay.alignment == 0,
                    onClick = { viewModel.updateTextOverlay(activeOverlay.id) { copy(alignment = 0) } },
                    label = { Icon(Icons.Default.FormatAlignLeft, contentDescription = "Left", modifier = Modifier.size(16.dp)) }
                )

                FilterChip(
                    selected = activeOverlay.alignment == 1,
                    onClick = { viewModel.updateTextOverlay(activeOverlay.id) { copy(alignment = 1) } },
                    label = { Icon(Icons.Default.FormatAlignCenter, contentDescription = "Center", modifier = Modifier.size(16.dp)) }
                )

                FilterChip(
                    selected = activeOverlay.alignment == 2,
                    onClick = { viewModel.updateTextOverlay(activeOverlay.id) { copy(alignment = 2) } },
                    label = { Icon(Icons.Default.FormatAlignRight, contentDescription = "Right", modifier = Modifier.size(16.dp)) }
                )

                IconButton(
                    onClick = { viewModel.removeTextOverlay(activeOverlay.id) },
                    modifier = Modifier.testTag("delete_text_overlay_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Font Family selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("sans-serif", "serif", "monospace", "cursive").forEach { font ->
                    FilterChip(
                        selected = activeOverlay.fontFamily == font,
                        onClick = { viewModel.updateTextOverlay(activeOverlay.id) { copy(fontFamily = font) } },
                        label = { Text(font, fontSize = 11.sp) }
                    )
                }
            }

            // Colors
            val colors = listOf(
                android.graphics.Color.WHITE,
                android.graphics.Color.BLACK,
                android.graphics.Color.YELLOW,
                android.graphics.Color.RED,
                android.graphics.Color.CYAN,
                android.graphics.Color.GREEN,
                android.graphics.Color.MAGENTA
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colors.forEach { col ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(col))
                            .border(
                                width = if (activeOverlay.color == col) 2.5.dp else 1.dp,
                                color = if (activeOverlay.color == col) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable {
                                viewModel.updateTextOverlay(activeOverlay.id) { copy(color = col) }
                            }
                    )
                }
            }

            // Shadow & Background Badge toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeOverlay.hasShadow,
                    onClick = { viewModel.updateTextOverlay(activeOverlay.id) { copy(hasShadow = !hasShadow) } },
                    label = { Text("Shadow", fontSize = 11.sp) }
                )

                FilterChip(
                    selected = activeOverlay.hasBackground,
                    onClick = { viewModel.updateTextOverlay(activeOverlay.id) { copy(hasBackground = !hasBackground) } },
                    label = { Text("Badge Background", fontSize = 11.sp) }
                )
            }

            // Size slider
            ToolSlider(
                label = "Font Size",
                value = activeOverlay.sizeSp,
                valueRange = 12f..72f,
                unit = "sp",
                onValueChange = { viewModel.updateTextOverlay(activeOverlay.id) { copy(sizeSp = it) } }
            )
        }
    }
}

@Composable
private fun ExportTabContent(
    viewModel: StudioViewModel,
    format: String,
    onFormatChange: (String) -> Unit,
    quality: Int,
    onQualityChange: (Int) -> Unit,
    resolution: String,
    onResolutionChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Format & Resolution
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("JPG", "PNG").forEach { fmt ->
                    FilterChip(
                        selected = format == fmt,
                        onClick = { onFormatChange(fmt) },
                        label = { Text(fmt, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Original", "4K", "2K", "1080P").forEach { res ->
                    FilterChip(
                        selected = resolution == res,
                        onClick = { onResolutionChange(res) },
                        label = { Text(res, fontSize = 10.sp) }
                    )
                }
            }
        }

        // Quality slider (for JPG)
        if (format == "JPG") {
            ToolSlider(
                label = "Export Quality",
                value = quality.toFloat(),
                valueRange = 50f..100f,
                defaultValue = 92f,
                unit = "%",
                onValueChange = { onQualityChange(it.roundToInt()) }
            )
        }

        // Save and Share Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.exportToGallery(format, quality, resolution) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f).testTag("save_gallery_button")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.btn_save_gallery), color = Color.Black, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.shareCurrentPhoto() },
                modifier = Modifier.weight(1f).testTag("share_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.btn_share_image), fontWeight = FontWeight.Bold)
            }
        }
    }
}
