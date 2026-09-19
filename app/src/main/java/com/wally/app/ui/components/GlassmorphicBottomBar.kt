package com.wally.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wally.app.R
import com.wally.app.ui.theme.WallyBg
import com.wally.app.ui.theme.WallyBronze
import com.wally.app.ui.theme.WallyBronzeLight
import com.wally.app.ui.theme.WallyGlassBg
import com.wally.app.ui.theme.WallyGlassBorder
import com.wally.app.ui.theme.WallyMuted

enum class WallyTab(val title: String, val iconRes: Int) {
    HOME("Home", R.drawable.ic_home),
    EXPLORE("Explore", R.drawable.ic_explore),
    SAVED("Saved", R.drawable.ic_bookmark),
    PROFILE("Profile", R.drawable.ic_profile)
}

@Composable
fun GlassmorphicBottomBar(
    currentTab: WallyTab,
    onTabSelected: (WallyTab) -> Unit,
    onQuickPickClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glassmorphic pill container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(32.dp))
                .background(WallyGlassBg)
                .border(1.dp, WallyGlassBorder, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home
                NavTabItem(
                    tab = WallyTab.HOME,
                    isSelected = currentTab == WallyTab.HOME,
                    onClick = { onTabSelected(WallyTab.HOME) }
                )

                // Tab 2: Explore
                NavTabItem(
                    tab = WallyTab.EXPLORE,
                    isSelected = currentTab == WallyTab.EXPLORE,
                    onClick = { onTabSelected(WallyTab.EXPLORE) }
                )

                // Center Action Button: Quick Pick / Dice
                CenterActionButton(onClick = onQuickPickClick)

                // Tab 3: Saved
                NavTabItem(
                    tab = WallyTab.SAVED,
                    isSelected = currentTab == WallyTab.SAVED,
                    onClick = { onTabSelected(WallyTab.SAVED) }
                )

                // Tab 4: Profile
                NavTabItem(
                    tab = WallyTab.PROFILE,
                    isSelected = currentTab == WallyTab.PROFILE,
                    onClick = { onTabSelected(WallyTab.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    tab: WallyTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (isSelected) WallyBronzeLight else WallyMuted,
        animationSpec = tween(durationMillis = 200),
        label = "tab_tint"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = tab.iconRes),
            contentDescription = tab.title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun CenterActionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(8.dp, CircleShape, spotColor = WallyBronze)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(WallyBronzeLight, WallyBronze)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_dice),
            contentDescription = "Quick Pick",
            tint = WallyBg,
            modifier = Modifier.size(20.dp)
        )
    }
}
