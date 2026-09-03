package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.editor.ImageProcessor
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CollageScreen(
    viewModel: StudioViewModel
) {
    val context = LocalContext.current
    var selectedLayout by remember { mutableIntStateOf(2) } // 2, 3, or 4
    val collageSlots = remember { mutableStateListOf<Bitmap?>(null, null, null, null) }
    var activeSlotIndex by remember { mutableIntStateOf(0) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val bmp = ImageProcessor.loadOptimizedBitmap(context, uri, 1000)
                if (bmp != null && activeSlotIndex in 0 until 4) {
                    collageSlots[activeSlotIndex] = bmp
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("collage_screen")
    ) {
        // Collage Canvas Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .testTag("collage_container_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                when (selectedLayout) {
                    2 -> {
                        Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            CollageSlotBox(
                                bitmap = collageSlots[0],
                                modifier = Modifier.weight(1f).fillMaxSize(),
                                onClick = {
                                    activeSlotIndex = 0
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            CollageSlotBox(
                                bitmap = collageSlots[1],
                                modifier = Modifier.weight(1f).fillMaxSize(),
                                onClick = {
                                    activeSlotIndex = 1
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                        }
                    }

                    3 -> {
                        Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            CollageSlotBox(
                                bitmap = collageSlots[0],
                                modifier = Modifier.weight(1f).fillMaxSize(),
                                onClick = {
                                    activeSlotIndex = 0
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                                CollageSlotBox(
                                    bitmap = collageSlots[1],
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                    onClick = {
                                        activeSlotIndex = 1
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CollageSlotBox(
                                    bitmap = collageSlots[2],
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                    onClick = {
                                        activeSlotIndex = 2
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                )
                            }
                        }
                    }

                    4 -> {
                        Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                            Row(modifier = Modifier.weight(1f).fillMaxSize()) {
                                CollageSlotBox(
                                    bitmap = collageSlots[0],
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                    onClick = {
                                        activeSlotIndex = 0
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                CollageSlotBox(
                                    bitmap = collageSlots[1],
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                    onClick = {
                                        activeSlotIndex = 1
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.weight(1f).fillMaxSize()) {
                                CollageSlotBox(
                                    bitmap = collageSlots[2],
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                    onClick = {
                                        activeSlotIndex = 2
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                CollageSlotBox(
                                    bitmap = collageSlots[3],
                                    modifier = Modifier.weight(1f).fillMaxSize(),
                                    onClick = {
                                        activeSlotIndex = 3
                                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Lower Layout Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Collage Templates",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedLayout == 2,
                        onClick = { selectedLayout = 2 },
                        label = { Text(stringResource(R.string.layout_2_photo)) },
                        modifier = Modifier.weight(1f).testTag("collage_2_layout")
                    )
                    FilterChip(
                        selected = selectedLayout == 3,
                        onClick = { selectedLayout = 3 },
                        label = { Text(stringResource(R.string.layout_3_photo)) },
                        modifier = Modifier.weight(1f).testTag("collage_3_layout")
                    )
                    FilterChip(
                        selected = selectedLayout == 4,
                        onClick = { selectedLayout = 4 },
                        label = { Text(stringResource(R.string.layout_4_photo)) },
                        modifier = Modifier.weight(1f).testTag("collage_4_layout")
                    )
                }

                Button(
                    onClick = {
                        val firstAvailable = collageSlots.firstOrNull { it != null }
                        if (firstAvailable != null) {
                            viewModel.setNewBitmap(firstAvailable, "Collage_Composite")
                            viewModel.navigateTo(StudioScreen.EDIT)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("open_collage_in_editor_btn")
                ) {
                    Text("Open in Studio Editor")
                }
            }
        }
    }
}

@Composable
fun CollageSlotBox(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE2E8F0))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Collage photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Tap to add",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
