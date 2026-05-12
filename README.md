# Video Journal App

Small production-style Android app for recording short local video journal clips, adding an optional description, and watching them in a vertical snapping feed.

## Setup

1. Open the project in Android Studio.
2. Use JDK 17.
3. Sync Gradle.
4. Run the debug app on a device or emulator with a camera app available.

Useful commands:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

## Architecture

The app uses a simple Clean Architecture split:

- `presentation`: Compose screens and ViewModels for feed playback and recording.
- `domain`: `VideoEntry`, repository contracts, and use cases.
- `data`: SQLDelight metadata repository and app-specific video file creation.
- `di`: Koin module wiring database, repositories, use cases, and ViewModels.

ViewModels only depend on domain use cases and domain models. `ExoPlayer` is created and released inside the feed composable item, so it is not stored in a ViewModel. File creation is behind `VideoFileRepository`, while SQLDelight only stores clip metadata.

## Technologies

- Kotlin
- Jetpack Compose
- Material 3
- Media3 ExoPlayer
- SQLDelight
- Koin
- JUnit and kotlinx-coroutines-test
- GitHub Actions CI

## Features

- Records videos through `ActivityResultContracts.CaptureVideo`.
- Stores video files in app-specific Movies storage through a `FileProvider`.
- Saves metadata locally with SQLDelight: `id`, `videoUri`, `description`, and `createdAt`.
- Shows a latest-first full-screen vertical snapping feed.
- Taps toggle inline play/pause.
- Keeps only the focused feed item playing.
- Handles missing or denied camera permission with user-visible messages.
- Supports sharing a saved video through Android's share sheet.

## Testing

Unit tests cover:

- use-case behavior
- fake repository ordering
- feed ViewModel focus/playback state
- record ViewModel capture, cancel, and save flows

Run:

```bash
./gradlew testDebugUnitTest
```

## Trade-offs and Limitations

`ActivityResultContracts.CaptureVideo` was chosen instead of a custom CameraX recording pipeline to keep the assignment focused on architecture, persistence, playback, and UX.

Thumbnails are not pre-generated; the feed uses Media3 playback surfaces directly. The app is intentionally local-only and does not include deletion, editing, cloud sync, or migration examples beyond the initial SQLDelight schema.
