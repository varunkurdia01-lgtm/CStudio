# CStudio — Phone-only APK build

This project is prepared to build the debug APK using GitHub Actions, so a laptop/Android Studio is not required.

## What was prepared
- Added `.github/workflows/build-apk.yml`
- Uses Java 17
- Uses Gradle 9.3.1
- Installs Android API 36, Build Tools 36.0.0, CMake 3.22.1 and NDK 28.2.13676358
- Builds `app-debug.apk`
- Uploads the APK as a workflow artifact

## After the APK is built
1. Install the APK on the Android phone.
2. Open CStudio.
3. Open Settings.
4. Choose **Install Offline Toolchain ZIP**.
5. Select `compiler-minimal.zip`.
6. Switch Compiler Mode to **LOCAL COMPILER TEST (Offline)**.
7. Test C and C++.

The offline compiler ZIP is intentionally NOT embedded in this project ZIP. Keep using the separately-created `compiler-minimal.zip` (about 101 MB).
