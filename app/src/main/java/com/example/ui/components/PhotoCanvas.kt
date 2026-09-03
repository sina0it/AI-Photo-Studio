package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlin.math.roundToInt

enum class CompareMode {
    HOLD, SPLIT_SWIPE, SIDE_BY_SIDE
}

@Composable
fun PhotoCanvas(
    originalBitmap: Bitmap?,
    previewBitmap: Bitmap?,
    isComparing: Boolean,
    onImportRequested: () -> Unit,
    modifier: Modifier = Modifier,
    enableComparisonModes: Boolean = true
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var compareMode by remember { mutableStateOf(CompareMode.HOLD) }
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E11))
            .clipToBounds()
            .testTag("photo_canvas_container"),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        val activeBitmap = if ((isComparing || compareMode == CompareMode.HOLD && isComparing) && originalBitmap != null) {
            originalBitmap
        } else {
            previewBitmap ?: originalBitmap
        }

        if (activeBitmap != null) {
            when (compareMode) {
                CompareMode.HOLD -> {
                    // Standard canvas with Pinch-to-zoom, Pan, and Double-tap-zoom
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { tapOffset ->
                                        if (scale > 1.2f) {
                                            scale = 1f
                                            panOffset = Offset.Zero
                                        } else {
                                            scale = 2.5f
                                            panOffset = Offset(
                                                (size.width / 2f - tapOffset.x) * 1.5f,
                                                (size.height / 2f - tapOffset.y) * 1.5f
                                            )
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                                    val maxPanX = (size.width * (newScale - 1f)) / 2f
                                    val maxPanY = (size.height * (newScale - 1f)) / 2f
                                    val newPanX = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX)
                                    val newPanY = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)

                                    scale = newScale
                                    panOffset = if (newScale > 1f) Offset(newPanX, newPanY) else Offset.Zero
                                }
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = activeBitmap.asImageBitmap(),
                            contentDescription = "Active Studio Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("active_photo_image"),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                CompareMode.SPLIT_SWIPE -> {
                    // Interactive Swipe Comparison: Left side = Original, Right side = Edited
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, _, _ ->
                                    val delta = pan.x / size.width
                                    splitFraction = (splitFraction + delta).coerceIn(0.05f, 0.95f)
                                }
                            }
                    ) {
                        val containerWidthPx = constraints.maxWidth.toFloat()
                        val containerHeightPx = constraints.maxHeight.toFloat()

                        // Background: Edited bitmap
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Edited Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Clipped Left overlay: Original bitmap
                        if (originalBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(splitFraction)
                                    .clipToBounds()
                            ) {
                                Image(
                                    bitmap = originalBitmap.asImageBitmap(),
                                    contentDescription = "Original Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        // Draggable Divider Line & Handle
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .offset { IntOffset((splitFraction * containerWidthPx).roundToInt(), 0) }
                                .background(Color.White)
                        )

                        // Central Pill Handle on divider
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .offset {
                                    IntOffset(
                                        (splitFraction * containerWidthPx - 18.dp.toPx()).roundToInt(),
                                        (containerHeightPx / 2f - 18.dp.toPx()).roundToInt()
                                    )
                                }
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compare,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Labels: Before / After
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.filter_none).uppercase(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "EDITED",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                CompareMode.SIDE_BY_SIDE -> {
                    // Side by Side view: Left is Original, Right is Preview
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BEFORE",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            if (originalBitmap != null) {
                                Image(
                                    bitmap = originalBitmap.asImageBitmap(),
                                    contentDescription = "Original",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AFTER",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap.asImageBitmap(),
                                    contentDescription = "Edited",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            // Quick Floating "Fit to Screen" Reset Button (visible when zoomed in)
            if (scale > 1.05f) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clickable {
                            scale = 1f
                            panOffset = Offset.Zero
                        }
                        .testTag("fit_to_screen_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitScreen,
                            contentDescription = "Fit to Screen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fit (${(scale * 100).toInt()}%)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Comparison Controls Pill (Top Right)
            if (enableComparisonModes && originalBitmap != null && previewBitmap != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Split swipe toggle
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (compareMode == CompareMode.SPLIT_SWIPE)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Black.copy(alpha = 0.65f)
                            )
                            .clickable {
                                compareMode = if (compareMode == CompareMode.SPLIT_SWIPE) CompareMode.HOLD else CompareMode.SPLIT_SWIPE
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Split View",
                            tint = if (compareMode == CompareMode.SPLIT_SWIPE) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Side-by-side toggle
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (compareMode == CompareMode.SIDE_BY_SIDE)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Black.copy(alpha = 0.65f)
                            )
                            .clickable {
                                compareMode = if (compareMode == CompareMode.SIDE_BY_SIDE) CompareMode.HOLD else CompareMode.SIDE_BY_SIDE
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewColumn,
                            contentDescription = "Side by Side",
                            tint = if (compareMode == CompareMode.SIDE_BY_SIDE) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Hold-to-Compare Indicator banner
            AnimatedVisibility(
                visible = isComparing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "ORIGINAL (BEFORE)",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

        } else {
            // Empty state placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 12.dp)
                )
                Text(
                    text = stringResource(R.string.hero_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.hero_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onImportRequested,
                    modifier = Modifier.testTag("canvas_import_button")
                ) {
                    Text(stringResource(R.string.btn_choose_photo))
                }
            }
        }
    }
}
