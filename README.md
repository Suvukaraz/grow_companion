# Grow Companion

Grow Companion is a privacy-friendly Android app for organizing fertilizers,
nutrient solutions (*Nährlösungen*), seeds (*SamenDB*), and plant growth timelines in one place. It is designed for growers who want practical records without relying on an online account or cloud service.

## Features

- **Fertilizer library (Dünger)** - Store fertilizer names, manufacturers, types (liquid/solid), density, and N-P-K values for quick reference.
- **Nutrient solution recipes (Nährlösungen 🧪)** - Create and maintain reusable nutrient solution recipes with volume calculations and automatic N-P-K result previews.
- **Plant timelines (Zeiten)** - Track plants through growth phases (*Sämling*, *Wachstum*, *Blüte*, *Trocknung*, or custom phase names) with elapsed day counters.
- **Customizable phase colors (Phasen-Farben)** - Easily assign custom colors to your growth phases via the settings menu for a personalized timeline view.
- **Seed database (SamenDB 🌱)** - Keep a detailed inventory of available seeds with sorting by strain, quantity, breeder, autofem type, growth time, and THC content.
- **Settings & Backup** - Export and restore all your local data (fertilizers, nutrient solutions, seeds, plant timelines, and phase colors) via local JSON files.
- **Offline-first storage** - Your data is stored 100% locally on your device.
- **Timer edition** - A lightweight build flavor focused on plant growth timelines and seed tracking.

## Screens and flavors

The project provides two product flavors:

| Flavor | Application ID | Includes Tabs |
| --- | --- | --- |
| `full` | `com.example.fertilizerapp` | Dünger, Nährlösungen 🧪, Zeiten, SamenDB 🌱 |
| `timer` | `com.example.fertilizerapp.timer` | Zeiten, SamenDB 🌱 |

The app interface contains German labels and texts.

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- Android Room
- Kotlin Serialization and Gson
- Gradle Version Catalog

## Requirements

- Android Studio with Android SDK 36
- JDK 11 or newer
- Android 8.0 (API 26) or newer for running the app

## Build and run

Clone the repository, open it in Android Studio, allow Gradle to sync, and run one of the available variants:

```bash
./gradlew assembleFullDebug
./gradlew assembleTimerDebug
```

On Windows, use `gradlew.bat` instead:

```bat
gradlew.bat assembleFullDebug
gradlew.bat assembleTimerDebug
```

The generated APKs are written to `app/build/outputs/apk/`.

## Data and privacy

Grow Companion does not require an account or network connection for its core features. All fertilizer, recipe, seed, plant, and phase color data is kept in the app's local database and preferences. Use the in-app backup function before uninstalling the app or changing devices.

## Project status

This is an actively developed personal Android project. The data model, interface, and supported Android/Gradle versions may change as development continues.

## License

No license has been selected yet. Until a license is added to this repository, all rights are reserved by the project author.
