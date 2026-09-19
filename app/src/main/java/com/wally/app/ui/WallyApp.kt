package com.wally.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.wally.app.data.WallpaperItem
import com.wally.app.ui.theme.WallyPalette
import kotlinx.coroutines.delay

private val categories = listOf("All", "Abstract", "Nature", "Architecture", "Minimal", "Dark")

@Composable
fun WallyApp(viewModel: WallyViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val uploadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::addUpload)
    }

    BackHandler(enabled = state.selected != null) { viewModel.closePreview() }
    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(3_200)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WallyPalette.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        when (state.selectedTab) {
            WallyTab.HOME -> HomeScreen(
                state = state,
                onCategory = viewModel::selectCategory,
                onOpen = viewModel::openPreview,
                onSave = viewModel::toggleSaved,
                onLoadMore = viewModel::loadMoreHome,
            )
            WallyTab.EXPLORE -> ExploreScreen(
                state = state,
                onQuery = viewModel::searchExplore,
                onOpen = viewModel::openPreview,
                onSave = viewModel::toggleSaved,
            )
            WallyTab.SAVED -> SavedScreen(
                state = state,
                onOpen = viewModel::openPreview,
                onSave = viewModel::toggleSaved,
            )
            WallyTab.PROFILE -> ProfileScreen(state = state)
        }

        if (state.selected == null) {
            BottomBar(
                selectedTab = state.selectedTab,
                onTab = viewModel::selectTab,
                onUpload = { uploadLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        AnimatedVisibility(
            visible = state.selected != null,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            state.selected?.let { item ->
                PreviewSheet(
                    item = item,
                    isSaved = state.savedIds.contains(item.stableId),
                    isApplying = state.isApplying,
                    isApplied = state.appliedId == item.stableId,
                    onClose = viewModel::closePreview,
                    onSave = { viewModel.toggleSaved(item) },
                    onSet = { viewModel.applyWallpaper(item) },
                )
            }
        }

        state.message?.let { message ->
            if (state.selected == null) {
                MessagePill(message = message, modifier = Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

@Composable
private fun HomeScreen(
    state: WallyUiState,
    onCategory: (String) -> Unit,
    onOpen: (WallpaperItem) -> Unit,
    onSave: (WallpaperItem) -> Unit,
    onLoadMore: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val hero = state.home.firstOrNull()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 112.dp),
    ) {
        TopHeader(
            eyebrow = "A quieter screen",
            onProfile = null,
        )
        SectionEyebrow(text = "TODAY'S PICK", modifier = Modifier.padding(top = 24.dp))
        if (hero != null) {
            HeroCard(
                item = hero,
                isSaved = state.savedIds.contains(hero.stableId),
                onOpen = { onOpen(hero) },
                onSave = { onSave(hero) },
            )
        } else {
            LoadingCard(modifier = Modifier.padding(top = 10.dp))
        }

        SectionHeader(
            title = "Find your atmosphere",
            action = "",
            modifier = Modifier.padding(top = 28.dp),
        )
        CategoryRow(
            selected = state.lastCategory,
            onSelected = onCategory,
            modifier = Modifier.padding(top = 12.dp),
        )

        SectionHeader(
            title = "Collections",
            action = "View all",
            modifier = Modifier.padding(top = 28.dp),
        )
        CollectionsRow(modifier = Modifier.padding(top = 12.dp))

        SectionHeader(
            title = "Made for you",
            action = if (state.isLoadingMore) "Loading…" else "Refresh",
            onAction = onLoadMore,
            modifier = Modifier.padding(top = 30.dp),
        )
        if (state.isLoading && state.home.isEmpty()) {
            LoadingCard(modifier = Modifier.padding(top = 12.dp))
        } else {
            MasonryGrid(
                items = state.home.drop(1),
                savedIds = state.savedIds,
                onOpen = onOpen,
                onSave = onSave,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Text(
            text = "Quality filtered · portrait only · 1440 × 2560 minimum",
            style = MaterialTheme.typography.labelLarge,
            color = WallyPalette.muted.copy(alpha = .68f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ExploreScreen(
    state: WallyUiState,
    onQuery: (String) -> Unit,
    onOpen: (WallpaperItem) -> Unit,
    onSave: (WallpaperItem) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 112.dp),
    ) {
        TopHeader(eyebrow = "Curated from the open web", onProfile = null)
        Text(
            text = "Explore",
            style = MaterialTheme.typography.displayMedium,
            color = WallyPalette.text,
            modifier = Modifier.padding(top = 26.dp),
        )
        Text(
            text = "A little color for the space around you.",
            style = MaterialTheme.typography.bodyLarge,
            color = WallyPalette.muted,
            modifier = Modifier.padding(top = 8.dp),
        )
        ExploreSearch(
            query = state.exploreQuery,
            onQuery = onQuery,
            modifier = Modifier.padding(top = 22.dp),
        )
        CategoryRow(
            selected = state.exploreQuery.ifBlank { "All" },
            onSelected = { category -> onQuery(if (category == "All") "" else category) },
            modifier = Modifier.padding(top = 14.dp),
        )
        SectionHeader(
            title = if (state.exploreQuery.isBlank()) "Fresh finds" else "Results for “${state.exploreQuery}”",
            action = if (state.isExploreLoading) "Loading…" else "",
            modifier = Modifier.padding(top = 30.dp),
        )
        if (state.isExploreLoading && state.explore.isEmpty()) {
            LoadingCard(modifier = Modifier.padding(top = 12.dp))
        } else {
            MasonryGrid(
                items = state.explore,
                savedIds = state.savedIds,
                onOpen = onOpen,
                onSave = onSave,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun SavedScreen(
    state: WallyUiState,
    onOpen: (WallpaperItem) -> Unit,
    onSave: (WallpaperItem) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 112.dp),
    ) {
        TopHeader(eyebrow = "Kept for later", onProfile = null)
        Text(
            text = "Saved",
            style = MaterialTheme.typography.displayMedium,
            color = WallyPalette.text,
            modifier = Modifier.padding(top = 26.dp),
        )
        Text(
            text = if (state.saved.isEmpty()) "Your personal wall, waiting for a first favorite." else "${state.saved.size} quiet corners",
            style = MaterialTheme.typography.bodyLarge,
            color = WallyPalette.muted,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (state.saved.isEmpty()) {
            EmptyState(modifier = Modifier.padding(top = 60.dp))
        } else {
            MasonryGrid(
                items = state.saved,
                savedIds = state.savedIds,
                onOpen = onOpen,
                onSave = onSave,
                modifier = Modifier.padding(top = 28.dp),
            )
        }
    }
}

@Composable
private fun ProfileScreen(state: WallyUiState) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 112.dp),
    ) {
        TopHeader(eyebrow = "A space that is yours", onProfile = null)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 34.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(WallyPalette.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("W", color = WallyPalette.background, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text("your space", style = MaterialTheme.typography.headlineSmall, color = WallyPalette.text)
                Text("A thoughtful screen starts here.", style = MaterialTheme.typography.bodyMedium, color = WallyPalette.muted)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 30.dp),
        ) {
            ProfileStat(value = state.savedIds.size.toString(), label = "SAVED", modifier = Modifier.weight(1f))
            ProfileStat(value = "4", label = "SOURCES", modifier = Modifier.weight(1f))
            ProfileStat(value = "∞", label = "MOODS", modifier = Modifier.weight(1f))
        }
        ProfileCard(
            title = "Your taste, not a feed",
            body = "Wally asks each source for portrait-first images, then keeps only the sharp, considered ones.",
            modifier = Modifier.padding(top = 28.dp),
        )
        ProfileCard(
            title = "Private by default",
            body = "Saved wallpaper IDs and your last category stay on this device. API keys are never shipped in source.",
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "WALLY  /  1.0",
            style = MaterialTheme.typography.labelLarge,
            color = WallyPalette.muted,
            modifier = Modifier.padding(top = 30.dp),
        )
    }
}

@Composable
private fun TopHeader(eyebrow: String, onProfile: (() -> Unit)?) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WallyPalette.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("W", color = WallyPalette.background, fontSize = 17.sp, fontWeight = FontWeight.Black)
            }
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = "WALLY",
                    style = MaterialTheme.typography.labelLarge,
                    color = WallyPalette.text,
                    letterSpacing = 2.sp,
                )
                Text(text = eyebrow, style = MaterialTheme.typography.bodyMedium, color = WallyPalette.muted)
            }
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(WallyPalette.card)
                .border(1.dp, WallyPalette.hairline, CircleShape)
                .clickable(enabled = onProfile != null) { onProfile?.invoke() },
            contentAlignment = Alignment.Center,
        ) {
            Text("A", style = MaterialTheme.typography.labelLarge, color = WallyPalette.accentLight)
        }
    }
}

@Composable
private fun SectionEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = WallyPalette.accentLight,
        modifier = modifier,
        letterSpacing = 1.8.sp,
    )
}

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = WallyPalette.text)
        if (action.isNotBlank()) {
            TextButton(onClick = { onAction?.invoke() }, contentPadding = PaddingValues(0.dp)) {
                Text(action, style = MaterialTheme.typography.labelLarge, color = WallyPalette.accentLight)
            }
        }
    }
}

