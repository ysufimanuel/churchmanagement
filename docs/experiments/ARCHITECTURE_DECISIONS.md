# ARCHITECTURE_DECISIONS
- ADR-001 Versions (Kotlin 2.0.21, AGP 8.7.3, Compose BOM 2024.12.01, Hilt 2.52, supabase-kt 3.0.3, Ktor 3.0.3). **Unverified: never compiled.** Align with the catalog on first build.
- ADR-002 MVVM + repository; ViewModels extend `FeatureViewModel<T>` (Load state, `mutate`).
- ADR-003 No WebView wrapper; native Compose.
- ADR-004 Room offline deferred; finance writes online-only.
- ADR-005 Android-only strings live in `assets/i18n/extra-*.json`.
