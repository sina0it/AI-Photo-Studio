package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PhotoCanvas
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

@Composable
fun AIStudioScreen(
    viewModel: StudioViewModel
) {
    val originalBitmap by viewModel.originalBitmap.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val isComparing by viewModel.isComparing.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var promptText by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.loadPhotoFromUri(uri)
    }

    val examplePrompts = listOf(
        stringResource(R.string.prompt_cinematic_lighting),
        stringResource(R.string.prompt_remove_person),
        stringResource(R.string.prompt_change_shirt),
        stringResource(R.string.prompt_luxury_studio),
        stringResource(R.string.prompt_sunset_sky),
        stringResource(R.string.prompt_sharpen_quality)
    )

    val outfitStyles = listOf(
        stringResource(R.string.outfit_black_shirt),
        stringResource(R.string.outfit_formal_suit),
        stringResource(R.string.outfit_red_dress),
        stringResource(R.string.outfit_leather_jacket),
        stringResource(R.string.outfit_streetwear)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_studio_screen")
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

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.ai_processing),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Lower AI Control Center
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AI Engine Status Indicator Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (viewModel.aiEngine.isConfigured) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (viewModel.aiEngine.isConfigured) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.aiEngine.statusDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // AI Auto Enhance Quick Button
                Button(
                    onClick = { viewModel.runAutoEnhance() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_auto_enhance_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ai_enhance_title), fontWeight = FontWeight.Bold)
                }

                // Natural Language Prompt Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_prompt_input"),
                        placeholder = {
                            Text(
                                stringResource(R.string.prompt_placeholder),
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (promptText.isNotBlank()) {
                                viewModel.runAIGeneratePrompt(promptText)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("ai_send_prompt_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = stringResource(R.string.btn_generate),
                            tint = Color.Black
                        )
                    }
                }

                // AI Studio Tools Quick Actions
                Text(
                    text = "AI STUDIO TOOLS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.navigateTo(StudioScreen.OBJECT_REMOVER) },
                        label = { Text("Remove Object", fontSize = 12.sp) },
                        modifier = Modifier.testTag("ai_tool_remove_object")
                    )

                    FilterChip(
                        selected = false,
                        onClick = { viewModel.runAIGeneratePrompt("Replace background with professional modern studio backdrop") },
                        label = { Text("Replace Background", fontSize = 12.sp) },
                        modifier = Modifier.testTag("ai_tool_replace_bg")
                    )

                    FilterChip(
                        selected = false,
                        onClick = { viewModel.runAIGeneratePrompt("Generative fill and expand edges naturally") },
                        label = { Text("Generative Fill", fontSize = 12.sp) },
                        modifier = Modifier.testTag("ai_tool_gen_fill")
                    )

                    FilterChip(
                        selected = false,
                        onClick = { viewModel.runAIGeneratePrompt("Cinematic 85mm prime lens studio portrait lighting") },
                        label = { Text("Studio Portrait", fontSize = 12.sp) },
                        modifier = Modifier.testTag("ai_tool_portrait")
                    )

                    FilterChip(
                        selected = false,
                        onClick = { viewModel.runAIFilterGenerate("Cyberpunk Neon Night") },
                        label = { Text("AI Filter Generator", fontSize = 12.sp) },
                        modifier = Modifier.testTag("ai_tool_filter_gen")
                    )
                }

                // Example Prompts Chips
                Text(
                    text = stringResource(R.string.example_prompts_title),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    examplePrompts.forEach { example ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                promptText = example
                                viewModel.runAIGeneratePrompt(example)
                            },
                            label = { Text(example, fontSize = 12.sp) },
                            modifier = Modifier.testTag("example_prompt_${example.take(8).lowercase()}")
                        )
                    }
                }

                // AI Wardrobe / Outfit Change
                Text(
                    text = stringResource(R.string.outfit_title),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    outfitStyles.forEach { style ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.runOutfitChange(style) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Checkroom,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(style, fontSize = 12.sp) },
                            modifier = Modifier.testTag("outfit_${style.take(6).lowercase()}")
                        )
                    }
                }
            }
        }
    }
}
