# Release checklist

- [ ] Verify dependency/JDK/AGP/Gradle compatibility with CI.
- [ ] Configure debug against staging or safe test data.
- [ ] Configure production URL and publishable key without committing local.properties.
- [ ] Configure reviewed Android session persistence.
- [ ] Verify username login endpoint and OAuth redirect allowlist.
- [ ] Complete per-table RLS, storage and realtime audits.
- [ ] Complete all role-based functional tests and screenshot comparisons.
- [ ] Test API 29 and current Android physical devices.
- [ ] Test offline cache, account switching and outbox isolation.
- [ ] Add Crashlytics/FCM only after backend event contracts are approved.
- [ ] Configure signing secrets in GitHub Actions; never commit keystore.
- [ ] Build signed APK and AAB, verify upgrade/install path.
- [ ] Complete privacy policy and Play Data Safety review.
