# Church Managements — Native Android

Native Kotlin + Jetpack Compose application for Growth Axys.

- Package: `com.growthaxys.churchmanagements`
- Minimum Android SDK: 29
- Backend integration: Supabase
- UI: Jetpack Compose + Material 3

## Default branch

The integrated native application has been promoted to **`main`**. The `integrate-experiments` branch is retained as the integration/source history.

## Included application

The integrated app combines the feature modules and UI work from the experiments line with the Android build setup from `android-native` (supabase-kt 2.6.1). It includes authentication, dashboard, attendance, communication, donations, events, families, finance, groups, members, reports, volunteers, and web-manager related functionality.

Login is email-based. Username login requires `LOGIN_RESOLVER_URL`, an optional trusted endpoint. Keep any Supabase service-role credential on a trusted server; never bundle it in the Android client.

## Build

Open the repository in Android Studio, allow Gradle sync to finish, then run the `app` configuration on an Android 10+ device or emulator.

Command-line debug build:

```bash
./gradlew :app:assembleDebug
```

Configure Supabase URL and publishable/anon key through Gradle properties, environment variables, or `local.properties`. Enforce authorization with Supabase Row Level Security (RLS).

The checked-in debug keystore is for debug/CI convenience only. Do not use it to sign a production release.

## Related project

- Web application: https://github.com/ysufimanuel/church-managements
- Integrated Android history: https://github.com/ysufimanuel/churchmanagement/tree/integrate-experiments

## Design and implementation notes

See `DESIGN.md`, `ANDROID_PARITY_IMPLEMENTATION.md`, and the documentation under `docs/`.