@Composable
private fun CategoryRow(selected: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        categories.forEach { category ->
            CategoryChip(category = category, selected = category.equals(selected, ignoreCase = true)) {
                onSelected(category)
            }
        }
    }
}

@Composable
private fun CategoryChip(category: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) WallyPalette.accent else Color.Transparent
    val foreground = if (selected) WallyPalette.background else WallyPalette.muted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .border(1.dp, if (selected) WallyPalette.accent else WallyPalette.hairline, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(category, style = MaterialTheme.typography.labelLarge, color = foreground)
    }
}

@Composable
private fun HeroCard(item: WallpaperItem, isSaved: Boolean, onOpen: () -> Unit, onSave: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(328.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(WallyPalette.card)
            .clickable(onClick = onOpen),
    ) {
        WallpaperArtwork(item = item, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = .82f)),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = .45f))
                .border(1.dp, Color.White.copy(alpha = .18f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Text("WALLY CURATED", style = MaterialTheme.typography.labelLarge, color = WallyPalette.text)
        }
        IconButton(onClick = onSave, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
            SaveIcon(isSaved = isSaved, tint = WallyPalette.text)
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(18.dp)) {
            Text(item.title, style = MaterialTheme.typography.headlineSmall, color = WallyPalette.text)
            Text(
                "A clear mind begins with a clear frame",
                style = MaterialTheme.typography.bodyMedium,
                color = WallyPalette.text.copy(alpha = .72f),
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun CollectionsRow(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        CollectionCard("QUIET\nMORNINGS", "12 pieces", listOf(Color(0xFF7A5D43), Color(0xFF211C17)))
        CollectionCard("DEEP\nFOCUS", "18 pieces", listOf(Color(0xFF344044), Color(0xFF101517)))
        CollectionCard("SOFT\nGEOMETRY", "09 pieces", listOf(Color(0xFF9A6E3F), Color(0xFF332117)))
    }
}

@Composable
private fun CollectionCard(title: String, count: String, colors: List<Color>) {
    Column(
        modifier = Modifier
            .width(152.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors))
            .border(1.dp, Color.White.copy(alpha = .10f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = WallyPalette.text, lineHeight = 14.sp)
        Text(count, style = MaterialTheme.typography.bodyMedium, color = WallyPalette.text.copy(alpha = .65f))
    }
}

@Composable
private fun MasonryGrid(
    items: List<WallpaperItem>,
    savedIds: Set<String>,
    onOpen: (WallpaperItem) -> Unit,
    onSave: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEachIndexed { columnIndex, item ->
                    val index = rowIndex * 2 + columnIndex
                    WallpaperCard(
                        item = item,
                        isSaved = savedIds.contains(item.stableId),
                        height = if (index % 3 == 1) 224.dp else 184.dp,
                        onOpen = { onOpen(item) },
                        onSave = { onSave(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WallpaperCard(
    item: WallpaperItem,
    isSaved: Boolean,
    height: androidx.compose.ui.unit.Dp,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(WallyPalette.card)
            .clickable(onClick = onOpen),
    ) {
        WallpaperArtwork(item = item, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .88f)))),
        )
        IconButton(onClick = onSave, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
            SaveIcon(isSaved = isSaved, tint = WallyPalette.text)
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(13.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp),
                color = WallyPalette.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(item.origin.label.uppercase(), style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp), color = WallyPalette.text.copy(alpha = .56f))
        }
    }
}

@Composable
private fun WallpaperArtwork(item: WallpaperItem, modifier: Modifier) {
    Box(modifier = modifier.background(Brush.linearGradient(listOf(Color(0xFF362B23), Color(0xFF111416))))) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SaveIcon(isSaved: Boolean, tint: Color) {
    val animatedTint by animateColorAsState(
        targetValue = if (isSaved) WallyPalette.accentLight else tint,
        label = "save tint",
    )
    Icon(
        imageVector = if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
        contentDescription = if (isSaved) "Remove from saved" else "Save wallpaper",
        tint = animatedTint,
    )
}

@Composable
private fun ExploreSearch(query: String, onQuery: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Search a mood, place, or color", color = WallyPalette.muted) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = WallyPalette.muted) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQuery("") }) { Icon(Icons.Outlined.Close, contentDescription = "Clear search") }
            } else {
                Icon(Icons.Outlined.Tune, contentDescription = "Search filters", tint = WallyPalette.muted)
            }
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WallyPalette.accent,
            unfocusedBorderColor = WallyPalette.hairline,
            focusedContainerColor = WallyPalette.card,
            unfocusedContainerColor = WallyPalette.card,
            cursorColor = WallyPalette.accentLight,
            focusedTextColor = WallyPalette.text,
            unfocusedTextColor = WallyPalette.text,
        ),
    )
}

