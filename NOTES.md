# Wally — Architecture, Sourcing & Build Documentation

## 1. Executive Summary & Deliverables

**Wally** is a minimalist dark-mode Android wallpaper app built with Kotlin, Jetpack Compose, Material 3, and modern Android architecture. It features a curated multi-source aggregation engine (Wallhaven, Unsplash, Pexels, Pixabay), a strict resolution and aspect ratio quality gate, perceptual hash deduplication, DataStore local persistence, and native `WallpaperManager` integration.

- **Generated Debug APK Deliverable:** `app/build/outputs/apk/debug/app-debug.apk` (also mirrored at `app/build/outputs/apk/debug/Wally-debug.apk`)
- **Package Name:** `com.wally.app`
- **Minimum SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 34 (Android 14)
- **Main Activity:** `com.wally.app.MainActivity`

---

## 2. API Providers & Free-Tier Keys Configuration

All API keys are externalized and read from the gitignored `local.properties` file at the root of the project. If keys are omitted or pending approval, Wally provides high-resolution curated fallback presets so the application is completely functional and interactive immediately.

### How to Configure API Keys

Open or create `local.properties` in the project root:

```properties
# ==========================================
# Wally API Configuration (Free Tier Keys)
# ==========================================

# 1. Wallhaven API (Primary Source)
# URL: https://wallhaven.cc/settings/account
# Free tier: SFW public toplist searches can function without a key.
# For full user collections and higher rate limits, generate a key from your Account Settings.
WALLHAVEN_API_KEY=

# 2. Unsplash API (Secondary Source)
# URL: https://unsplash.com/developers
# Register a free developer account, create an application, and paste the "Access Key" here:
UNSPLASH_ACCESS_KEY=

# 3. Pexels API (Tertiary Source)
# URL: https://www.pexels.com/api/
# Sign up for a free developer account and retrieve your API Key from the dashboard:
PEXELS_API_KEY=

# 4. Pixabay API (Quaternary Source)
# URL: https://pixabay.com/api/docs/
# Create a free account and copy the API key shown in the documentation header:
PIXABAY_API_KEY=
```

### Provider Details & Query Specifications

1. **Wallhaven (`https://wallhaven.cc/api/v1`)**
   - **Weight:** Primary source (weighted first in feed).
   - **Parameters:** `purity=100` (SFW only), `sorting=toplist` or `relevance`, `atleast=1440x2560`, `ratios=9x16,9x18,9x19.5`.
   - **Quality threshold:** Minimum 5 favorites.

2. **Unsplash (`https://api.unsplash.com`)**
   - **Weight:** Secondary source.
   - **Parameters:** `orientation=portrait`, requesting `full` resolution URL only (never `thumb`/`small`), and hard-filtered by `width >= 1440`.
   - **Quality threshold:** Minimum 5 likes.

3. **Pexels (`https://api.pexels.com/v1`)**
   - **Weight:** Tertiary source.
   - **Parameters:** `orientation=portrait`, using `original` or `large2x` URLs only.
   - **Quality threshold:** Curated portrait feed, width >= 1440.

4. **Pixabay (`https://pixabay.com/api`)**
   - **Weight:** Quaternary source.
   - **Parameters:** `image_type=photo`, `orientation=vertical`, `min_width=1440`.
   - **Quality threshold:** `imageWidth * imageHeight >= 1440 * 2560`, minimum 5 likes.

---

## 3. Uniform Quality Gate & Deduplication

Regardless of source, every candidate image is evaluated by the `QualityGate` before insertion:

1. **Long-Edge Resolution Floor:**
   - Long edge must be >= 2560px.
   - Images below 2560px are strictly rejected without upscaling.
2. **Aspect Ratio Filtering:**
   - Filters for mobile portrait screens (approx. 9:16 to 9:20.5).
   - Width-to-height ratio must lie in the range `[0.40, 0.62]`. Landscape and square formats are discarded.
3. **Engagement Floor:**
   - Discards low-engagement or junk results based on each provider's metrics (Wallhaven favorites, Unsplash likes, Pixabay likes/downloads).
