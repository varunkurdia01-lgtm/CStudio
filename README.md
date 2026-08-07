<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/b6a70d36-0405-4804-afa8-6a606c0c4d8f

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.


## Offline compiler setup

This build supports an offline C/C++ toolchain that is installed into the app's private storage.

1. Open **Settings**
2. In **Build System**, tap **Install Offline Toolchain ZIP**
3. Select your toolchain ZIP (it should contain `bin/`, `lib/`, and `include/`)
4. Switch **Compiler Mode** to **LOCAL COMPILER TEST (Offline)**

After installation, the app will compile C and C++ code locally without internet access.
