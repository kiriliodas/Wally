package com.wally.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.wally.app.data.model.WallpaperItem
import com.wally.app.ui.theme.WallyBg
import com.wally.app.ui.theme.WallyBronze
import com.wally.app.ui.theme.WallyBronzeLight
import com.wally.app.ui.theme.WallyCard
import com.wally.app.ui.theme.WallyHairline
import com.wally.app.ui.theme.WallyMuted
import com.wally.app.ui.theme.WallyText

@Composable
fun TodayPickHero(
    wallpaper: WallpaperItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WallyCard)
            .border(1.dp, WallyHairline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        // Wallpaper Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.previewUrl.ifBlank { wallpaper.fullUrl })
                .crossfade(true)
                .build(),
            contentDescription = "Today's Pick",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.88f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Top Badge: "TODAY'S PICK"
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(12.dp))
                .background(WallyBg.copy(alpha = 0.85f))
                .border(1.dp, WallyBronze.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(WallyBronzeLight)
                )
                Text(
                    text = "TODAY'S PICK",
                    style = MaterialTheme.typography.labelSmall,
                    color = WallyBronzeLight,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // Bottom Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = wallpaper.tags.firstOrNull()?.uppercase() ?: "DARK MINIMAL",
                        style = MaterialTheme.typography.headlineMedium,
                        color = WallyText
                    )
                    Text(
                        text = "by ${wallpaper.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WallyMuted
                    )
                }

                // Source tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WallyCard.copy(alpha = 0.9f))
                        .border(1.dp, WallyHairline, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = wallpaper.source.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = WallyBronzeLight
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${wallpaper.resolutionFormatted} • UHD",
                    style = MaterialTheme.typography.labelSmall,
                    color = WallyMuted
                )

                // Quick preview indicator
                Text(
                    text = "TAP TO PREVIEW →",
                    style = MaterialTheme.typography.labelSmall,
                    color = WallyBronzeLight
                )
            }
        }
    }
}
