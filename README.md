# Grow Companion

Grow Companion is a privacy-friendly Android app for organizing fertilizers,
recipes, seeds, and plant growth timelines in one place. It is designed for
growers who want practical records without relying on an online account or
cloud service.

## Features

- **Fertilizer library** - Store fertilizer names, manufacturers, types, and
  N-P-K values for quick reference.
- **Recipe management** - Create and maintain reusable feeding recipes.
- **Plant timelines** - Track plants through seedling, vegetative, flowering,
  and harvest phases.
- **Date calculations** - See how long each plant has been in its current
  phase.
- **Seed inventory** - Keep a personal overview of available seeds.
- **Backup and restore** - Export your local data and restore it when moving
  to another device.
- **Offline-first storage** - Your data is stored locally on the device.
- **Timer edition** - A lightweight build focused on plant timing and seed
  tracking.

## Screens and flavors

The project provides two product flavors:

| Flavor | Application ID | Includes |
| --- | --- | --- |
| `full` | `com.example.fertilizerapp` | Fertilizers, recipes, plant timelines, and seeds |
| `timer` | `com.example.fertilizerapp.timer` | Plant timelines and seeds |

The app interface currently contains German labels and texts.

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

Clone the repository, open it in Android Studio, allow Gradle to sync, and
run one of the available variants:

```bash
./gradlew assembleFullDebug
./gradlew assembleTimerDebug
```

On Windows, use `gradlew.bat` instead:

```bat
gradlew.bat assembleFullDebug
gradlew.bat assembleTimerDebug
```

The generated APKs are written below
`app/build/outputs/apk/`.

## Data and privacy

Grow Companion does not require an account or network connection for its core
features. Fertilizer, recipe, seed, and plant data is kept in the app's local
database. Use the in-app backup function before uninstalling the app or
changing devices.

## Project status

This is an actively developed personal Android project. The data model,
interface, and supported Android/Gradle versions may change as development
continues.

## License

No license has been selected yet. Until a license is added to this repository,
all rights are reserved by the project author.
