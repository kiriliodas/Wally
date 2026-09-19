package com.wally.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wally.app.ui.theme.WallyBronzeLight
import com.wally.app.ui.theme.WallyCard
import com.wally.app.ui.theme.WallyHairline
import com.wally.app.ui.theme.WallyMuted
import com.wally.app.ui.theme.WallyText

data class CollectionItem(
    val id: String,
    val title: String,
    val description: String,
    val previewUrl: String,
    val count: Int
)

val CURATED_COLLECTIONS = listOf(
    CollectionItem(
        id = "col_obsidian",
        title = "Obsidian Dark",
        description = "Deep OLED pitch blacks",
        previewUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500",
        count = 24
    ),
    CollectionItem(
        id = "col_neon",
        title = "Neon Horizon",
        description = "Dark cyberpunk silhouettes",
        previewUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500",
        count = 18
    ),
    CollectionItem(
        id = "col_minimal",
        title = "Minimalist Voids",
        description = "Pure negative space",
        previewUrl = "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?w=500",
        count = 32
    ),
    CollectionItem(
        id = "col_cosmic",
        title = "Cosmic Drift",
        description = "Interstellar deep horizons",
        previewUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500",
        count = 15
    )
)

@Composable
fun CollectionsRow(
    collections: List<CollectionItem> = CURATED_COLLECTIONS,
    onCollectionClick: (CollectionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CURATED COLLECTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = WallyMuted,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "EXPLORE ALL →",
                style = MaterialTheme.typography.labelSmall,
                color = WallyBronzeLight,
                letterSpacing = 1.0.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            collections.forEach { item ->
                CollectionCard(item = item, onClick = { onCollectionClick(item) })
            }
        }
    }
}

@Composable
private fun CollectionCard(
    item: CollectionItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(WallyCard)
            .border(1.dp, WallyHairline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.previewUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = WallyText
            )
            Text(
                text = "${item.count} WALLPAPERS",
                style = MaterialTheme.typography.labelSmall,
                color = WallyBronzeLight
            )
        }
    }
}
