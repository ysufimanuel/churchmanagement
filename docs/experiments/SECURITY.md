# SECURITY
- No service-role key in the APK, ever. Release signing keys come from env/Gradle props, never committed.
- RLS is the authority; client role checks are UX only.
- Financial writes require connectivity (no offline queue).
- Release build: R8 + `proguard-rules.pro` (keep kotlinx-serialization models).
- Demo account profile edits are locked like the web.
- Debug-only demo dialog.
