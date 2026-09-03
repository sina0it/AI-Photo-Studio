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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.PhotoCanvas
import com.example.ui.components.ToolSlider
import com.example.viewmodel.StudioViewModel

@Composable
fun BodyScreen(
    viewModel: StudioViewModel
) {
    val originalBitmap by viewModel.originalBitmap.collectAsState()
    val previewBitmap by viewModel.previewBitmap.collectAsState()
    val isComparing by viewModel.isComparing.collectAsState()
    val editorState by viewModel.editorState.collectAsState()
    val body = editorState.body

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.loadPhotoFromUri(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("body_screen")
    ) {
        // Canvas
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

        // Body Controls Panel
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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.body_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = stringResource(R.string.body_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.resetBody() },
                        modifier = Modifier.testTag("reset_body_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.body_reset),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ToolSlider(
                        label = stringResource(R.string.body_slim),
                        value = body.slim,
                        valueRange = -50f..50f,
                        onValueChange = { viewModel.updateBody { copy(slim = it) } }
                    )
                    ToolSlider(
                        label = stringResource(R.string.body_waist),
                        value = body.waist,
                        valueRange = -50f..50f,
                        onValueChange = { viewModel.updateBody { copy(waist = it) } }
                    )
                    ToolSlider(
                        label = stringResource(R.string.body_shoulders),
                        value = body.shoulders,
                        valueRange = -50f..50f,
                        onValueChange = { viewModel.updateBody { copy(shoulders = it) } }
                    )
                    ToolSlider(
                        label = stringResource(R.string.body_arms),
                        value = body.arms,
                        valueRange = -50f..50f,
                        onValueChange = { viewModel.updateBody { copy(arms = it) } }
                    )
                    ToolSlider(
                        label = stringResource(R.string.body_legs),
                        value = body.legs,
                        valueRange = -50f..50f,
                        onValueChange = { viewModel.updateBody { copy(legs = it) } }
                    )
                    ToolSlider(
                        label = stringResource(R.string.body_hips),
                        value = body.hips,
                        valueRange = -50f..50f,
                        onValueChange = { viewModel.updateBody { copy(hips = it) } }
                    )
                    ToolSlider(
                        label = stringResource(R.string.body_height),
                        value = body.height,
                        valueRange = -50f..50f,
                        onValueChange = { viewModel.updateBody { copy(height = it) } }
                    )
                }
            }
        }
    }
}
