# canlitv-android

Quick instructions

Local build

1. Ensure Android SDK is installed and `sdk.dir` is set in `local.properties`, or set `ANDROID_HOME` environment variable.

	You can create a `local.properties` automatically (uses `ANDROID_HOME` or `$HOME/Android/Sdk`):

```bash
./scripts/setup_local_properties.sh
```

2. Build the debug APK:

```bash
./gradlew assembleDebug
```

CI

This repository includes a GitHub Actions workflow at `.github/workflows/android-build.yml` which runs `./gradlew assembleDebug` on Ubuntu runners with JDK 17 and Android SDK installed.

