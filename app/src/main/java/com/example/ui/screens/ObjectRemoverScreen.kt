package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ToolSlider
import com.example.viewmodel.StudioViewModel

data class StrokePath(
    val points: List<Offset>,
    val radius: Float,
    val isEraser: Boolean = false
)

@Composable
fun ObjectRemoverScreen(
    viewModel: StudioViewModel
) {
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    var brushSize by remember { mutableFloatStateOf(35f) }
    var isEraserMode by remember { mutableStateOf(false) }

    val strokePaths = remember { mutableStateListOf<StrokePath>() }
    val undoneStrokes = remember { mutableStateListOf<StrokePath>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("object_remover_screen")
    ) {
        // Upper Canvas with Interactive Mask Drawing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            if (previewBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = previewBitmap!!.asImageBitmap(),
                    contentDescription = "Remove object photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Brush drawing overlay
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(brushSize, isEraserMode) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints.clear()
                                currentPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    strokePaths.add(StrokePath(currentPoints.toList(), brushSize, isEraserMode))
                                    undoneStrokes.clear()
                                    currentPoints.clear()
                                }
                            }
                        )
                    }
                    .testTag("mask_draw_canvas")
            ) {
                // Draw existing strokes
                for (stroke in strokePaths) {
                    val strokeColor = if (stroke.isEraser) Color.Black.copy(alpha = 0.8f) else Color(0x99EF4444)
                    for (i in 0 until stroke.points.size - 1) {
                        drawLine(
                            color = strokeColor,
                            start = stroke.points[i],
                            end = stroke.points[i + 1],
                            strokeWidth = stroke.radius * 2,
                            cap = StrokeCap.Round
                        )
                    }
                }
                // Draw active stroke
                val activeColor = if (isEraserMode) Color.Black.copy(alpha = 0.8f) else Color(0x99EF4444)
                for (i in 0 until currentPoints.size - 1) {
                    drawLine(
                        color = activeColor,
                        start = currentPoints[i],
                        end = currentPoints[i + 1],
                        strokeWidth = brushSize * 2,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Lower Control Panel
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mode selector + Undo / Redo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isEraserMode,
                            onClick = { isEraserMode = false },
                            leadingIcon = { Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Brush", fontSize = 12.sp) }
                        )

                        FilterChip(
                            selected = isEraserMode,
                            onClick = { isEraserMode = true },
                            leadingIcon = { Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Eraser", fontSize = 12.sp) }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (strokePaths.isNotEmpty()) {
                                    val last = strokePaths.removeAt(strokePaths.size - 1)
                                    undoneStrokes.add(last)
                                }
                            },
                            enabled = strokePaths.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo stroke",
                                tint = if (strokePaths.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(
                            onClick = {
                                if (undoneStrokes.isNotEmpty()) {
                                    val next = undoneStrokes.removeAt(undoneStrokes.size - 1)
                                    strokePaths.add(next)
                                }
                            },
                            enabled = undoneStrokes.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo stroke",
                                tint = if (undoneStrokes.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Brush Size Slider
                ToolSlider(
                    label = stringResource(R.string.brush_size),
                    value = brushSize,
                    valueRange = 10f..100f,
                    defaultValue = 35f,
                    unit = "px",
                    onValueChange = { brushSize = it }
                )

                // Actions: Clear & Remove
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            strokePaths.clear()
                            undoneStrokes.clear()
                            currentPoints.clear()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_mask_btn")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.clear_mask), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val original = previewBitmap
                            val maskBitmap = if (original != null && strokePaths.isNotEmpty()) {
                                val mask = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ALPHA_8)
                                val canvas = android.graphics.Canvas(mask)
                                val paint = Paint().apply {
                                    isAntiAlias = true
                                    color = android.graphics.Color.WHITE
                                    strokeCap = Paint.Cap.ROUND
                                }
                                for (stroke in strokePaths) {
                                    paint.strokeWidth = stroke.radius * 2
                                    for (i in 0 until stroke.points.size - 1) {
                                        canvas.drawLine(
                                            stroke.points[i].x, stroke.points[i].y,
                                            stroke.points[i + 1].x, stroke.points[i + 1].y,
                                            paint
                                        )
                                    }
                                }
                                mask
                            } else null

                            viewModel.runRemoveObject(maskBitmap)
                            strokePaths.clear()
                            undoneStrokes.clear()
                            currentPoints.clear()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("execute_remove_object_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_remove_object), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
