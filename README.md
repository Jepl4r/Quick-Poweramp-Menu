# Poweramp Quick Menu

A companion app and Magisk module for FiiO Android DAPs that remaps the multifunction button to control [Poweramp](https://powerampapp.com/).

**Short press** opens Poweramp. **Long press** opens a customizable quick menu with Poweramp actions.

## How It Works

### 1. Poweramp Quick Menu (Android App)

A lightweight app that displays a popup menu over Poweramp with your chosen actions.

Open the app to configure which actions appear in the menu (up to 10), and reorder them with the arrow buttons.

#### Available Actions

**Playback**
- Play / Pause, Next Track, Previous Track, Stop
- Next Album/Folder, Previous Album/Folder
- Seek Forward +10s, Seek Backward -10s

**Modes**
- Toggle Shuffle, Toggle Repeat
- Shuffle Off / All / Songs / Categories / Songs & Categories
- Repeat Off / List / Advance List / Song

**Rating**
- Like / Dislike (toggle)

**Screens**
- Equalizer, Search, Now Playing

**Library Categories**
- All Tracks, Folders, Albums, Artists, Album Artists, Composers
- Genres, Years, Playlists, Queue
- Top Rated, Recently Added, Recently Played, Most Played
- Long Tracks, Streams

### 2. Quick Poweramp Menu (Magisk Module)

A Magisk module that runs a background daemon listening for the multifunction button (`KEY_TV`) on `/dev/input/event1`. It distinguishes between short and long presses to open Poweramp (short press) or show the quick menu (long press).

The module also automatically disables the native FiiO `MultiFuncButtonHandler` overlay menu at boot.

The Quick Menu app is bundled inside the module zip and auto-installs during module installation.

> **Note:** Remember to also disable the native multifunction button single-press action in FiiO Settings to avoid conflicts.

## Requirements

- FiiO M21 (or similar FiiO Android DAP)
- Android 13
- Magisk with root
- [Poweramp](https://play.google.com/store/apps/details?id=com.maxmpz.audioplayer) installed

## Installation

### From source

1. **Build the app** - Open the `PowerampQuickMenu` project in Android Studio and build the APK (`Build → Build APK`)
2. **Add the APK to the module** - Copy the built APK into the module folder as `PowerampQuickMenu.apk` and create the zip file
3. **Flash the module** - Install the zip file through Magisk Manager
4. **Reboot** - The daemon starts automatically at boot
5. **Configure** - Open "Poweramp Quick Menu" from the launcher to customize your menu actions

### From the release page

1. **Download** the latest release
2. **Open Magisk**, **install** the module and **Reboot**

### Menu Actions

Open the Poweramp Quick Menu app from the launcher. Use the **+** button to add actions, **arrows** to reorder, and **✕** to remove. Changes take effect immediately, no reboot needed.

### Quick Menu Behavior

- The quick menu only appears if Poweramp is currently running (foreground or background)
- The menu closes when tapping outside or selecting an action
- Actions that send commands (play/pause, shuffle, like, etc.) execute instantly without opening Poweramp
- Actions that open screens (EQ, library, search) switch to Poweramp

## Project Structure

```
Poweramp_Quick_Menu/           # Android app source
├── app/src/main/java/com/mat/powerampmenu/
│   ├── MenuActivity.java    # Popup menu (launched by daemon)
│   ├── ConfigActivity.java  # App settings screen
│   ├── PowerampAction.java  # All available Poweramp actions
│   └── MenuPreferences.java # Saved menu configuration
├── app/src/main/res/
│   ├── drawable/             # Quick menu icons
│   ├── layout/               # XML layouts
│   ├── anim/                 # Fade in/out animations
│   └── values/               # Styles, strings
└── ...

Quick_Poweramp_Menu (Module)/             # Magisk module
├── module.prop
├── service.sh               # Boot daemon (button listener)
├── customize.sh             # Installation script
├── PowerampQuickMenu.apk    # Bundled app (add after build)
└── META-INF/                # Magisk flash scripts
```

