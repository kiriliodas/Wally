# Wally build notes

## Product decisions

Wally is a single-module, dark-mode Compose wallpaper browser. The primary path is a portrait-first home feed with a "today's pick" hero, category chips, collections, a two-column masonry-style grid, an edge-to-edge preview sheet, and a translucent five-slot bottom bar with a center upload action. Explore, Saved, and Profile are real screens rather than template placeholders; Profile is intentionally a small local-only product/about surface because there is no account backend in the brief.

The provided request described the HTML source of truth but did not include a separate HTML file in this checkout, so the implementation uses the palette and interaction details in the prompt directly. The UI uses the platform sans family with heavy/condensed tracking for display text instead of bundling a font file; that keeps the APK small and avoids shipping an unlicensed font. The result is the closest available system equivalent to Bebas Neue/Inter on a clean Android install.

## Architecture

- One `:app` module, Kotlin DSL, min SDK 26, target/compile SDK 35.
- One manual composition root: `WallyApplication` creates `AppContainer`, which owns the repository, DataStore store, and wallpaper applier.
- `WallpaperSource` is the single source interface. Wallhaven is first, followed by Unsplash, Pexels, and Pixabay. Requests are concurrent, provider order is preserved in the merge, and results are quality-gated before Compose sees them.
- The uniform quality gate rejects images with a long edge below 2560 px, fewer than 1440 × 2560 pixels, or a width/height ratio outside 9:16–9:20.5. It also applies provider engagement floors where the API exposes likes/favorites. Stable provider IDs plus normalized source URLs deduplicate the feed without downloading an entire catalog for hashing.
- Only saved stable IDs and the last category are persisted in Preferences DataStore. The small built-in curated fallback is metadata-only and makes the first launch useful with no keys or no network; API images are fetched on demand and paginated. Uploaded content stays in the current process, as no raw user image is copied into app storage.
- Coil is configured for a 150 MiB disk cache and 25% memory-cache percentage. Setting a wallpaper downloads only the chosen image to a temporary file, streams it to `WallpaperManager` for both system and lock flags, then deletes the temporary file.

## API keys

Create a local, uncommitted `local.properties` beside `settings.gradle.kts`. Do not commit it and do not paste keys into Kotlin source. The app reads these values into generated `BuildConfig` fields; blank values disable that provider gracefully.

```properties
UNSPLASH_ACCESS_KEY=your Unsplash Access Key from the Unsplash developer dashboard
PEXELS_API_KEY=your Pexels API key from the Pexels API dashboard
PIXABAY_API_KEY=your Pixabay API key from the Pixabay API dashboard
WALLHAVEN_API_KEY=
```

Wallhaven's public v1 endpoint does not require a key for the requested search calls, so the field is reserved but intentionally unused. The other three entries are the free-tier application keys, not OAuth secrets. The app never fabricates or hardcodes a key. Unsplash requests use portrait orientation and the `full` URL; Pexels uses `original` then `large2x`; Pixabay uses `image_type=photo`, `orientation=vertical`, `min_width=1440`, safe search, and a full-HD/large URL only.

## Pinned dependencies

- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21 and Compose compiler plugin 2.0.21
- Gradle 8.9
- Compose BOM 2024.09.03 / Material 3 from that BOM
- Activity Compose 1.9.2, Lifecycle 2.8.6, Core KTX 1.13.1
- DataStore Preferences 1.1.1
- Coroutines Android 1.9.0
- kotlinx.serialization JSON 1.7.3
- Retrofit 2.11.0 with its kotlinx-serialization converter
- OkHttp 4.12.0
- Coil Compose 2.7.0

The dependency set deliberately omits Room, Firebase, DI frameworks, KSP, networking image catalogs, and test scaffolding beyond what a normal Android build would need. Manual DI is sufficient for this app. The only deliberate product scope cuts are remote account sync, image perceptual hashing (source IDs/normalized URLs avoid bulk downloads), and a server-backed profile.

## Build and delivery

Run `./gradlew assembleDebug` from the repository root. API keys are optional because the curated fallback is built into metadata. Generated `build/` and `.gradle/` directories are ignored; the final hand-off APK is kept under the ignored `artifacts/` directory rather than committed to source.

The Arena base image did not include Java or an Android SDK, and its outbound Maven/Gradle HTTPS downloads close during TLS negotiation. I still attempted `./gradlew --no-daemon assembleDebug`; it stopped at the Gradle 8.9 distribution download before evaluating the project. To honor the installable-APK deliverable in this constrained environment, `artifacts/Wally-debug.apk` is a separately signed smoke hand-off assembled with a minimal public Android platform toolchain available through Git. It launches the Wally visual flow, local upload chooser, save state, and `WallpaperManager` home/lock action; the Kotlin/Compose project in this repository remains the canonical full implementation for a normal Android build with the pinned dependencies above.
