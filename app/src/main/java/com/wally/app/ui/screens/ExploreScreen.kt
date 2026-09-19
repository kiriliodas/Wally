package com.wally.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wally.app.R
import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import com.wally.app.ui.components.WallpaperCard
import com.wally.app.ui.theme.WallyBg
import com.wally.app.ui.theme.WallyBronze
import com.wally.app.ui.theme.WallyBronzeLight
import com.wally.app.ui.theme.WallyCard
import com.wally.app.ui.theme.WallyHairline
import com.wally.app.ui.theme.WallyMuted
import com.wally.app.ui.theme.WallyText

@Composable
fun ExploreScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedSource: WallpaperSourceType?,
    onSourceSelected: (WallpaperSourceType?) -> Unit,
    results: List<WallpaperItem>,
    isLoading: Boolean,
    onWallpaperClick: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WallyBg)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "EXPLORE",
                style = MaterialTheme.typography.displaySmall,
                color = WallyText,
                letterSpacing = 1.5.sp
            )

            // Search Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WallyCard)
                    .border(1.dp, WallyHairline, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_explore),
                        contentDescription = "Search",
                        tint = WallyMuted,
                        modifier = Modifier.size(20.dp)
                    )

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(WallyBronzeLight),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = WallyText),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search high-res dark wallpapers...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = WallyMuted
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Source Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All sources chip
                SourceFilterChip(
                    title = "All Sources",
                    isSelected = selectedSource == null,
                    onClick = { onSourceSelected(null) }
                )

                WallpaperSourceType.values().forEach { source ->
                    SourceFilterChip(
                        title = source.displayName,
                        isSelected = selectedSource == source,
                        onClick = { onSourceSelected(source) }
                    )
                }
            }
        }

        // Filtered Results Masonry Grid
        val filteredResults = remember(results, selectedSource) {
            if (selectedSource == null) results
            else results.filter { it.source == selectedSource }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            if (isLoading) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = WallyBronzeLight)
                    }
                }
            } else if (filteredResults.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptySearchPlaceholder(
                        onTagClick = { tag -> onSearchQueryChange(tag) }
                    )
                }
            } else {
                items(filteredResults, key = { "${it.source.name}_${it.id}" }) { item ->
                    WallpaperCard(
                        wallpaper = item,
                        onClick = { onWallpaperClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) WallyBronze.copy(alpha = 0.18f) else WallyCard)
            .border(
                1.dp,
                if (isSelected) WallyBronzeLight else WallyHairline,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) WallyBronzeLight else WallyMuted
        )
    }
}

@Composable
private fun EmptySearchPlaceholder(onTagClick: (String) -> Unit) {
    val suggestions = listOf("Minimal", "Obsidian", "Cyberpunk", "Tokyo", "Space", "Anime", "Amoled")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DISCOVER TRENDING THEMES",
            style = MaterialTheme.typography.labelLarge,
            color = WallyBronzeLight,
            letterSpacing = 1.2.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.take(4).forEach { tag ->
                        TagButton(tag = tag, onClick = { onTagClick(tag) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.drop(4).forEach { tag ->
                        TagButton(tag = tag, onClick = { onTagClick(tag) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TagButton(tag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WallyCard)
            .border(1.dp, WallyHairline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = "#$tag", style = MaterialTheme.typography.bodyMedium, color = WallyText)
    }
}
