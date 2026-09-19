package com.wally.app.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wally.app.R
import com.wally.app.data.model.WallpaperItem
import com.wally.app.domain.wallpaper.WallpaperSetter
import com.wally.app.domain.wallpaper.WallpaperTarget
import com.wally.app.ui.theme.WallyBg
import com.wally.app.ui.theme.WallyBronze
import com.wally.app.ui.theme.WallyBronzeLight
import com.wally.app.ui.theme.WallyCard
import com.wally.app.ui.theme.WallyGlassBg
import com.wally.app.ui.theme.WallyGlassBorder
import com.wally.app.ui.theme.WallyHairline
import com.wally.app.ui.theme.WallyMuted
import com.wally.app.ui.theme.WallyText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WallpaperPreviewSheet(
    wallpaper: WallpaperItem?,
    isSaved: Boolean,
    wallpaperSetter: WallpaperSetter,
    onToggleSave: (WallpaperItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (wallpaper == null) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showTargetDialog by remember { mutableStateOf(false) }
    var isSettingWallpaper by remember { mutableStateOf(false) }
    var setSuccess by remember { mutableStateOf(false) }

    val saveIconTint by animateColorAsState(
        targetValue = if (isSaved) WallyBronzeLight else WallyText,
        animationSpec = tween(durationMillis = 250),
        label = "save_icon_tint"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Full resolution image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(wallpaper.fullUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Full Wallpaper Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top bar scrim & controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(WallyGlassBg)
                        .border(1.dp, WallyGlassBorder, CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = WallyText, fontSize = 16.sp)
                }

                // Source indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(WallyGlassBg)
                        .border(1.dp, WallyGlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${wallpaper.source.displayName.uppercase()} • ${wallpaper.resolutionFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = WallyBronzeLight,
                        letterSpacing = 1.0.sp
                    )
                }
            }
        }

        // Bottom Actions & Details Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Author & Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "PHOTOGRAPHY / ART",
                            style = MaterialTheme.typography.labelSmall,
                            color = WallyBronzeLight,
                            letterSpacing = 1.0.sp
                        )
                        Text(
                            text = wallpaper.author,
                            style = MaterialTheme.typography.titleLarge,
                            color = WallyText
                        )
                    }

                    if (wallpaper.likesOrFavorites > 0) {
                        Text(
                            text = "♥ ${wallpaper.likesOrFavorites}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WallyMuted
                        )
                    }
                }

                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Set Wallpaper Primary Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(WallyBronze, WallyBronzeLight)
                                )
                            )
                            .clickable(enabled = !isSettingWallpaper) {
                                showTargetDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSettingWallpaper) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = WallyBg,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "APPLYING...",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = WallyBg
                                )
                            } else if (setSuccess) {
                                Text(
                                    text = "WALLPAPER APPLIED ✓",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = WallyBg
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_set_wallpaper),
                                    contentDescription = null,
                                    tint = WallyBg,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "SET WALLPAPER",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = WallyBg
                                )
                            }
                        }
                    }

                    // Save Bookmark Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(WallyGlassBg)
                            .border(1.dp, WallyGlassBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                onToggleSave(wallpaper)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bookmark),
                            contentDescription = "Save Wallpaper",
                            tint = saveIconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Screen Target Selection Dialog
        if (showTargetDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { showTargetDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(WallyCard)
                        .border(1.dp, WallyHairline, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "APPLY WALLPAPER TO",
                        style = MaterialTheme.typography.labelLarge,
                        color = WallyBronzeLight,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TargetOptionItem(title = "Home Screen") {
                        showTargetDialog = false
                        isSettingWallpaper = true
                        scope.launch {
                            val res = wallpaperSetter.applyWallpaper(
                                context,
                                wallpaper.fullUrl,
                                WallpaperTarget.HOME
                            )
                            isSettingWallpaper = false
                            if (res.isSuccess) {
                                setSuccess = true
                                Toast.makeText(context, "Home wallpaper set!", Toast.LENGTH_SHORT).show()
                                delay(2000)
                                setSuccess = false
                            } else {
                                Toast.makeText(context, "Error setting wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    TargetOptionItem(title = "Lock Screen") {
                        showTargetDialog = false
                        isSettingWallpaper = true
                        scope.launch {
                            val res = wallpaperSetter.applyWallpaper(
                                context,
                                wallpaper.fullUrl,
                                WallpaperTarget.LOCK
                            )
                            isSettingWallpaper = false
                            if (res.isSuccess) {
                                setSuccess = true
                                Toast.makeText(context, "Lock wallpaper set!", Toast.LENGTH_SHORT).show()
                                delay(2000)
                                setSuccess = false
                            } else {
                                Toast.makeText(context, "Error setting wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    TargetOptionItem(title = "Both Screens") {
                        showTargetDialog = false
                        isSettingWallpaper = true
                        scope.launch {
                            val res = wallpaperSetter.applyWallpaper(
                                context,
                                wallpaper.fullUrl,
                                WallpaperTarget.BOTH
                            )
                            isSettingWallpaper = false
                            if (res.isSuccess) {
                                setSuccess = true
                                Toast.makeText(context, "Both wallpapers set!", Toast.LENGTH_SHORT).show()
                                delay(2000)
                                setSuccess = false
                            } else {
                                Toast.makeText(context, "Error setting wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetOptionItem(title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WallyBg)
            .border(1.dp, WallyHairline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = WallyText
        )
    }
}
