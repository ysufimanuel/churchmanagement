# Church Managements — Native Android

Native Android client for the Church Management System ecosystem.

## Technology

- Kotlin
- Jetpack Compose
- Android Gradle Plugin / Kotlin Gradle plugin
- Supabase integration
- Package: `com.growthaxys.churchmanagements`
- Minimum Android SDK: 29

## Branch guide

| Branch | Purpose |
| --- | --- |
| `main` | Repository landing page and implementation notes |
| `android-native` | Native application baseline |
| `integrate-experiments` | Integrated native feature set and UI updates |
| `experiments` | Earlier experimental work |
| `backup-before-author-rewrite` | Historical backup |

The source tree is currently maintained on `integrate-experiments`. It is not the default branch. That branch and `android-native` have diverged, so compare and test changes before merging or changing the default branch.

## Build

Open the repository in Android Studio, allow Gradle sync to finish, then run the `app` configuration on an Android 10+ device or emulator.

For command-line builds:

```bash
./gradlew :app:assembleDebug
```

Configure Supabase URL and publishable/anon key through Gradle properties, environment variables, or `local.properties`. The public client key is not a substitute for database security: enforce access through Supabase RLS. Never place a service-role key in the Android client.

The checked-in debug keystore is only for debug/CI convenience. Do not use it to sign a production release.

## Related project

- Web application: https://github.com/ysufimanuel/church-managements
- Native source: https://github.com/ysufimanuel/churchmanagement/tree/integrate-experiments
