package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

@Composable
fun ToolsScreen(
    viewModel: StudioViewModel,
    onNavigate: (StudioScreen) -> Unit
) {
    val context = LocalContext.current
    val editorState by viewModel.editorState.collectAsState()

    var showTextDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    var showAdUnlockDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("tools_screen_scroll"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Rewarded Ad Pro Access Banner (Tapsell architecture)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rewarded_ad_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.rewarded_ad_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(R.string.rewarded_ad_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.watchRewardedAd(context as? Activity) {
                                // Pro unlocked
                            }
                        },
                        modifier = Modifier.testTag("watch_ad_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.ad_ready), fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. Creative Overlays (Text & Stickers)
        item {
            Text(
                text = stringResource(R.string.text_and_stickers),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolActionCard(
                    title = stringResource(R.string.tool_text),
                    subtitle = "Studio typography",
                    icon = Icons.Default.TextFields,
                    modifier = Modifier.weight(1f),
                    onClick = { showTextDialog = true }
                )

                ToolActionCard(
                    title = stringResource(R.string.tool_stickers),
                    subtitle = "Emojis & badges",
                    icon = Icons.Default.EmojiEmotions,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.addSticker("✨")
                    }
                )
            }
        }

        // 3. Quick Stickers Strip
        item {
            Text(
                text = "Quick Studio Stickers",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val sampleStickers = listOf("✨", "🔥", "📸", "👑", "❤️", "🎨", "🌟", "💫", "💎", "⚡")
                items(sampleStickers) { sticker ->
                    Card(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { viewModel.addSticker(sticker) }
                            .testTag("sticker_$sticker"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(sticker, fontSize = 22.sp)
                        }
                    }
                }
            }
        }

        // 4. Background Studio
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.bg_tools_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolActionCard(
                    title = stringResource(R.string.bg_remover),
                    subtitle = "One-tap background cut out",
                    icon = Icons.Default.Layers,
                    onClick = {
                        if (viewModel.originalBitmap.value == null) viewModel.loadSamplePhoto()
                        viewModel.runRemoveObject()
                    }
                )
                ToolActionCard(
                    title = stringResource(R.string.bg_blur),
                    subtitle = "Portrait bokeh depth simulation",
                    icon = Icons.Default.BlurOn,
                    onClick = {
                        viewModel.updateAdjustments { copy(blur = 30f) }
                        onNavigate(StudioScreen.EDIT)
                    }
                )
                ToolActionCard(
                    title = stringResource(R.string.object_remover_title),
                    subtitle = stringResource(R.string.object_remover_desc),
                    icon = Icons.Default.Brush,
                    onClick = {
                        onNavigate(StudioScreen.OBJECT_REMOVER)
                    }
                )
            }
        }

        // 5. Layout & Collage
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tool_collage),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolActionCard(
                    title = stringResource(R.string.layout_2_photo),
                    subtitle = "Split duet",
                    icon = Icons.Default.GridOn,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StudioScreen.COLLAGE) }
                )
                ToolActionCard(
                    title = stringResource(R.string.layout_4_photo),
                    subtitle = "Quad grid",
                    icon = Icons.Default.GridOn,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(StudioScreen.COLLAGE) }
                )
            }
        }
    }

    // Add text dialog
    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text(stringResource(R.string.add_text)) },
            text = {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(stringResource(R.string.text_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_text_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addTextOverlay(inputText)
                        inputText = ""
                        showTextDialog = false
                    },
                    modifier = Modifier.testTag("add_text_confirm_btn")
                ) {
                    Text(stringResource(R.string.action_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun ToolActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tool_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