4. **Perceptual Hash Deduplication:**
   - Calculates a 64-bit visual fingerprint (perceptual/difference hash) and maintains a thread-safe registry.
   - Images within a Hamming distance <= 4 are flagged as duplicate/near-duplicate and rejected.

---

## 4. UI & Design System

Recreated pixel-faithfully from the dark-mode aesthetic:

- **Palette:**
  - Background: `#0A0A0A` (`WallyBg`)
  - Card: `#1A1A1A` (`WallyCard`)
  - Bronze Accent: `#C8832A` (`WallyBronze`)
  - Bronze Light: `#E0A04A` (`WallyBronzeLight`)
  - Primary Text: `#F2EDE6` (`WallyText`)
  - Muted Text: `#8A8580` (`WallyMuted`)
  - Hairline Border: `#2A2622` (`WallyHairline`)
  - Translucent Glass: `rgba(20, 20, 20, 0.90)` (`WallyGlassBg`)
- **Typography:**
  - Condensed display face (`CondensedDisplayFont`) for headings and badges.
  - Clean sans-serif (`InterBodyFont`) for body and metadata.
- **Screens:**
  1. **Home:** Hero "Today's Pick" (with bronze badge & gradient), horizontal category selector chips, curated collection carousel, and 2-column staggered masonry grid.
  2. **Explore:** Instant search input, multi-source provider filter chips (All, Wallhaven, Unsplash, Pexels, Pixabay), and trending theme tags.
  3. **Saved:** Favorites grid, counter badge, and empty state with bookmark illustration.
  4. **Profile & Settings:** Cache usage summary, "Clear Image Cache" button, provider connection status, and quality gate specifications.
- **Bottom Navigation:**
  - Floating glassmorphic pill bar with translucent background, hairline border, 4 navigation tabs, and a center elevated "Quick Pick / Dice" button.
- **Motion:**
  - Single deliberate transition per interaction: slide-up sheet entrance (`300ms`), animated bookmark fill on save, and button status feedback on wallpaper apply.
  - Edge-to-edge support with system safe-area drawing.

---

## 5. Storage, Cache & Data Persistence

1. **Coil Image Loading Cache:**
   - Configured in `WallyApplication.kt`:
     - **Disk Cache:** Hard-capped at **150MB** using `DiskCache.Builder().maxSizeBytes(150L * 1024 * 1024)`.
     - **Memory Cache:** Capped at **25%** of application memory class using `MemoryCache.Builder().maxSizePercent(0.25)`.
   - Never bulk downloads wallpaper files; always loads paginated thumbnails and streams full resolutions on demand.
2. **Local Persistence (DataStore):**
   - Jetpack `DataStore<Preferences>` (`androidx.datastore:datastore-preferences:1.1.1`).
   - Caches saved wallpaper IDs, serialized metadata JSON (URL, author, dimensions, tags), and the last active category.
   - Raw image bytes are never persisted in DataStore.
   - Room was intentionally omitted to minimize APK footprint and dependency overhead.

---

## 6. WallpaperManager Integration

- Handled via `WallpaperSetter.kt` using Android's native `WallpaperManager`.
- Supports three target destinations:
  1. `WallpaperTarget.HOME` -> `WallpaperManager.FLAG_SYSTEM`
  2. `WallpaperTarget.LOCK` -> `WallpaperManager.FLAG_LOCK`
  3. `WallpaperTarget.BOTH` -> `WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK`
- Downloads full-resolution stream on `Dispatchers.IO` and sets wallpaper directly without memory bloat.

---

## 7. Sandbox Constraints Compliance

- **Total Workspace File Count:** Under 100 files (constraint: max 10,000 files).
- **Total Workspace Size:** Under 1 MB (constraint: max 128 MB).
- **Transitive Footprint:** Zero bloated frameworks. No Firebase, no Dagger/Hilt (clean manual DI in `AppContainer`), no multi-module build overhead.
- **Clean Source Tree:** All generated build outputs (`build/`, `.gradle/`, `app/build/`) and secrets (`local.properties`) are strictly gitignored.
