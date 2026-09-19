package com.wally.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wally.app.data.AppContainer
import com.wally.app.data.FallbackCatalog
import com.wally.app.data.WallpaperItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WallyTab { HOME, EXPLORE, SAVED, PROFILE }

data class WallyUiState(
    val selectedTab: WallyTab = WallyTab.HOME,
    val home: List<WallpaperItem> = FallbackCatalog.items,
    val explore: List<WallpaperItem> = FallbackCatalog.items,
    val saved: List<WallpaperItem> = emptyList(),
    val savedIds: Set<String> = emptySet(),
    val lastCategory: String = "All",
    val exploreQuery: String = "",
    val isLoading: Boolean = true,
    val isExploreLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isApplying: Boolean = false,
    val selected: WallpaperItem? = null,
    val appliedId: String? = null,
    val message: String? = null,
)

class WallyViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(WallyUiState())
    val state: StateFlow<WallyUiState> = _state.asStateFlow()

    private val known = LinkedHashMap<String, WallpaperItem>().apply {
        FallbackCatalog.items.forEach { put(it.stableId, it) }
    }
    private var homePage = 1
    private var homeJob: Job? = null
    private var exploreJob: Job? = null

    init {
        viewModelScope.launch {
            container.savedStore.savedIds.collectLatest { ids ->
                _state.update { it.copy(savedIds = ids, saved = known.values.filter { item -> ids.contains(item.stableId) }) }
            }
        }
        viewModelScope.launch {
            container.savedStore.lastCategory.collectLatest { category ->
                _state.update { it.copy(lastCategory = category) }
            }
        }
        loadHome("All")
        searchExplore("")
    }

    fun selectTab(tab: WallyTab) {
        _state.update { it.copy(selectedTab = tab, message = null) }
    }

    fun selectCategory(category: String) {
        viewModelScope.launch { container.savedStore.setLastCategory(category) }
        loadHome(category)
    }

    private fun loadHome(category: String) {
        homeJob?.cancel()
        homeJob = viewModelScope.launch {
            _state.update { it.copy(lastCategory = category, isLoading = true, message = null) }
            val query = category.takeUnless { it == "All" }.orEmpty()
            val result = runCatching { container.repository.page(query, 1) }
                .getOrDefault(emptyList())
                .ifEmpty { FallbackCatalog.items.filterFor(category) }
            homePage = 1
            remember(result)
            _state.update { it.copy(home = result, isLoading = false) }
        }
    }

    fun loadMoreHome() {
        if (_state.value.isLoadingMore || _state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            val query = _state.value.lastCategory.takeUnless { it == "All" }.orEmpty()
            val nextPage = homePage + 1
            val result = runCatching { container.repository.page(query, nextPage) }.getOrDefault(emptyList())
            if (result.isNotEmpty()) {
                homePage = nextPage
                remember(result)
                _state.update { current -> current.copy(home = (current.home + result).distinctBy { it.stableId }) }
            }
            _state.update { it.copy(isLoadingMore = false) }
        }
    }

    fun searchExplore(query: String) {
        _state.update { it.copy(exploreQuery = query) }
        exploreJob?.cancel()
        exploreJob = viewModelScope.launch {
            delay(280)
            _state.update { it.copy(isExploreLoading = true) }
            val result = runCatching { container.repository.page(query.trim(), 1) }
                .getOrDefault(emptyList())
                .ifEmpty { FallbackCatalog.items.filterFor(query) }
            remember(result)
            _state.update { it.copy(explore = result, isExploreLoading = false) }
        }
    }

    fun toggleSaved(item: WallpaperItem) {
        remember(listOf(item))
        viewModelScope.launch { container.savedStore.toggle(item.stableId) }
    }

    fun openPreview(item: WallpaperItem) {
        remember(listOf(item))
        _state.update { it.copy(selected = item, appliedId = null, message = null) }
    }

    fun closePreview() {
        _state.update { it.copy(selected = null, isApplying = false, message = null) }
    }

    fun addUpload(uri: Uri) {
        val item = WallpaperItem(
            id = "upload-${System.currentTimeMillis()}",
            title = "Your new wallpaper",
            imageUrl = uri.toString(),
            width = 1440,
            height = 2560,
            origin = com.wally.app.data.WallpaperOrigin.UPLOAD,
            tags = listOf("Your upload"),
        )
        remember(listOf(item))
        _state.update { it.copy(selected = item, message = "Preview ready") }
    }

    fun applyWallpaper(item: WallpaperItem) {
        if (_state.value.isApplying) return
        viewModelScope.launch {
            _state.update { it.copy(isApplying = true, message = null) }
            val result = container.wallpaperApplier.set(item)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isApplying = false,
                            appliedId = item.stableId,
                            message = "Set for home and lock screen",
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isApplying = false,
                            message = error.message ?: "Could not set this wallpaper",
                        )
                    }
                },
            )
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun remember(items: List<WallpaperItem>) {
        items.forEach { known[it.stableId] = it }
        val ids = _state.value.savedIds
        _state.update { it.copy(saved = known.values.filter { item -> ids.contains(item.stableId) }) }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WallyViewModel(container) as T
    }
}

private fun List<WallpaperItem>.filterFor(filter: String): List<WallpaperItem> {
    if (filter.isBlank() || filter == "All") return this
    return filter { item ->
        item.tags.any { tag -> tag.equals(filter, ignoreCase = true) } ||
            item.title.contains(filter, ignoreCase = true)
    }.ifEmpty { this }
}
