package com.wally.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import com.wally.app.data.persistence.SavedWallpapersStorage
import com.wally.app.data.repository.WallpaperRepository
import com.wally.app.domain.wallpaper.WallpaperSetter
import com.wally.app.ui.components.GlassmorphicBottomBar
import com.wally.app.ui.components.WallpaperPreviewSheet
import com.wally.app.ui.components.WallyTab
import com.wally.app.ui.theme.WallyBg
import kotlinx.coroutines.launch

@Composable
fun WallyMainScreen(
    repository: WallpaperRepository,
    savedStorage: SavedWallpapersStorage,
    wallpaperSetter: WallpaperSetter,
    sourcesConfigured: Map<WallpaperSourceType, Boolean>,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(WallyTab.HOME) }
    var selectedWallpaperForPreview by remember { mutableStateOf<WallpaperItem?>(null) }

    // Home feed state
    val lastCategory by savedStorage.lastCategory.collectAsState(initial = "All")
    var selectedCategory by remember { mutableStateOf("All") }
    var homeWallpapers by remember { mutableStateOf<List<WallpaperItem>>(emptyList()) }
    var isHomeLoading by remember { mutableStateOf(false) }

    // Explore feed state
    var searchQuery by remember { mutableStateOf("") }
    var selectedSourceFilter by remember { mutableStateOf<WallpaperSourceType?>(null) }
    var exploreResults by remember { mutableStateOf<List<WallpaperItem>>(emptyList()) }
    var isExploreLoading by remember { mutableStateOf(false) }

    // Saved list state
    val savedWallpapers by savedStorage.savedWallpapers.collectAsState(initial = emptyList())
    val savedIds by savedStorage.savedIds.collectAsState(initial = emptySet())

    val todayPick = remember { repository.getTodayPick() }

    // Sync initial category from DataStore
    LaunchedEffect(lastCategory) {
        if (selectedCategory == "All" && lastCategory.isNotBlank()) {
            selectedCategory = lastCategory
        }
    }

    // Load home wallpapers when category changes
    LaunchedEffect(selectedCategory) {
        isHomeLoading = true
        val cat = if (selectedCategory == "All") null else selectedCategory
        homeWallpapers = repository.getWallpapers(category = cat, page = 1)
        isHomeLoading = false
    }

    // Load explore search results
    LaunchedEffect(searchQuery) {
        isExploreLoading = true
        val q = searchQuery.ifBlank { null }
        exploreResults = repository.getWallpapers(query = q, page = 1)
        isExploreLoading = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WallyBg)
    ) {
        // Tab Content
        when (currentTab) {
            WallyTab.HOME -> {
                HomeScreen(
                    todayPick = todayPick,
                    wallpapers = homeWallpapers,
                    selectedCategory = selectedCategory,
                    isLoading = isHomeLoading,
                    onCategorySelected = { cat ->
                        selectedCategory = cat
                        scope.launch { savedStorage.saveLastCategory(cat) }
                    },
                    onCollectionClick = { col ->
                        selectedCategory = col.title.split(" ").firstOrNull() ?: "Minimal"
                    },
                    onWallpaperClick = { item ->
                        selectedWallpaperForPreview = item
                    }
                )
            }
            WallyTab.EXPLORE -> {
                ExploreScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedSource = selectedSourceFilter,
                    onSourceSelected = { selectedSourceFilter = it },
                    results = exploreResults,
                    isLoading = isExploreLoading,
                    onWallpaperClick = { item ->
                        selectedWallpaperForPreview = item
                    }
                )
            }
            WallyTab.SAVED -> {
                SavedScreen(
                    savedWallpapers = savedWallpapers,
                    onWallpaperClick = { item ->
                        selectedWallpaperForPreview = item
                    },
                    onExploreClick = { currentTab = WallyTab.EXPLORE }
                )
            }
            WallyTab.PROFILE -> {
                ProfileScreen(
                    sourcesConfigured = sourcesConfigured
                )
            }
        }

        // Floating Glassmorphic Bottom Navigation
        GlassmorphicBottomBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            onQuickPickClick = {
                // Quick Pick: Pick a random wallpaper from the current list or hero
                val pick = homeWallpapers.shuffled().firstOrNull() ?: todayPick
                selectedWallpaperForPreview = pick
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Full-screen Wallpaper Preview Sheet with single deliberate slide-up motion
        AnimatedVisibility(
            visible = selectedWallpaperForPreview != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 250)
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
        ) {
            val previewItem = selectedWallpaperForPreview
            val isSaved = previewItem?.let { savedIds.contains(it.id) } ?: false

            WallpaperPreviewSheet(
                wallpaper = previewItem,
                isSaved = isSaved,
                wallpaperSetter = wallpaperSetter,
                onToggleSave = { item ->
                    scope.launch { savedStorage.toggleSave(item) }
                },
                onClose = { selectedWallpaperForPreview = null }
            )
        }
    }
}