@Composable
private fun BottomBar(
    selectedTab: WallyTab,
    onTab: (WallyTab) -> Unit,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .shadow(18.dp, RoundedCornerShape(30.dp), ambientColor = Color.Black.copy(alpha = .6f))
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xEA1A1918))
            .border(1.dp, Color.White.copy(alpha = .12f), RoundedCornerShape(30.dp))
            .padding(horizontal = 7.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth(),
        ) {
            NavItem(WallyTab.HOME, selectedTab, Icons.Outlined.Home, "Home", onTab)
            NavItem(WallyTab.EXPLORE, selectedTab, Icons.Outlined.Explore, "Explore", onTab)
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(WallyPalette.accent)
                    .clickable(onClick = onUpload),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Upload a wallpaper", tint = WallyPalette.background, modifier = Modifier.size(27.dp))
            }
            NavItem(WallyTab.SAVED, selectedTab, Icons.Outlined.BookmarkBorder, "Saved", onTab)
            NavItem(WallyTab.PROFILE, selectedTab, Icons.Outlined.PersonOutline, "Profile", onTab)
        }
    }
}

@Composable
private fun NavItem(
    tab: WallyTab,
    selectedTab: WallyTab,
    icon: ImageVector,
    label: String,
    onTab: (WallyTab) -> Unit,
) {
    val selected = tab == selectedTab
    val tint = if (selected) WallyPalette.accentLight else WallyPalette.muted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onTab(tab) }
            .padding(vertical = 4.dp),
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(21.dp))
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp), color = tint, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun PreviewSheet(
    item: WallpaperItem,
    isSaved: Boolean,
    isApplying: Boolean,
    isApplied: Boolean,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onSet: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF50A0A0A)),
    ) {
        WallpaperArtwork(item = item, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = .62f), Color.Transparent, Color(0xFF0A0A0A)),
                    ),
                ),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Close preview", tint = WallyPalette.text)
            }
            Text("PREVIEW", style = MaterialTheme.typography.labelLarge, color = WallyPalette.text, letterSpacing = 2.sp)
            IconButton(onClick = onSave) { SaveIcon(isSaved = isSaved, tint = WallyPalette.text) }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Text(item.origin.label.uppercase(), style = MaterialTheme.typography.labelLarge, color = WallyPalette.accentLight, letterSpacing = 1.6.sp)
            Text(item.title, style = MaterialTheme.typography.displayMedium, color = WallyPalette.text, modifier = Modifier.padding(top = 4.dp))
            Text(
                "${item.width} × ${item.height}  ·  ${if (isSaved) "saved to your wall" else "ready to keep"}",
                style = MaterialTheme.typography.bodyMedium,
                color = WallyPalette.text.copy(alpha = .70f),
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 20.dp)) {
                Button(
                    onClick = onSet,
                    enabled = !isApplying,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApplied) WallyPalette.text else WallyPalette.accent,
                        contentColor = if (isApplied) WallyPalette.background else WallyPalette.background,
                        disabledContainerColor = WallyPalette.accent.copy(alpha = .55f),
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(54.dp),
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(color = WallyPalette.background, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        AnimatedContent(targetState = isApplied, label = "wallpaper set state") { applied ->
                            if (applied) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("  Set", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Set wallpaper", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = onSave,
                    border = BorderStroke(1.dp, WallyPalette.text.copy(alpha = .45f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WallyPalette.text),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(54.dp).width(112.dp),
                ) {
                    Icon(if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (isSaved) "Saved" else "Save", modifier = Modifier.padding(start = 5.dp))
                }
            }
            TextButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Close preview", color = WallyPalette.muted)
            }
        }
    }
}

