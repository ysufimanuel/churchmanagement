# Church Managements — Android Native

Native Kotlin + Jetpack Compose application for Growth Axys. Package: `com.growthaxys.churchmanagements`, minSdk 29.

See `docs/ANDROID_PARITY_IMPLEMENTATION.md` and `docs/WEB_PARITY_MATRIX.md`. Current branch: `android-native`.

## Integrated app (branch `integrate-experiments`)
Full module set from `experiments` (see `docs/experiments/`) on top of the `android-native` build setup (supabase-kt 2.6.1). Login is email-based; username login needs `LOGIN_RESOLVER_URL` (optional trusted endpoint, service-role stays server-side).
