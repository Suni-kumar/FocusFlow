# FocusFlow (SepFol) — Modern Android Workspace & Flashcard Studio

FocusFlow (SepFol) is a high-performance, edge-to-edge Android application engineered with **Jetpack Compose**, **Kotlin Coroutines & StateFlow**, and **Material Design 3**. It seamlessly combines a robust **Files & Document Vault** (with native PDF & Image viewing capabilities) with a **3D Flashcard Learning Studio**.

---

## 📱 Table of Contents
1. [Architecture & Tech Stack](#architecture--tech-stack)
2. [Design System & Visual Engineering](#design-system--visual-engineering)
3. [Core Feature Modules](#core-feature-modules)
   - [1. Files & Document Vault](#1-files--document-vault)
   - [2. Ergonomic PDF & Image Document Viewer](#2-ergonomic-pdf--image-document-viewer)
   - [3. Flashcard Studio & Active Recall Engine](#3-flashcard-studio--active-recall-engine)
   - [4. Visual Engines & Customization](#4-visual-engines--customization)
4. [Screen Navigation & Insets Architecture](#screen-navigation--insets-architecture)
5. [Gesture & Interaction Engine](#gesture--interaction-engine)
6. [Data Models & State Management](#data-models--state-management)
7. [Build & Environment Setup](#build--environment-setup)
8. [No-Modification Guarantee](#no-modification-guarantee)

---

## 🛠 Architecture & Tech Stack

- **UI Framework:** 100% Declarative Jetpack Compose (Kotlin DSL)
- **Design System:** Material Design 3 (M3) with custom glassmorphism layers, neon accents, and adaptive light/dark color schemes
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **State Management:** `StateFlow`, `collectAsState()`, and immutable UI state data classes
- **Asynchronous Execution:** Kotlin Coroutines (`viewModelScope`, `Dispatchers.IO`)
- **System Insets:** Strict edge-to-edge implementation (`enableEdgeToEdge()`, `statusBarsPadding()`, `navigationBarsPadding()`)
- **Android Target SDK:** `compileSdk = 35`, `targetSdk = 35`, `minSdk = 24`

---

## 🎨 Design System & Visual Engineering

FocusFlow utilizes a sophisticated dark slate aesthetic:
- **Surface Palette:** Obsidian dark canvases (`#0D0E17`, `#13141F`, `#1C1D2B`), deep slate card backgrounds, and frosted glass borders (`Color.White.copy(alpha = 0.08f)`).
- **Accent Radiance:** Focus Blue (`#3B82F6`), Emerald (`#10B981`), Cyber Amber (`#F59E0B`), Violet (`#8B5CF6`), and Crimson (`#EF4444`).
- **Tactile Haptics:** Integrated `LocalHapticFeedback` providing distinct haptic impulses for button clicks, long-press item selections, card flips, and swipe triggers.
- **Glassmorphism (`GlassCard`):** Reusable frosted-glass cards with subtle translucent gradient fills and crisp edge borders.

---

## 🚀 Core Feature Modules

### 1. Files & Document Vault (`FolderScreen.kt`, `FolderViewModel.kt`)
The vault provides comprehensive local document organization:
- **Folder Navigation & Breadcrumbs:** Real-time breadcrumb navigation trail at the top to traverse deep folder hierarchies with one-tap ancestor jumping and hardware back-press intercept.
- **Speed Dial FAB:** Animated multi-action FAB providing instant creation options:
  - 📁 *New Folder*: Custom naming with instant validation.
  - 📝 *New Markdown Note*: Rich title and markdown content editor.
  - 📥 *Import File*: System SAF file picker (`*/*`) for local storage imports.
- **Filter & Sort System:**
  - Dynamic type tabs: **ALL**, **PDF**, **IMAGE**, **MD**, **STARRED**.
  - Multi-attribute sorting: Sort by **Name**, **Date Modified**, **Size**, and **File Type**.
- **Context Menu Actions (`ItemActionMenuDialog.kt`):** Full item action sheet featuring:
  - ✏️ Rename (Folder / File)
  - 📌 Pin to Top (Priority ordering)
  - ⭐ Favorite / Star
  - 📋 Duplicate Item
  - 📦 Move to Destination Folder
  - 📤 Android System Share Sheet
  - 🗑️ Safe Delete confirmation
- **Multi-Selection Mode:** Long-press or top selection activation enables batch operations (Batch Delete, Batch Rename, Select All, Clear).

---

### 2. Ergonomic PDF & Image Document Viewer (`PdfImageViewerScreen.kt`)
Designed for an expanded, distraction-free reading experience:
- **Zero-Clutter Expanded Layout:** Main FocusFlow header is automatically hidden, shifting all document controls directly beneath the device status bar.
- **Smart Navigation Header (`ViewerTopBar`):**
  - Instant back navigation to the parent folder.
  - Document title and subtitle (Page count, file dimensions, file size).
  - **PDF Specific Controls:**
    - Fast page paging buttons (`<` Prev, `>` Next).
    - Page indicator badge (`Page X of Y`).
    - 🗂️ **4-Square Grid Overview Modal:** Visual thumbnail sheet to jump directly to any page.
    - 📤 Native Android share sheet button.
  - **Image Specific Controls:**
    - 🔄 90° Clockwise Rotation engine.
    - 🔲 Fit-to-screen / Reset viewport toggle.
    - 📤 Share button.
- **Precision Viewport & Gestures:**
  - Multi-touch pinch-to-zoom and two-finger pan gestures.
  - Bottom floating zoom pill: `-` (Zoom out), `100%` (Tap to reset), `+` (Zoom in).

---

### 3. Flashcard Studio & Active Recall Engine (`StudioScreen.kt`, `StudyScreen.kt`, `DecksDashboardScreen.kt`)
A comprehensive spaced-repetition and active recall study system:
- **3D Flippable Flashcards:**
  - Smooth 180° Y-axis rotation with front/back card face mirroring.
  - Interactive flip triggers: Tap card, press "Flip Card" button, or swipe.
- **Layered Stack Depth:** Multi-layer card background illusion representing remaining deck volume.
- **Study Mode (`StudyScreen.kt`):**
  - Progress bar and counter (`X / Total`).
  - Study-specific theme toggle (Light / Dark mode).
  - "Mastered" status tracking with forward progression.
  - Deck Shuffle capability.
- **Decks Dashboard (`DecksDashboardScreen.kt`):**
  - Full deck search and category tag filters.
  - Create new flashcard deck dialog with custom topic tagging and vibrant color presets.
  - Multi-deck selection and batch deletion.

---

### 4. Visual Engines & Customization (`SettingsDialog.kt`)
- **Theme Modes:** Dark Theme, Light Theme, and System Default.
- **Visual Engines:** Classic Obsidian, AMOLED Cyber, and Frost Glass.
- **Grid Layout Customizer:** Switch vault layout from 1-column list up to 4-column adaptive grid.
- **Accent Themes:** Cyber AMOLED, Emerald Glow, Solar Amber, Neon Violet.
- **Haptic Tactility Switch:** Toggle device vibration feedback globally.

---

## 🧭 Screen Navigation & Insets Architecture

### Screen State Machine (`SepFolApp.kt`)
- `ScreenState.MAIN_WORKSPACE`: Hosts bottom navigation bar between **Files Vault** and **Flashcard Studio**.
- `ScreenState.STUDY_STAGE`: Full-screen active recall flashcard study interface.
- `ScreenState.ALL_DECKS`: Dedicated deck collection manager.
- `ScreenState.SETTINGS`: App-wide customization and visual settings.

### Context-Aware Top App Bar (`SepFolTopAppBar`)
- **Visible Strictly On:**
  - Home Vault root (`folderStack.size <= 1`).
  - Flashcard Studio tab root.
- **Automatically Hidden & Screen Expanded On:**
  - PDF & Image Document Viewer.
  - Subfolder exploration (`folderStack.size > 1`).
  - Active Flashcard Study Mode.
  - All Decks manager & Settings.

---

## 🎯 Gesture & Interaction Engine

- **Swipe Left on Settings Gear:** Instantly exposes the live search bar.
- **Swipe Up on FAB:** Quick creation shortcut with haptic feedback.
- **Long Press on Items:** Activates multi-selection mode.
- **Pinch & Double Tap in Viewer:** Fluid viewport scaling and centering.
- **Card Tap in Study Mode:** Triggers 3D perspective flip.

---

## 📦 Data Models & State Management

### Key Data Entities
- `FolderItem`: Represents directories and files (`ItemType.FOLDER`, `ItemType.PDF`, `ItemType.IMAGE`, `ItemType.MARKDOWN`). Contains metadata (`isPinned`, `isFavorite`, `sizeBytes`, `lastModified`, `contentData`).
- `FlashcardDeck`: Deck metadata (`id`, `title`, `description`, `cardCount`, `progress`, `categoryColor`, `iconName`).
- `Flashcard`: Individual card payload (`id`, `front`, `back`, `isMastered`).

---

## 🏗 Build & Environment Setup

1. **Prerequisites:**
   - Android Studio Ladybug (2024.2.1) or newer.
   - JDK 17 or JDK 21.
   - Gradle 8.7+ with Android Gradle Plugin (AGP) 8.6+.
2. **Build Commands:**
   ```bash
   # Build debug APK
   gradle :app:assembleDebug

   # Run local unit tests
   gradle :app:testDebugUnitTest
   ```
3. **Dependencies:**
   - `androidx.compose.ui:ui`
   - `androidx.compose.material3:material3`
   - `androidx.compose.material:material-icons-extended`
   - `androidx.lifecycle:lifecycle-viewmodel-compose`
   - `androidx.navigation:navigation-compose`
   - `androidx.activity:activity-compose`

---

## 🔒 No-Modification Guarantee

This repository contains the complete, self-contained implementation of FocusFlow. All screens, dialogs, gestures, animations, and theme controllers are fully implemented without missing dependencies or placeholders. When cloned or built on any environment or account, the application compiles and runs out of the box with the exact layout and features documented above.
