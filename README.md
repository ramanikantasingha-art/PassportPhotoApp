# Passport Photo App — Setup Guide

This is a complete, ready-to-open Android Studio project (Kotlin + Jetpack Compose)
implementing camera capture, orientation-aware auto-crop, ML Kit background removal,
lighting normalization, and 4x6in/300DPI canvas layout (12-photo or 6-photo grid),
with save-to-gallery and wireless print support.

## What's included
- Full Gradle project (Kotlin DSL) — just open the folder in Android Studio
- All source files under `app/src/main/java/com/passportphoto/app/`
- No OpenCV dependency (avoids flaky Maven resolution) — lighting normalization
  uses Android's native `ColorMatrix` instead. See "Optional: adding OpenCV" below
  if you want more advanced local-contrast enhancement later.

## 1. Open the project
1. Install Android Studio (https://developer.android.com/studio) if you haven't.
2. Unzip this project anywhere on your PC.
3. Android Studio → **Open** → select the unzipped `PassportPhotoApp` folder (the
   one containing `settings.gradle.kts`).
4. Wait for Gradle sync to finish (first sync downloads dependencies — a few
   minutes depending on your connection).

## 2. Run on your phone
1. On your phone: **Settings → About Phone** → tap **Build Number** 7 times to
   unlock Developer Options.
2. **Settings → System → Developer Options** → enable **USB Debugging**.
3. Plug your phone into your PC via USB. Tap **Allow** on the "Allow USB
   debugging?" popup that appears on your phone.
4. In Android Studio, select your phone from the device dropdown (top toolbar).
5. Click the green **Run ▶** button. The app builds and installs automatically.
6. Grant camera permission when prompted in the app.

## 3. Build an APK file to install manually
### Quick debug APK
**Build → Build App Bundle(s) / APK(s) → Build APK(s)**
Output: `app/build/outputs/apk/debug/app-debug.apk`
Copy this file to your phone (USB/email/Drive) and tap it to install (allow
"install from unknown sources" if prompted).

### Signed release APK (recommended for real use)
1. **Build → Generate Signed Bundle / APK** → choose **APK** → Next.
2. **Create new...** keystore — set a save path, password, alias, and 25+ year
   validity. **Save these credentials** — you'll need them for future updates.
3. Select **release** build variant → check both signature versions → Finish.
4. Output: `app/release/app-release.apk` — transfer and install as above.

## 4. Project structure
```
app/src/main/java/com/passportphoto/app/
├── MainActivity.kt              — entry point, sets up Compose + NavGraph
├── model/
│   ├── PassportSpec.kt          — country photo-size presets (US/Schengen/India)
│   ├── LayoutOption.kt          — 12-photo / 6-photo enum, background colors
│   └── CapturedPhoto.kt         — data classes for pipeline stages
├── camera/
│   ├── OrientationResolver.kt   — EXIF rotation → portrait/landscape detection
│   └── CameraController.kt      — CameraX preview + capture wrapper
├── processing/
│   ├── FaceCropUtils.kt         — ML Kit face detection + spec-based auto-crop
│   ├── BackgroundRemoval.kt     — ML Kit selfie segmentation + color composite
│   ├── LightingNormalizer.kt    — brightness/contrast auto-correction
│   ├── CanvasCompositor.kt      — builds the 4x6in/300DPI photo grid
│   ├── EnhancementPipeline.kt   — orchestrates the above, off main thread
│   └── ExportUtils.kt           — save to gallery + Android Print framework
├── viewmodel/
│   └── PhotoSessionViewModel.kt — shared state across all screens
├── navigation/
│   └── NavGraph.kt              — Camera → Editor → Layout → Export
└── ui/
    ├── screens/                 — one file per screen
    └── theme/                   — Compose Material3 theme
```

## 5. How orientation matching works
- `CameraController` captures a JPEG and hands it to `OrientationResolver`.
- `OrientationResolver` reads the file's EXIF rotation tag, rotates the decoded
  bitmap so its width/height match what the user actually saw, then classifies
  it `PORTRAIT` (width < height) or `LANDSCAPE` (width >= height).
- `CanvasCompositor` uses that orientation to swap the passport spec's width/
  height when placing photos, so a landscape capture produces landscape-
  oriented passport photo cells, and vice versa.

## 6. Known limitations / next steps
- Editor screen currently exposes background color + photo-spec pickers; a
  drag-to-reposition crop box is a good next addition (`EditorScreen.kt`).
- Background removal quality depends on ML Kit's selfie segmentation model —
  works well for typical headshots against plain backgrounds, less reliably
  with busy/cluttered scenes behind the subject.
- Front camera is used by default in `CameraController` (`DEFAULT_FRONT_CAMERA`)
  for selfie-style capture; switch to `DEFAULT_BACK_CAMERA` if you'd rather
  have someone else take the photo with the rear camera.

## 7. Optional: adding OpenCV later
If you want CLAHE-style local contrast enhancement beyond what `ColorMatrix`
provides:
1. Download the OpenCV Android SDK: https://opencv.org/releases/
2. Android Studio → **File → New → Import Module** → point to the SDK's `sdk` folder
3. Add `implementation(project(":opencv"))` to `app/build.gradle.kts`
4. Replace the body of `LightingNormalizer.normalize()` with OpenCV Mat-based
   CLAHE processing (convert to Lab color space, apply CLAHE to the L channel).

## Troubleshooting
| Problem | Fix |
|---|---|
| Phone not detected | Try a different USB cable/port; check USB debugging is enabled |
| Gradle sync fails | File → Invalidate Caches → Restart |
| "App not installed" | Uninstall any previous version first |
| ML Kit models not downloading | Ensure phone has internet on first run (models download on-device once) |
| Camera preview is blank | Confirm camera permission was granted in system settings |
