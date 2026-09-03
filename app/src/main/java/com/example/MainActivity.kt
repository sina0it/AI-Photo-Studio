package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.StudioBottomNav
import com.example.ui.components.StudioTopAppBar
import com.example.ui.screens.AIStudioScreen
import com.example.ui.screens.BeautyScreen
import com.example.ui.screens.BodyScreen
import com.example.ui.screens.CollageScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.FiltersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageSelectScreen
import com.example.ui.screens.ObjectRemoverScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StudioScreen
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.languageManager.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val currentLang by viewModel.languageManager.currentLanguage.collectAsState()
            val layoutDirection = viewModel.languageManager.getLayoutDirection()

            MyApplicationTheme(darkTheme = isDark) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    StudioMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun StudioMainApp(viewModel: StudioViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val originalBitmap by viewModel.originalBitmap.collectAsState()
    val dialogMessage by viewModel.dialogMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    if (currentScreen == StudioScreen.LANGUAGE_SELECT) {
        LanguageSelectScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            StudioTopAppBar(
                viewModel = viewModel,
                currentScreen = currentScreen,
                canUndo = canUndo,
                canRedo = canRedo,
                hasActivePhoto = originalBitmap != null,
                onNavigateBack = {
                    when (currentScreen) {
                        StudioScreen.SETTINGS -> viewModel.navigateTo(StudioScreen.HOME)
                        StudioScreen.EXPORT -> viewModel.navigateTo(StudioScreen.EDIT)
                        StudioScreen.OBJECT_REMOVER -> viewModel.navigateTo(StudioScreen.TOOLS)
                        StudioScreen.COLLAGE -> viewModel.navigateTo(StudioScreen.TOOLS)
                        else -> viewModel.navigateTo(StudioScreen.HOME)
                    }
                },
                onOpenSettings = {
                    viewModel.navigateTo(StudioScreen.SETTINGS)
                }
            )
        },
        bottomBar = {
            if (currentScreen != StudioScreen.SETTINGS &&
                currentScreen != StudioScreen.EXPORT &&
                currentScreen != StudioScreen.OBJECT_REMOVER &&
                currentScreen != StudioScreen.COLLAGE
            ) {
                StudioBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    StudioScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    StudioScreen.AI_STUDIO -> AIStudioScreen(viewModel = viewModel)
                    StudioScreen.EDIT -> EditorScreen(viewModel = viewModel)
                    StudioScreen.BEAUTY -> BeautyScreen(viewModel = viewModel)
                    StudioScreen.BODY -> BodyScreen(viewModel = viewModel)
                    StudioScreen.FILTERS -> FiltersScreen(viewModel = viewModel)
                    StudioScreen.TOOLS -> ToolsScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    StudioScreen.OBJECT_REMOVER -> ObjectRemoverScreen(viewModel = viewModel)
                    StudioScreen.COLLAGE -> CollageScreen(viewModel = viewModel)
                    StudioScreen.EXPORT -> ExportScreen(viewModel = viewModel)
                    StudioScreen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    StudioScreen.ABOUT -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                    StudioScreen.LANGUAGE_SELECT -> LanguageSelectScreen(viewModel = viewModel)
                }
            }
        }

        // Informational Dialog (e.g. AI Engine backend configuration)
        dialogMessage?.let { (title, message) ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text(title) },
                text = { Text(message) },
                confirmButton = {
                    Button(onClick = { viewModel.dismissDialog() }) {
                        Text(stringResource(R.string.action_apply))
                    }
                }
            )
        }
    }
}
