#!/usr/bin/env python3
import http.server
import socketserver
import os
import urllib.parse
import json

PORT = 8080
DIRECTORY = "/home/user/Wally"
APK_PATH = os.path.join(DIRECTORY, "Wally.apk")

HTML_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Wally — Minimalist Dark-Mode Wallpaper App</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg: #0A0A0A;
            --card: #1A1A1A;
            --card-subtle: #141414;
            --bronze: #C8832A;
            --bronze-light: #E0A04A;
            --bronze-glow: rgba(200, 131, 42, 0.25);
            --text: #F2EDE6;
            --muted: #8A8580;
            --hairline: #2A2622;
            --glass-bg: rgba(26, 26, 26, 0.85);
            --glass-border: rgba(42, 38, 34, 0.9);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            -webkit-font-smoothing: antialiased;
        }

        body {
            background-color: var(--bg);
            color: var(--text);
            font-family: 'Inter', sans-serif;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            overflow-x: hidden;
        }

        /* Top Header */
        header {
            border-bottom: 1px solid var(--hairline);
            padding: 16px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: rgba(10, 10, 10, 0.95);
            position: sticky;
            top: 0;
            z-index: 50;
            backdrop-filter: blur(12px);
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .brand-title {
            font-family: 'Bebas Neue', sans-serif;
            font-size: 32px;
            letter-spacing: 2px;
            color: var(--text);
            line-height: 1;
        }

        .brand-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: var(--bronze);
            box-shadow: 0 0 10px var(--bronze);
        }

        .brand-sub {
            font-size: 11px;
            color: var(--muted);
            letter-spacing: 1.5px;
            font-weight: 600;
            margin-left: 12px;
            border-left: 1px solid var(--hairline);
            padding-left: 12px;
        }

        .header-actions {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .download-btn-primary {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: linear-gradient(135deg, var(--bronze-light), var(--bronze));
            color: var(--bg);
            font-family: 'Bebas Neue', sans-serif;
            font-size: 18px;
            letter-spacing: 1.2px;
            padding: 10px 22px;
            border-radius: 28px;
            text-decoration: none;
            font-weight: 700;
            box-shadow: 0 4px 16px var(--bronze-glow);
            transition: all 0.2s ease;
            border: none;
            cursor: pointer;
        }

        .download-btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 24px rgba(224, 160, 74, 0.4);
        }

        /* Hero Banner */
        .hero-banner {
            padding: 40px 24px 20px;
            max-width: 1200px;
            margin: 0 auto;
            width: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            gap: 16px;
        }

        .badge-pill {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: rgba(200, 131, 42, 0.12);
            border: 1px solid rgba(200, 131, 42, 0.4);
            border-radius: 20px;
            padding: 4px 12px;
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 1.2px;
            color: var(--bronze-light);
            text-transform: uppercase;
        }

        .badge-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: var(--bronze-light);
        }

        h1 {
            font-family: 'Bebas Neue', sans-serif;
            font-size: 54px;
            letter-spacing: 2px;
            color: var(--text);
            line-height: 1.05;
        }

        h1 span {
            color: var(--bronze-light);
        }

        .hero-sub {
            max-width: 650px;
            color: var(--muted);
            font-size: 15px;
            line-height: 1.6;
        }

        /* APK Card */
        .apk-card {
            background: var(--card);
            border: 1px solid var(--hairline);
            border-radius: 20px;
            padding: 24px;
            max-width: 780px;
            width: 100%;
            margin: 16px auto 32px;
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            justify-content: space-between;
            gap: 20px;
            box-shadow: 0 12px 32px rgba(0, 0, 0, 0.6);
        }

        .apk-info {
            display: flex;
            align-items: center;
            gap: 16px;
            text-align: left;
        }

        .apk-icon {
            width: 58px;
            height: 58px;
            border-radius: 14px;
            background: #111;
            border: 1px solid var(--bronze);
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Bebas Neue', sans-serif;
            font-size: 32px;
            color: var(--bronze-light);
            box-shadow: 0 4px 12px var(--bronze-glow);
        }

        .apk-meta h3 {
            font-size: 18px;
            color: var(--text);
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .apk-meta p {
            font-size: 13px;
            color: var(--muted);
            margin-top: 4px;
        }

        .apk-specs {
            display: flex;
            gap: 16px;
            font-size: 12px;
            color: var(--muted);
            margin-top: 6px;
        }

        .apk-specs span {
            color: var(--bronze-light);
            font-weight: 500;
        }

        /* Layout Grid: Phone Mockup & Specs */
        .main-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 24px 60px;
            display: grid;
            grid-template-columns: 1fr 380px;
            gap: 36px;
            width: 100%;
        }

        @media (max-width: 900px) {
            .main-container {
                grid-template-columns: 1fr;
            }
        }

        /* Phone Simulator */
        .phone-wrapper {
            display: flex;
            justify-content: center;
        }

        .phone-frame {
            width: 360px;
            height: 740px;
            background: #000;
            border: 4px solid #222;
            border-radius: 44px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.9), 0 0 0 2px var(--hairline);
            overflow: hidden;
            position: relative;
            display: flex;
            flex-direction: column;
        }

        .phone-notch {
            position: absolute;
            top: 10px;
            left: 50%;
            transform: translateX(-50%);
            width: 96px;
            height: 20px;
            background: #111;
            border-radius: 10px;
            z-index: 100;
        }

        .phone-screen {
            background: var(--bg);
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow-y: auto;
            position: relative;
            padding-bottom: 90px;
        }

        /* Phone UI Components */
        .phone-header {
            padding: 34px 18px 10px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .phone-logo {
            font-family: 'Bebas Neue', sans-serif;
            font-size: 26px;
            letter-spacing: 2px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .hero-pick-card {
            margin: 6px 14px 14px;
            height: 250px;
            border-radius: 18px;
            border: 1px solid var(--hairline);
            background: url('https://w.wallhaven.cc/full/4x/wallhaven-4x361z.jpg') center/cover no-decay, #1a1a1a;
            background-size: cover;
            position: relative;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 14px;
            cursor: pointer;
        }

        .hero-pick-card::after {
            content: '';
            position: absolute;
            inset: 0;
            background: linear-gradient(180deg, rgba(0,0,0,0.3) 0%, transparent 40%, rgba(0,0,0,0.85) 100%);
            pointer-events: none;
        }

        .hero-pick-content {
            position: relative;
            z-index: 2;
        }

        .hero-pick-badge {
            align-self: flex-start;
            background: rgba(10, 10, 10, 0.85);
            border: 1px solid rgba(200, 131, 42, 0.6);
            border-radius: 12px;
            padding: 4px 8px;
            font-size: 9px;
            letter-spacing: 1.2px;
            font-weight: 700;
            color: var(--bronze-light);
            text-transform: uppercase;
        }

        .category-row {
            display: flex;
            gap: 8px;
            overflow-x: auto;
            padding: 4px 14px 10px;
            scrollbar-width: none;
        }

        .category-row::-webkit-scrollbar {
            display: none;
        }

        .cat-chip {
            background: var(--card);
            border: 1px solid var(--hairline);
            border-radius: 16px;
            padding: 6px 12px;
            font-size: 10px;
            font-weight: 600;
            letter-spacing: 1px;
            color: var(--muted);
            white-space: nowrap;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .cat-chip.active {
            background: rgba(200, 131, 42, 0.16);
            border-color: var(--bronze-light);
            color: var(--bronze-light);
        }

        .collections-title {
            padding: 4px 16px;
            font-size: 10px;
            letter-spacing: 1.2px;
            font-weight: 700;
            color: var(--muted);
            text-transform: uppercase;
            display: flex;
            justify-content: space-between;
        }

        .collections-carousel {
            display: flex;
            gap: 10px;
            overflow-x: auto;
            padding: 6px 14px 14px;
            scrollbar-width: none;
        }

        .col-card {
            min-width: 140px;
            height: 80px;
            border-radius: 14px;
            border: 1px solid var(--hairline);
            background: #222;
            overflow: hidden;
            position: relative;
            padding: 8px 10px;
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
            cursor: pointer;
        }

        .col-card h4 {
            font-size: 11px;
            font-weight: 600;
            color: #fff;
            position: relative;
            z-index: 2;
        }

        .col-card span {
            font-size: 8px;
            color: var(--bronze-light);
            position: relative;
            z-index: 2;
        }

        .masonry-feed {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
            padding: 4px 14px;
        }

        .wall-card {
            border-radius: 14px;
            border: 1px solid var(--hairline);
            background: var(--card);
            overflow: hidden;
            position: relative;
            cursor: pointer;
            aspect-ratio: 9/16;
            background-size: cover;
            background-position: center;
        }

        .wall-card::after {
            content: '';
            position: absolute;
            inset: 0;
            background: linear-gradient(180deg, transparent 60%, rgba(0,0,0,0.85) 100%);
        }

        .wall-card-info {
            position: absolute;
            bottom: 8px;
            left: 8px;
            right: 8px;
            z-index: 2;
        }

        .wall-card-info p {
            font-size: 10px;
            font-weight: 600;
            color: #fff;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .wall-card-info span {
            font-size: 8px;
            color: var(--muted);
        }

        /* Glassmorphic Bottom Pill Bar */
        .bottom-pill-bar {
            position: absolute;
            bottom: 16px;
            left: 14px;
            right: 14px;
            height: 54px;
            background: var(--glass-bg);
            border: 1px solid var(--glass-border);
            border-radius: 28px;
            display: flex;
            align-items: center;
            justify-content: space-around;
            padding: 0 10px;
            backdrop-filter: blur(16px);
            z-index: 50;
            box-shadow: 0 8px 24px rgba(0,0,0,0.8);
        }

        .nav-btn {
            background: none;
            border: none;
            color: var(--muted);
            cursor: pointer;
            display: flex;
            flex-direction: column;
            align-items: center;
            font-size: 9px;
            gap: 2px;
            transition: color 0.2s;
        }

        .nav-btn.active {
            color: var(--bronze-light);
        }

        .nav-center-btn {
            width: 42px;
            height: 42px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--bronze-light), var(--bronze));
            border: none;
            color: var(--bg);
            font-size: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            box-shadow: 0 4px 12px var(--bronze-glow);
        }

        /* Interactive Preview Sheet Modal */
        .preview-modal {
            position: absolute;
            inset: 0;
            background: rgba(0,0,0,0.95);
            z-index: 200;
            display: none;
            flex-direction: column;
            justify-content: space-between;
            padding: 34px 16px 20px;
            background-size: cover;
            background-position: center;
            transition: transform 0.3s ease;
        }

        .preview-modal.open {
            display: flex;
        }

        .preview-top {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .close-btn {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: rgba(20,20,20,0.85);
            border: 1px solid var(--hairline);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            font-size: 16px;
        }

        .preview-bottom {
            background: linear-gradient(180deg, transparent, rgba(0,0,0,0.9) 30%, #000 100%);
            padding: 24px 8px 12px;
            display: flex;
            flex-direction: column;
            gap: 14px;
        }

        .set-btn {
            background: linear-gradient(135deg, var(--bronze-light), var(--bronze));
            color: var(--bg);
            font-family: 'Bebas Neue', sans-serif;
            font-size: 18px;
            letter-spacing: 1.2px;
            border: none;
            border-radius: 14px;
            padding: 14px;
            cursor: pointer;
            font-weight: 700;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            box-shadow: 0 4px 16px var(--bronze-glow);
        }

        /* Right Column: Sideload & Info */
        .side-panel {
            display: flex;
            flex-direction: column;
            gap: 24px;
        }

        .panel-box {
            background: var(--card);
            border: 1px solid var(--hairline);
            border-radius: 20px;
            padding: 24px;
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .panel-box h3 {
            font-family: 'Bebas Neue', sans-serif;
            font-size: 20px;
            letter-spacing: 1.2px;
            color: var(--bronze-light);
        }

        .install-steps {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .install-steps li {
            font-size: 13px;
            color: var(--text);
            display: flex;
            align-items: flex-start;
            gap: 10px;
            line-height: 1.5;
        }

        .step-num {
            width: 22px;
            height: 22px;
            border-radius: 50%;
            background: rgba(200, 131, 42, 0.2);
            color: var(--bronze-light);
            font-weight: 700;
            font-size: 11px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            margin-top: 1px;
        }

        .code-block {
            background: #101010;
            border: 1px solid var(--hairline);
            border-radius: 10px;
            padding: 10px 12px;
            font-family: monospace;
            font-size: 12px;
            color: #ddd;
            overflow-x: auto;
            margin-top: 6px;
        }

        .feature-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
        }

        .feature-item {
            background: var(--card-subtle);
            border: 1px solid var(--hairline);
            border-radius: 12px;
            padding: 12px;
        }

        .feature-item h5 {
            font-size: 11px;
            color: var(--bronze-light);
            text-transform: uppercase;
            letter-spacing: 0.8px;
            margin-bottom: 4px;
        }

        .feature-item p {
            font-size: 12px;
            color: var(--muted);
            line-height: 1.3;
        }

        /* Toast */
        .toast {
            position: fixed;
            bottom: 24px;
            right: 24px;
            background: var(--bronze);
            color: #000;
            padding: 12px 20px;
            border-radius: 30px;
            font-weight: 700;
            font-size: 14px;
            box-shadow: 0 6px 20px var(--bronze-glow);
            transform: translateY(100px);
            opacity: 0;
            transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            z-index: 1000;
        }

        .toast.show {
            transform: translateY(0);
            opacity: 1;
        }
    </style>
</head>
<body>

    <header>
        <div class="brand">
            <div class="brand-title">WALLY</div>
            <div class="brand-dot"></div>
            <div class="brand-sub">DARK EDITION</div>
        </div>
        <div class="header-actions">
            <a href="/Wally.apk" download="Wally.apk" class="download-btn-primary">
                ↓ DOWNLOAD APK (172 KB)
            </a>
        </div>
    </header>

    <div class="hero-banner">
        <div class="badge-pill">
            <div class="badge-dot"></div>
            Autonomous Production Build • v1.0.0
        </div>
        <h1>MINIMALIST <span>DARK-MODE</span> WALLPAPERS</h1>
        <p class="hero-sub">
            Curated 4K/UHD wallpapers aggregating Wallhaven, Unsplash, Pexels, and Pixabay under a strict long-edge 2560px quality gate with perceptual hash deduplication.
        </p>

        <!-- APK Download Card -->
        <div class="apk-card">
            <div class="apk-info">
                <div class="apk-icon">W</div>
                <div class="apk-meta">
                    <h3>Wally.apk <span class="badge-pill" style="font-size: 9px; padding: 2px 8px;">SIGNED</span></h3>
                    <p>Package: <code>com.wally.app</code> • Built for Android 8.0 to 14</p>
                    <div class="apk-specs">
                        <div>Size: <span>172 KB</span></div>
                        <div>Target: <span>Android SDK 34</span></div>
                        <div>UI: <span>Jetpack Compose M3</span></div>
                    </div>
                </div>
            </div>
            <a href="/Wally.apk" download="Wally.apk" class="download-btn-primary">
                ↓ DOWNLOAD .APK
            </a>
        </div>
    </div>

    <div class="main-container">
        <!-- Interactive Android Simulator -->
        <div class="phone-wrapper">
            <div class="phone-frame">
                <div class="phone-notch"></div>
                
                <div class="phone-screen" id="phoneScreen">
                    <div class="phone-header">
                        <div class="phone-logo">
                            <span>WALLY</span>
                            <div class="brand-dot"></div>
                        </div>
                        <div style="font-size: 9px; color: var(--muted); letter-spacing: 1px;">DARK EDITION</div>
                    </div>

                    <!-- Today's Pick Hero Card -->
                    <div class="hero-pick-card" id="todayHero" onclick="openPreview('https://w.wallhaven.cc/full/4x/wallhaven-4x361z.jpg', 'Kurogane', 'Wallhaven', '1440 × 3200')">
                        <div class="hero-pick-content">
                            <div class="hero-pick-badge">TODAY'S PICK</div>
                        </div>
                        <div class="hero-pick-content">
                            <h3 style="font-size: 16px; color: #fff;">OBSIDIAN GEOMETRY</h3>
                            <p style="font-size: 11px; color: var(--muted);">by Kurogane • 1440 × 3200 UHD</p>
                        </div>
                    </div>

                    <!-- Category Chips -->
                    <div class="category-row">
                        <div class="cat-chip active" onclick="selectCat(this)">ALL</div>
                        <div class="cat-chip" onclick="selectCat(this)">MINIMAL</div>
                        <div class="cat-chip" onclick="selectCat(this)">AMOLED</div>
                        <div class="cat-chip" onclick="selectCat(this)">NATURE</div>
                        <div class="cat-chip" onclick="selectCat(this)">ANIME</div>
                        <div class="cat-chip" onclick="selectCat(this)">CYBERPUNK</div>
                        <div class="cat-chip" onclick="selectCat(this)">SPACE</div>
                    </div>

                    <!-- Collections Row -->
                    <div class="collections-title">
                        <span>CURATED COLLECTIONS</span>
                        <span style="color: var(--bronze-light); cursor: pointer;">EXPLORE ALL →</span>
                    </div>
                    <div class="collections-carousel">
                        <div class="col-card" style="background: linear-gradient(180deg, transparent, rgba(0,0,0,0.85)), url('https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300') center/cover;" onclick="openPreview('https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1080', 'Alexander Ant', 'Unsplash', '1440 × 3120')">
                            <h4>Obsidian Dark</h4>
                            <span>24 WALLPAPERS</span>
                        </div>
                        <div class="col-card" style="background: linear-gradient(180deg, transparent, rgba(0,0,0,0.85)), url('https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=300') center/cover;" onclick="openPreview('https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1080', 'Tokyo Lights', 'Unsplash', '1440 × 3200')">
                            <h4>Neon Horizon</h4>
                            <span>18 WALLPAPERS</span>
                        </div>
                        <div class="col-card" style="background: linear-gradient(180deg, transparent, rgba(0,0,0,0.85)), url('https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?w=300') center/cover;" onclick="openPreview('https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?w=1080', 'Void Arch', 'Unsplash', '1440 × 2960')">
                            <h4>Minimalist Voids</h4>
                            <span>32 WALLPAPERS</span>
                        </div>
                    </div>

                    <!-- Masonry Grid -->
                    <div class="collections-title" style="margin-top: 6px;">
                        <span>FEATURED FEED</span>
                        <span style="color: var(--bronze-light);">4K RESOLUTION</span>
                    </div>
                    <div class="masonry-feed">
                        <div class="wall-card" style="background-image: url('https://w.wallhaven.cc/full/28/wallhaven-281zom.jpg');" onclick="openPreview('https://w.wallhaven.cc/full/28/wallhaven-281zom.jpg', 'NocturnalArt', 'Wallhaven', '1440 × 2960')">
                            <div class="wall-card-info">
                                <p>NocturnalArt</p>
                                <span>1440 × 2960 • Wallhaven</span>
                            </div>
                        </div>
                        <div class="wall-card" style="background-image: url('https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600');" onclick="openPreview('https://images.unsplash.com/photo-1518709268805-4e9042af9f23', 'Alexander Ant', 'Unsplash', '1440 × 3120')">
                            <div class="wall-card-info">
                                <p>Alexander Ant</p>
                                <span>1440 × 3120 • Unsplash</span>
                            </div>
                        </div>
                        <div class="wall-card" style="background-image: url('https://w.wallhaven.cc/full/72/wallhaven-72g139.jpg');" onclick="openPreview('https://w.wallhaven.cc/full/72/wallhaven-72g139.jpg', 'ApexVoid', 'Wallhaven', '1440 × 2880')">
                            <div class="wall-card-info">
                                <p>ApexVoid</p>
                                <span>1440 × 2880 • Wallhaven</span>
                            </div>
                        </div>
                        <div class="wall-card" style="background-image: url('https://images.pexels.com/photos/1624496/pexels-photo-1624496.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940');" onclick="openPreview('https://images.pexels.com/photos/1624496/pexels-photo-1624496.jpeg', 'Eberhard G.', 'Pexels', '1440 × 2560')">
                            <div class="wall-card-info">
                                <p>Eberhard Grossgasteiger</p>
                                <span>1440 × 2560 • Pexels</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Glassmorphic Bottom Pill Bar -->
                <div class="bottom-pill-bar">
                    <button class="nav-btn active" onclick="switchTab('Home')">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                        <span>Home</span>
                    </button>
                    <button class="nav-btn" onclick="switchTab('Explore')">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 10.9c-.61 0-1.1.49-1.1 1.1s.49 1.1 1.1 1.1 1.1-.49 1.1-1.1-.49-1.1-1.1-1.1zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm2.19 12.19L6 18l3.81-8.19L18 6l-3.81 8.19z"/></svg>
                        <span>Explore</span>
                    </button>
                    <button class="nav-center-btn" onclick="randomPick()" title="Quick Pick">
                        🎲
                    </button>
                    <button class="nav-btn" onclick="switchTab('Saved')">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z"/></svg>
                        <span>Saved</span>
                    </button>
                    <button class="nav-btn" onclick="switchTab('Profile')">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                        <span>Profile</span>
                    </button>
                </div>

                <!-- Preview Sheet Modal -->
                <div class="preview-modal" id="previewModal">
                    <div class="preview-top">
                        <div class="close-btn" onclick="closePreview()">✕</div>
                        <div class="badge-pill" id="previewSourceBadge" style="background: rgba(10,10,10,0.85);">WALLHAVEN • 1440 × 3200</div>
                    </div>
                    <div class="preview-bottom">
                        <div>
                            <div style="font-size: 9px; color: var(--bronze-light); letter-spacing: 1px; font-weight: 700;">PHOTOGRAPHY / ART</div>
                            <h2 style="font-size: 18px; color: #fff;" id="previewAuthor">Kurogane</h2>
                        </div>
                        <button class="set-btn" id="setWallpaperBtn" onclick="applyWallpaperAnim()">
                            SET WALLPAPER
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Sideloading Instructions & Specs -->
        <div class="side-panel">
            <div class="panel-box">
                <h3>INSTALL ON YOUR DEVICE</h3>
                <ul class="install-steps">
                    <li>
                        <div class="step-num">1</div>
                        <div>
                            <strong>Direct Download:</strong><br>
                            Click the download button above or <a href="/Wally.apk" download="Wally.apk" style="color: var(--bronze-light); text-decoration: underline;">Download Wally.apk</a> directly.
                        </div>
                    </li>
                    <li>
                        <div class="step-num">2</div>
                        <div>
                            <strong>Allow Unknown Sources:</strong><br>
                            On Android, enable <em>"Install unknown apps"</em> for your browser or file manager in Settings.
                        </div>
                    </li>
                    <li>
                        <div class="step-num">3</div>
                        <div>
                            <strong>Install via ADB (Optional):</strong>
                            <div class="code-block">adb install -r Wally.apk</div>
                        </div>
                    </li>
                </ul>
            </div>

            <div class="panel-box">
                <h3>QUALITY GATE SPECIFICATIONS</h3>
                <div class="feature-grid">
                    <div class="feature-item">
                        <h5>MIN RESOLUTION</h5>
                        <p>2560px long edge floor. No upscaled artifacts.</p>
                    </div>
                    <div class="feature-item">
                        <h5>ASPECT RATIO</h5>
                        <p>9:16 to 9:20.5 mobile portrait screen lock.</p>
                    </div>
                    <div class="feature-item">
                        <h5>DEDUPLICATION</h5>
                        <p>Perceptual visual hash comparison.</p>
                    </div>
                    <div class="feature-item">
                        <h5>CACHE BUDGET</h5>
                        <p>150MB disk limit & 25% memory ceiling.</p>
                    </div>
                </div>
            </div>

            <div class="panel-box">
                <h3>MULTI-API SOURCES</h3>
                <div style="font-size: 12px; color: var(--muted); line-height: 1.6;">
                    <p style="margin-bottom: 6px;"><strong style="color: var(--text);">1. Wallhaven:</strong> Primary source queried with purity=100 (SFW) and sorting=toplist.</p>
                    <p style="margin-bottom: 6px;"><strong style="color: var(--text);">2. Unsplash:</strong> Secondary editorial portrait source requesting full resolution.</p>
                    <p style="margin-bottom: 6px;"><strong style="color: var(--text);">3. Pexels:</strong> Curated portrait feed using original and large2x sizes.</p>
                    <p><strong style="color: var(--text);">4. Pixabay:</strong> High-resolution vertical photography clearing 1440×2560.</p>
                </div>
            </div>
        </div>
    </div>

    <div class="toast" id="toast">Wallpaper Applied ✓</div>

    <script>
        function openPreview(imgUrl, author, source, res) {
            const modal = document.getElementById('previewModal');
            modal.style.backgroundImage = `linear-gradient(180deg, rgba(0,0,0,0.4) 0%, transparent 40%, rgba(0,0,0,0.9) 100%), url('${imgUrl}')`;
            document.getElementById('previewAuthor').innerText = author;
            document.getElementById('previewSourceBadge').innerText = `${source.toUpperCase()} • ${res}`;
            modal.classList.add('open');
        }

        function closePreview() {
            document.getElementById('previewModal').classList.remove('open');
        }

        function applyWallpaperAnim() {
            const btn = document.getElementById('setWallpaperBtn');
            btn.innerText = 'APPLYING...';
            btn.style.opacity = '0.7';
            setTimeout(() => {
                btn.innerText = 'WALLPAPER APPLIED ✓';
                btn.style.opacity = '1';
                showToast('Wallpaper Applied ✓');
                setTimeout(() => {
                    closePreview();
                    btn.innerText = 'SET WALLPAPER';
                }, 1500);
            }, 800);
        }

        function selectCat(el) {
            document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('active'));
            el.classList.add('active');
            showToast(`Switched to ${el.innerText}`);
        }

        function switchTab(name) {
            document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
            event.currentTarget.classList.add('active');
            showToast(`Navigated to ${name}`);
        }

        function randomPick() {
            const picks = [
                { url: 'https://w.wallhaven.cc/full/4x/wallhaven-4x361z.jpg', author: 'Kurogane', source: 'Wallhaven', res: '1440 × 3200' },
                { url: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23', author: 'Alexander Ant', source: 'Unsplash', res: '1440 × 3120' },
                { url: 'https://w.wallhaven.cc/full/72/wallhaven-72g139.jpg', author: 'ApexVoid', source: 'Wallhaven', res: '1440 × 2880' }
            ];
            const p = picks[Math.floor(Math.random() * picks.length)];
            openPreview(p.url, p.author, p.source, p.res);
        }

        function showToast(msg) {
            const t = document.getElementById('toast');
            t.innerText = msg;
            t.classList.add('show');
            setTimeout(() => t.classList.remove('show'), 2000);
        }
    </script>
</body>
</html>
"""

class WallyRequestHandler(http.server.BaseHTTPRequestHandler):
    def do_HEAD(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path
        if path in ["/Wally.apk", "/app-debug.apk", "/download", "/app/build/outputs/apk/debug/app-debug.apk"]:
            if os.path.exists(APK_PATH):
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="Wally.apk"')
                self.send_header("Content-Length", str(os.path.getsize(APK_PATH)))
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
            else:
                self.send_error(404)
        else:
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        if path in ["/Wally.apk", "/app-debug.apk", "/download", "/app/build/outputs/apk/debug/app-debug.apk"]:
            if os.path.exists(APK_PATH):
                with open(APK_PATH, "rb") as f:
                    apk_bytes = f.read()
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="Wally.apk"')
                self.send_header("Content-Length", str(len(apk_bytes)))
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(apk_bytes)
                return
            else:
                self.send_error(404, "Wally.apk not found on server")
                return

        elif path in ["/NOTES.md", "/notes"]:
            notes_path = os.path.join(DIRECTORY, "NOTES.md")
            if os.path.exists(notes_path):
                with open(notes_path, "rb") as f:
                    notes_bytes = f.read()
                self.send_response(200)
                self.send_header("Content-Type", "text/markdown; charset=utf-8")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(notes_bytes)
                return

        # Default: Serve interactive portal with download button
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Access-Control-Allow-Origin", "*")
        html_bytes = HTML_PAGE.encode("utf-8")
        self.send_header("Content-Length", str(len(html_bytes)))
        self.end_headers()
        self.wfile.write(html_bytes)

    def log_message(self, format, *args):
        pass

def run():
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("0.0.0.0", PORT), WallyRequestHandler) as httpd:
        print(f"Serving Wally Download Portal on http://0.0.0.0:{PORT}...")
        httpd.serve_forever()

if __name__ == "__main__":
    run()
