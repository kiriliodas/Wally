package com.wally.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wally.app.data.model.WallpaperItem
import com.wally.app.ui.components.CategoryChips
import com.wally.app.ui.components.CollectionItem
import com.wally.app.ui.components.CollectionsRow
import com.wally.app.ui.components.TodayPickHero
import com.wally.app.ui.components.WallpaperCard
import com.wally.app.ui.theme.WallyBg
import com.wally.app.ui.theme.WallyBronze
import com.wally.app.ui.theme.WallyBronzeLight
import com.wally.app.ui.theme.WallyMuted
import com.wally.app.ui.theme.WallyText

@Composable
fun HomeScreen(
    todayPick: WallpaperItem,
    wallpapers: List<WallpaperItem>,
    selectedCategory: String,
    isLoading: Boolean,
    onCategorySelected: (String) -> Unit,
    onCollectionClick: (CollectionItem) -> Unit,
    onWallpaperClick: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WallyBg)
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "WALLY",
                        style = MaterialTheme.typography.displaySmall,
                        color = WallyText,
                        letterSpacing = 2.0.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(WallyBronze)
                    )
                }

                Text(
                    text = "DARK EDITION",
                    style = MaterialTheme.typography.labelSmall,
                    color = WallyMuted,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // Staggered Masonry Grid Content
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            // 1. Hero Pick (Full Width Span)
            item(span = StaggeredGridItemSpan.FullLine) {
                TodayPickHero(
                    wallpaper = todayPick,
                    onClick = { onWallpaperClick(todayPick) },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // 2. Category Chips Row (Full Width Span)
            item(span = StaggeredGridItemSpan.FullLine) {
                CategoryChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // 3. Collections Row Carousel (Full Width Span)
            item(span = StaggeredGridItemSpan.FullLine) {
                CollectionsRow(
                    onCollectionClick = onCollectionClick,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            // 4. Section Heading: Feed
            item(span = StaggeredGridItemSpan.FullLine) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedCategory.uppercase()} FEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = WallyMuted,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "${wallpapers.size} WALLPAPERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = WallyBronzeLight,
                        letterSpacing = 1.0.sp
                    )
                }
            }

            // 5. Masonry Grid Wallpapers
            if (isLoading && wallpapers.isEmpty()) {
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
            } else {
                items(wallpapers, key = { "${it.source.name}_${it.id}" }) { item ->
                    WallpaperCard(
                        wallpaper = item,
                        onClick = { onWallpaperClick(item) }
                    )
                }
            }
        }
    }
}
