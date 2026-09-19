package com.wally.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.Coil
import com.wally.app.R
import com.wally.app.data.model.WallpaperSourceType
import com.wally.app.ui.theme.WallyBg
import com.wally.app.ui.theme.WallyBronze
import com.wally.app.ui.theme.WallyBronzeLight
import com.wally.app.ui.theme.WallyCard
import com.wally.app.ui.theme.WallyHairline
import com.wally.app.ui.theme.WallyMuted
import com.wally.app.ui.theme.WallyText
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    sourcesConfigured: Map<WallpaperSourceType, Boolean>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WallyBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        Text(
            text = "PROFILE & SETTINGS",
            style = MaterialTheme.typography.displaySmall,
            color = WallyText,
            letterSpacing = 1.5.sp
        )

        // User Avatar Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WallyCard)
                .border(1.dp, WallyHairline, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(WallyBg)
                    .border(2.dp, WallyBronze, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = null,
                    tint = WallyBronzeLight,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "WALLY USER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = WallyText
                )
                Text(
                    text = "Dark Mode Enthusiast",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WallyMuted
                )
            }
        }

        // Cache Management Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WallyCard)
                .border(1.dp, WallyHairline, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "STORAGE & IMAGE CACHE",
                style = MaterialTheme.typography.labelLarge,
                color = WallyBronzeLight,
                letterSpacing = 1.0.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Disk Cache Budget", style = MaterialTheme.typography.bodyMedium, color = WallyMuted)
                Text(text = "150 MB (Capped)", style = MaterialTheme.typography.bodyMedium, color = WallyText)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Memory Cache Budget", style = MaterialTheme.typography.bodyMedium, color = WallyMuted)
                Text(text = "25% of App RAM", style = MaterialTheme.typography.bodyMedium, color = WallyText)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WallyBg)
                    .border(1.dp, WallyHairline, RoundedCornerShape(12.dp))
                    .clickable {
                        scope.launch {
                            val loader = Coil.imageLoader(context)
                            loader.memoryCache?.clear()
                            loader.diskCache?.clear()
                            Toast.makeText(context, "Image cache cleared!", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CLEAR IMAGE CACHE",
                    style = MaterialTheme.typography.labelSmall,
                    color = WallyBronzeLight,
                    letterSpacing = 1.0.sp
                )
            }
        }

        // API Providers Integration Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WallyCard)
                .border(1.dp, WallyHairline, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "WALLPAPER API SOURCES",
                style = MaterialTheme.typography.labelLarge,
                color = WallyBronzeLight,
                letterSpacing = 1.0.sp
            )

            WallpaperSourceType.values().forEach { source ->
                val isReady = sourcesConfigured[source] ?: false
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = source.displayName, style = MaterialTheme.typography.titleMedium, color = WallyText)
                        Text(
                            text = if (source == WallpaperSourceType.WALLHAVEN) "Primary (SFW public toplist ready)"
                            else "Free tier endpoint",
                            style = MaterialTheme.typography.labelSmall,
                            color = WallyMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isReady) WallyBronze.copy(alpha = 0.2f) else WallyBg)
                            .border(1.dp, if (isReady) WallyBronzeLight else WallyHairline, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isReady) "READY" else "KEY NEEDED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isReady) WallyBronzeLight else WallyMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Quality Gate Floor Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WallyCard)
                .border(1.dp, WallyHairline, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "QUALITY GATE SPECIFICATIONS",
                style = MaterialTheme.typography.labelLarge,
                color = WallyBronzeLight,
                letterSpacing = 1.0.sp
            )

            Text(
                text = "• Min Resolution: 2560px long edge (Strict floor, no upscaling)\n" +
                       "• Aspect Ratios: 9:16 to 9:20.5 (Mobile portrait screens only)\n" +
                       "• Deduplication: Perceptual visual fingerprint comparison\n" +
                       "• Purity: SFW only with engagement floor enforcement",
                style = MaterialTheme.typography.bodyMedium,
                color = WallyMuted,
                lineHeight = 20.sp
            )
        }

        // About & Version
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WALLY v1.0.0 • MINIMAL DARK WALLPAPERS",
                style = MaterialTheme.typography.labelSmall,
                color = WallyMuted,
                letterSpacing = 1.2.sp
            )
        }
    }
}
