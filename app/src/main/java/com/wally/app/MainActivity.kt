package com.wally.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wally.app.ui.screens.WallyMainScreen
import com.wally.app.ui.theme.WallyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as WallyApplication).container

        setContent {
            WallyTheme {
                WallyMainScreen(
                    repository = appContainer.wallpaperRepository,
                    savedStorage = appContainer.savedStorage,
                    wallpaperSetter = appContainer.wallpaperSetter,
                    sourcesConfigured = appContainer.sourcesConfiguredMap
                )
            }
        }
    }
}
