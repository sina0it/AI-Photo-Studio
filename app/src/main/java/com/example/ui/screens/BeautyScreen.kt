package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PhotoCanvas
import com.example.ui.components.ToolSlider
import com.example.viewmodel.StudioViewModel

@Composable
fun BeautyScreen(
    viewModel: StudioViewModel
) {
    val originalBitmap by viewModel.originalBitmap.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val isComparing by viewModel.isComparing.collectAsState()
    val editorState by viewModel.editorState.collectAsState()
    val beauty = editorState.beauty

    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = listOf(
        stringResource(R.string.cat_skin),
        stringResource(R.string.cat_makeup),
        stringResource(R.string.cat_face),
        stringResource(R.string.cat_hair)
    )

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.loadPhotoFromUri(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("beauty_screen")
    ) {
        // Photo Canvas
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
        }

        // Control Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 0.dp,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        categories.forEachIndexed { index, name ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.resetBeauty() },
                        modifier = Modifier.testTag("reset_beauty_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_reset),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // SKIN
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                ToolSlider(
                                    label = stringResource(R.string.beauty_smoothing),
                                    value = beauty.smoothing,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(smoothing = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_tone),
                                    value = beauty.skinTone,
                                    valueRange = -100f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(skinTone = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_blemish),
                                    value = beauty.blemish,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(blemish = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_teeth),
                                    value = beauty.teeth,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(teeth = it) } }
                                )
                            }
                        }

                        1 -> {
                            // MAKEUP
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                ToolSlider(
                                    label = stringResource(R.string.beauty_blush),
                                    value = beauty.blush,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(blush = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_lipstick),
                                    value = beauty.lipstick,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(lipstick = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_eyeliner),
                                    value = beauty.eyeliner,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(eyeliner = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_eyeshadow),
                                    value = beauty.eyeshadow,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(eyeshadow = it) } }
                                )
                            }
                        }

                        2 -> {
                            // FACE
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                ToolSlider(
                                    label = stringResource(R.string.beauty_face_shape),
                                    value = beauty.faceShape,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(faceShape = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_jaw),
                                    value = beauty.jaw,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(jaw = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_nose),
                                    value = beauty.nose,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(nose = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_chin),
                                    value = beauty.chin,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(chin = it) } }
                                )
                            }
                        }

                        3 -> {
                            // HAIR
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                ToolSlider(
                                    label = stringResource(R.string.beauty_hair_color),
                                    value = beauty.hairColor,
                                    valueRange = -100f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(hairColor = it) } }
                                )
                                ToolSlider(
                                    label = stringResource(R.string.beauty_eyebrows),
                                    value = beauty.eyebrows,
                                    valueRange = 0f..100f,
                                    onValueChange = { viewModel.updateBeauty { copy(eyebrows = it) } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
