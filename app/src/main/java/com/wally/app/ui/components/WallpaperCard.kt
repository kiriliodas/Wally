package com.wally.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wally.app.data.model.WallpaperItem
import com.wally.app.ui.theme.WallyBronzeLight
import com.wally.app.ui.theme.WallyCard
import com.wally.app.ui.theme.WallyHairline
import com.wally.app.ui.theme.WallyMuted
import com.wally.app.ui.theme.WallyText

@Composable
fun WallpaperCard(
    wallpaper: WallpaperItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Aspect ratio between 0.5 (9:18) and 0.65 for staggered visual rhythm
    val ratio = wallpaper.aspectRatio.coerceIn(0.48f, 0.64f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(16.dp))
            .background(WallyCard)
            .border(1.dp, WallyHairline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.previewUrl.ifBlank { wallpaper.fullUrl })
                .crossfade(true)
                .build(),
            contentDescription = "Wallpaper by ${wallpaper.author}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Top source tag
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .border(0.5.dp, WallyHairline, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = wallpaper.source.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = WallyBronzeLight,
                fontSize = 9.sp
            )
        }

        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = wallpaper.author,
                style = MaterialTheme.typography.titleMedium,
                color = WallyText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wallpaper.resolutionFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = WallyMuted,
                    fontSize = 10.sp
                )
                if (wallpaper.likesOrFavorites > 0) {
                    Text(
                        text = "★ ${wallpaper.likesOrFavorites}",
                        style = MaterialTheme.typography.labelSmall,
                        color = WallyBronzeLight,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