@Composable
private fun LoadingCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(WallyPalette.card, WallyPalette.raised))),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = WallyPalette.accent, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(WallyPalette.card)
                .border(1.dp, WallyPalette.hairline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = WallyPalette.accentLight, modifier = Modifier.size(28.dp))
        }
        Text("Keep the good ones close", style = MaterialTheme.typography.titleLarge, color = WallyPalette.text, modifier = Modifier.padding(top = 18.dp))
        Text("Tap the bookmark on any wallpaper to make it yours.", style = MaterialTheme.typography.bodyMedium, color = WallyPalette.muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(17.dp))
            .background(WallyPalette.card)
            .border(1.dp, WallyPalette.hairline, RoundedCornerShape(17.dp))
            .padding(vertical = 15.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = WallyPalette.accentLight)
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp), color = WallyPalette.muted, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun ProfileCard(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WallyPalette.card)
            .border(1.dp, WallyPalette.hairline, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = WallyPalette.text)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = WallyPalette.muted, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun MessagePill(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(bottom = 100.dp)
            .clip(RoundedCornerShape(50))
            .background(WallyPalette.raised)
            .border(1.dp, WallyPalette.hairline, RoundedCornerShape(50))
            .padding(horizontal = 17.dp, vertical = 11.dp),
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = WallyPalette.text)
    }
}
