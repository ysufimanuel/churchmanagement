# Church Managements Android Native

## Parity-First Product & Technical Specification

**Document:** `ANDROID_PARITY_IMPLEMENTATION.md`\
**Version:** 1.1\
**Status:** Baseline approved, pending repository and backend audit\
**Product owner:** Growth Axys\
**Application ID:** `com.growthaxys.churchmanagements`

------------------------------------------------------------------------

## 1. Executive decision

The first Android release must reproduce the **existing Church
Managements mobile-web experience as closely as reasonably possible**.

This is a **parity-first native implementation**, not a redesign.

The existing web application is the product source of truth for: -
Available features and workflows. - Role and permission behavior. -
Business rules and validation. - Labels, terminology, and information
hierarchy. - Mobile layout, navigation, visual styling, and interaction
patterns. - Empty, loading, error, and success states.

The Android app will use Kotlin and Jetpack Compose for a genuinely
native implementation. It must not embed the website in a WebView as a
shortcut. Compose components should reproduce the current mobile-web
appearance and behavior, while using native Android capabilities where
appropriate.

**Important distinction:** "1:1" means functional and visual parity with
the current mobile-web version, subject to Android platform conventions
and technical constraints. It does not mean copying HTML/CSS or forcing
browser-only behavior into Android.

## 2. Approved project configuration

  ------------------------------------------------------------------------
  Area                                Decision
  ----------------------------------- ------------------------------------
  Product                             Church Managements Android

  Owner / brand                       Growth Axys

  Package / Application ID            `com.growthaxys.churchmanagements`

  Language                            Kotlin 2.0 baseline

  UI                                  Jetpack Compose

  Design system                       Material 3 components, styled to
                                      match the existing web UI

  Minimum SDK                         29 (Android 10)

  Compile / Target SDK                Select current stable versions
                                      compatible with the release
                                      requirements

  Architecture                        MVVM + repository pattern, with
                                      domain/use-case layer where useful

  State                               Kotlin Coroutines + Flow / StateFlow

  Dependency injection                Hilt

  Navigation                          Navigation Compose

  Backend                             Existing Supabase project

  Supabase integration                Supabase Kotlin SDK, Ktor engine,
                                      Kotlinx Serialization

  Local database                      Room

  Preferences                         DataStore

  Background work                     WorkManager

  Images                              Coil

  Push notifications                  Firebase Cloud Messaging

  Crash reporting                     Firebase Crashlytics

  Analytics                           Firebase Analytics, subject to
                                      privacy requirements

  Source control                      Separate GitHub repository

  Repository name                     `church-managements-android`

  Distribution                        Signed APK for direct
                                      testing/distribution + AAB for
                                      Google Play

  Offline strategy                    Advanced offline-first, introduced
                                      safely by feature

  Initial product strategy            Web mobile parity first; native
                                      enhancements later
  ------------------------------------------------------------------------

### Version policy

Do not blindly pin Gradle 8.5 or any other version from an old draft.
Before implementation, choose and verify a compatible set of: - JDK -
Android Gradle Plugin (AGP) - Gradle Wrapper - Kotlin - Compose compiler
plugin - AndroidX and third-party libraries

Keep dependency versions in `gradle/libs.versions.toml`. Record material
version changes in the project changelog or an Architecture Decision
Record (ADR).

Kotlin 2.0 remains the requested baseline, but compatibility must be
proven during project bootstrap. Do not silently upgrade or downgrade
it.

## 3. Product principle: web parity first

### 3.1 Source of truth

The current web application determines what the Android app must do.
Android must not invent a new workflow or remove an existing capability
merely because the native implementation is easier that way.

Before implementing a screen, inspect the corresponding web: 1. Route
and page structure. 2. Mobile layout and responsive breakpoints. 3.
Components and visual tokens. 4. User actions and resulting behavior. 5.
Data sources and queries. 6. Role and permission checks. 7. Validation
rules. 8. Loading, empty, error, and success states. 9. Search, sort,
filter, pagination, and refresh behavior. 10. Related dialogs, sheets,
menus, and confirmation flows.

### 3.2 Visual parity requirements

Reproduce the mobile-web UI as closely as possible: - Brand colors and
theme. - Typography, font weights, and text hierarchy. - Spacing,
padding, and component dimensions. - Cards, borders, radii, shadows, and
dividers. - Header, sidebar/drawer, bottom navigation, and page
hierarchy. - Avatar initials and image behavior. - Forms, buttons,
icons, badges, and status indicators. - Dialogs, dropdowns, bottom
sheets, and confirmation prompts. - Light/dark theme behavior, if
supported by the web app. - Responsive behavior at the supported mobile
widths.

Material 3 is the component foundation, **not permission to replace the
current design with default Material styling**. Apply the web design
tokens and component styling to Compose.

Where a web interaction has no direct Android equivalent, use the
closest native interaction without changing the business workflow.
Record the difference in the parity checklist.

### 3.3 Functional parity requirements

For every web feature included in the release, verify: - Same data and
business rules. - Same role/permission visibility and enforcement. -
Same create/read/update/delete capabilities. - Same validation and error
messages where practical. - Same search, filter, sort, and pagination
results. - Same confirmation and destructive-action behavior. - Same
upload and attachment constraints. - Same calculations and summaries. -
Same realtime expectations. - Same empty/loading/error states.

A button is not considered implemented merely because it is visible or
clickable. It must perform the same authorized action as the web app.

### 3.4 Feature parity matrix

Create `docs/WEB_PARITY_MATRIX.md` and maintain one row per web feature
or workflow.

  ---------------------------------------------------------------------------
  Web module /    Mobile web  Android     Roles       Actions     Status
  workflow        reference   screen                              
  --------------- ----------- ----------- ----------- ----------- -----------
  Dashboard       To audit    To map      To audit    To audit    Not audited

  Members         To audit    To map      To audit    To audit    Not audited

  Worship         To audit    To map      To audit    To audit    Not audited
  schedules                                                       

  Donations       To audit    To map      To audit    To audit    Not audited

  Announcements   To audit    To map      To audit    To audit    Not audited

  Media           To audit    To map      To audit    To audit    Not audited

  Profile         To audit    To map      To audit    To audit    Not audited

  Management /    To audit    To map      To audit    To audit    Not audited
  settings                                                        
  ---------------------------------------------------------------------------

The list above is an initial checklist, **not a claim that these are the
complete modules or exact database names**. Add every actual route,
menu, subpage, modal, and workflow found in the current web project.

Parity status values: - `NOT_AUDITED` - `MAPPED` - `IN_PROGRESS` -
`IMPLEMENTED` - `VERIFIED` - `BLOCKED`

No module may be marked `VERIFIED` until visual, functional, permission,
and data behavior have been checked against the web app.

## 4. Readiness: what is ready and what is not

### 4.1 Decisions already made

The following decisions are sufficiently clear to start project setup: -
Separate Android repository. - Package ID. - Minimum SDK 29. - Kotlin +
Compose + Material 3. - Supabase remains the shared backend. - Room is
the local database for offline support. - APK and Play Store AAB
distribution. - Existing mobile-web UI is the initial visual and
functional reference.

### 4.2 Required before feature implementation

These items are **not yet confirmed by this document** and must be
inspected from the actual web repository and Supabase project:

1.  Complete list of web routes, pages, menus, dialogs, and workflows.
2.  Screenshots or recordings of the current mobile-web UI at
    representative widths.
3.  Actual database schema, relationships, views, RPCs, triggers, and
    functions.
4.  Actual authentication and Google OAuth configuration.
5.  Actual roles, permission model, and tenant/church isolation rules.
6.  RLS policies for every table and storage bucket.
7.  Storage bucket names, access rules, upload size/type limits, and
    public/private behavior.
8.  Realtime publications and event behavior.
9.  Existing business logic that must remain consistent across web and
    Android.
10. Existing search/filter/pagination behavior and any known
    limitations.
11. Existing profile/avatar rules, including initials fallback and who
    can update a photo.
12. Environment strategy for development/staging/production.

**Readiness conclusion:** The project is ready to begin repository
bootstrap and web/backend auditing. It is not ready to safely implement
all business features until the audit and parity matrix are completed.
Do not guess table names, columns, role names, or permissions.

## 5. System architecture

### 5.1 High-level architecture

``` text
Android UI (Jetpack Compose)
          |
          v
ViewModel + StateFlow
          |
          v
Use Cases / Domain (where useful)
          |
          v
Repository interfaces
          |
          v
Repository implementations
       /       \
      v         v
   Room      Supabase SDK
 Local DB     Remote API
      \         /
       \       /
     Sync / Outbox
          |
      WorkManager
```

### 5.2 Architectural rules

-   Compose screens render state and emit user events.
-   ViewModels expose immutable UI state.
-   ViewModels do not issue Supabase queries directly.
-   Repositories coordinate local and remote data.
-   Room is the source for application reads after local data has been
    cached.
-   Supabase remains the authoritative remote source of business data.
-   Backend RLS and server-side validation are authoritative for
    permissions.
-   Do not duplicate business calculations in separate web and Android
    implementations when a shared backend function/RPC already exists.
-   Keep modules reasonably modular; do not over-engineer every small
    screen into many layers.

### 5.3 Suggested package structure

``` text
app/src/main/java/com/growthaxys/churchmanagements/
├── core/
│   ├── common/
│   ├── designsystem/
│   ├── navigation/
│   ├── network/
│   ├── security/
│   └── util/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── database/
│   ├── remote/
│   │   ├── dto/
│   │   └── datasource/
│   ├── mapper/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── di/
└── feature/
    ├── splash/
    ├── auth/
    ├── dashboard/
    ├── [actual-web-feature]/
    ├── notifications/
    ├── profile/
    └── settings/
```

Replace `[actual-web-feature]` with the modules discovered during the
web audit. Do not create empty feature packages for guessed modules.

## 6. Backend integration requirements

### 6.1 Shared Supabase project

Android and web must use the same existing Supabase project and business
data. Do not create a second production database for Android.

Before writing Android data models, produce: -
`docs/BACKEND_INTEGRATION_AUDIT.md` - `docs/DATABASE_MAPPING.md` -
`docs/PERMISSION_MATRIX.md`

The mapping must use actual schema names and types from the project.

### 6.2 Security

-   Use only the Supabase publishable/anon client key in the app.
-   Never put a service-role key, database password, or server secret in
    the APK.
-   Treat all client-side role checks as UI convenience only.
-   Enforce access through RLS and trusted backend logic.
-   Verify tenant/church isolation if the web system is multi-tenant.
-   Do not log access tokens, refresh tokens, passwords, or sensitive
    member/donation data.
-   Use HTTPS.
-   Keep private media private and use authorized access/signed URLs
    where appropriate.
-   Validate file type and size on the backend as well as the client.
-   Ensure logout/account switching clears or isolates user-specific
    local data.

### 6.3 Authentication

-   Reuse Supabase Auth and existing account records.
-   Support the login methods already supported by the web product,
    subject to a verified native OAuth setup.
-   Implement session restoration, refresh, expiry, revocation, logout,
    and password recovery.
-   Do not store passwords.
-   Store session credentials only through a reviewed secure persistence
    strategy compatible with the selected SDK.
-   Biometric unlock is an optional local gate for an existing
    authenticated session, not a replacement for server authentication.
-   Verify Google OAuth redirect/deep-link configuration before marking
    Google login complete.

## 7. Offline-first specification

Advanced offline support is a significant feature and must be
implemented incrementally. Do not enable offline writes for every table
on day one.

### 7.1 Read strategy

1.  UI observes Room via Flow.
2.  Cached data appears immediately.
3.  Repository refreshes from Supabase when online.
4.  Remote data is validated/mapped and upserted into Room.
5.  Room emits updated data to the UI.

### 7.2 Write strategy

For a feature explicitly approved for offline writes: 1. Validate user
input locally. 2. Save the local change and outbox operation atomically
in Room. 3. Show a clear pending-sync state. 4. WorkManager attempts
synchronization when constraints are met. 5. Backend validates
authorization, schema, and business rules. 6. On success, store the
canonical server response and mark the operation synced. 7. On transient
failure, retry with bounded exponential backoff. 8. On permanent failure
or conflict, retain enough information to explain and resolve the issue.

### 7.3 Proposed local tables

These are Room tables only. They are not proposed Supabase tables.

  -----------------------------------------------------------------------
  Room table                          Purpose
  ----------------------------------- -----------------------------------
  Feature-specific cache tables       Cached records for approved
                                      offline-readable features

  `sync_outbox`                       Pending mutations

  `sync_metadata`                     Last successful sync/cursor

  `local_preferences` or DataStore    User settings; prefer DataStore for
                                      simple preferences
  -----------------------------------------------------------------------

Avoid caching sensitive financial/member data unless there is a clear
product requirement, appropriate device protection, and a defined
retention policy.

### 7.4 Outbox minimum fields

``` text
id
operation_id
account_scope
entity_type
entity_id
operation_type
payload
created_at
retry_count
sync_status
last_error
```

Use an idempotency identifier for every mutation. Ensure outbox records
are scoped to the authenticated account and cannot be replayed under
another account.

### 7.5 Conflict policy

-   Never blindly overwrite a newer server record with stale local data.
-   Use server timestamps or version checks where available.
-   If the current schema has no concurrency mechanism, design a
    backward-compatible backend change or a trusted RPC.
-   Treat authorization failures and validation errors as permanent
    until the user or data is corrected.
-   Retry network/timeouts and other transient errors with limits.
-   Handle remote deletion explicitly.
-   Do not queue money movement, donations, or other financially
    consequential operations for offline execution unless a separately
    reviewed, auditable transaction design exists. Initial policy:
    financial writes require online server confirmation.
-   WorkManager provides deferred eventual work, not exact-time
    execution.

### 7.6 Account switching and logout

-   Every cached row and outbox operation must be associated with an
    account scope.
-   On logout, stop user-specific subscriptions and background work.
-   Do not upload pending operations using a different user's session.
-   Define whether pending changes are safely retained encrypted for the
    same account or discarded after an explicit warning.
-   Clear sensitive cached data according to the approved retention
    policy.
-   Test account A logout followed by account B login.

## 8. UI implementation standards

### 8.1 Design system

Material 3 supplies accessible native building blocks. The app's custom
theme must reproduce the existing web brand and mobile layout.

Create: - Color tokens. - Typography tokens. - Spacing scale. -
Shape/radius tokens. - Elevation/shadow rules. - Icon mapping. -
Component variants. - Light/dark theme mapping, if applicable.

Do not invent brand colors or typography. Extract them from the actual
web CSS/design system.

### 8.2 Screen implementation checklist

For every screen: - Capture reference screenshot(s) from the web mobile
layout. - Record viewport width and relevant scroll position. - Map web
components to Compose components. - Implement all states. - Implement
every visible action. - Match role-based visibility. - Compare
screenshots at the same viewport/device conditions. - Test small and
large supported phone widths. - Record intentional platform differences.

### 8.3 Interaction mapping

  -----------------------------------------------------------------------
  Web behavior                        Native implementation candidate
  ----------------------------------- -----------------------------------
  Page navigation                     Navigation Compose

  Sidebar on mobile                   Modal navigation drawer or existing
                                      mobile navigation pattern

  Bottom tabs                         Material 3 NavigationBar, styled to
                                      match web

  Dropdown                            Exposed dropdown menu

  Modal dialog                        Material 3 Dialog

  Mobile action sheet                 Modal bottom sheet

  Pull-to-refresh                     Compose pull-to-refresh

  File/image selection                Android Photo Picker

  Camera capture                      CameraX when required

  Share action                        Android Sharesheet / Intent

  Browser/deep link                   Android App Links / intents where
                                      appropriate
  -----------------------------------------------------------------------

Do not change the user's workflow just to use a particular Material
component.

## 9. Native feature policy

Native capabilities should support parity, not distract from it.

  -----------------------------------------------------------------------
  Capability                          Initial policy
  ----------------------------------- -----------------------------------
  Push notifications                  Implement if web/backend
                                      notification events are understood

  Biometric                           After stable authentication

  WorkManager                         Sync queue and deferred work

  Exact reminders                     Evaluate separately; do not promise
                                      exact timing via WorkManager

  Photo Picker                        Preferred for initial profile image
                                      selection

  CameraX                             Later, if direct camera capture is
                                      required

  Share Intent                        Add where a web share workflow
                                      exists or native sharing improves
                                      it

  Maps                                Only if the web product has a real
                                      maps/location requirement

  Analytics                           Add with privacy-conscious event
                                      design

  Crashlytics                         Add before broader testing/release
  -----------------------------------------------------------------------

OpenStreetMap is map data, not a guarantee of free tile infrastructure.
Select a compliant tile provider or map SDK only after a concrete maps
requirement is confirmed.

## 10. Build, CI/CD, and distribution

### 10.1 Build variants

At minimum: - `debug`: development/staging configuration. - `release`:
production configuration.

Do not point debug builds at production data by default. Use a staging
Supabase project if available. If not, agree on a safe test-data
strategy before integration testing.

### 10.2 GitHub Actions

Initial CI should: 1. Check out repository. 2. Set up the selected JDK.
3. Set up Gradle cache. 4. Run formatting/static checks. 5. Run unit
tests. 6. Build debug APK. 7. Upload APK artifact.

Release workflow should additionally: 1. Run release tests/checks. 2.
Load signing material from GitHub Secrets. 3. Build signed APK. 4. Build
signed AAB. 5. Upload artifacts. 6. Never print secrets or write
keystore material into the repository.

### 10.3 Release requirements

-   Direct APK distribution for controlled testing.
-   AAB for Google Play.
-   Secure keystore backup and access policy.
-   Version code increment policy.
-   Release notes and changelog.
-   Privacy policy and Data Safety review before Play Store submission.
-   Test installation and upgrade paths on physical devices.

Google Play registration and any applicable account fees are separate
from the free local/CI build process.

## 11. Testing and acceptance criteria

### 11.1 Required test layers

-   Unit tests: use cases, mappers, ViewModels, sync rules.
-   Repository tests: local/remote coordination.
-   Database tests: DAO, migrations, account scoping.
-   UI tests: Compose navigation and interactions.
-   Integration tests: Supabase Auth and selected backend workflows.
-   Security tests: RLS, tenant isolation, account switching.
-   Offline tests: airplane mode, reconnect, duplicate retry, conflict,
    logout.
-   Physical-device tests: Android 10 minimum and representative newer
    versions.

### 11.2 MVP acceptance criteria

MVP is not complete until: - App installs and launches on API 29+. -
Login and logout work with existing web accounts. - Session restoration
and expiry are handled. - All agreed MVP screens match the mobile web
reference. - All agreed MVP workflows behave consistently with web. -
Role and permission behavior is consistent and enforced by backend. -
Search, filters, pagination, forms, and validation work where
applicable. - Local cache displays correctly offline. - Approved offline
writes synchronize safely. - Account switching does not leak or replay
another account's data. - No service-role key or server secret exists in
the app. - CI builds and tests pass. - Signed APK and AAB can be
produced.

## 12. Backlog and implementation order

### Phase 0 --- Discovery and parity audit (must precede business feature coding)

-   [ ] Create Android repository.
-   [ ] Inspect the existing web repository and identify all
    routes/modules.
-   [ ] Capture mobile-web screenshots and interaction recordings.
-   [ ] Build the complete web parity matrix.
-   [ ] Audit roles and permissions.
-   [ ] Audit Supabase schema, RLS, storage, realtime, RPC, and
    functions.
-   [ ] Map web features to actual backend resources.
-   [ ] Identify staging/production environment strategy.
-   [ ] Record unresolved questions and blockers.

**Exit criteria:** The team knows exactly which screens and workflows
must be built and which backend resources they use.

### Phase 1 --- Android bootstrap

-   [ ] Create Kotlin Android project.
-   [ ] Set application ID and minSdk 29.
-   [ ] Verify JDK/AGP/Gradle/Kotlin compatibility.
-   [ ] Configure Version Catalog.
-   [ ] Configure Compose and Material 3.
-   [ ] Add custom web-derived design tokens.
-   [ ] Configure Hilt.
-   [ ] Configure Navigation Compose.
-   [ ] Add basic app shell and sample screen.
-   [ ] Build and install first debug APK.
-   [ ] Add initial GitHub Actions CI.

**Exit criteria:** A native APK builds reproducibly and runs on a
physical device.

### Phase 2 --- Backend and authentication

-   [ ] Configure Supabase Kotlin SDK.
-   [ ] Configure Ktor and serialization.
-   [ ] Implement network/error handling.
-   [ ] Implement secure session persistence.
-   [ ] Implement login methods verified in the web audit.
-   [ ] Implement logout, expiry, refresh, and password recovery.
-   [ ] Verify RLS and account/tenant isolation.
-   [ ] Add authentication tests.

**Exit criteria:** Existing accounts can authenticate safely and the app
handles session lifecycle.

### Phase 3 --- Design system and navigation parity

-   [ ] Extract web design tokens.
-   [ ] Build typography, color, shape, and spacing tokens.
-   [ ] Build reusable Compose components.
-   [ ] Reproduce mobile navigation.
-   [ ] Build common loading/empty/error/offline states.
-   [ ] Screenshot-compare representative screens.

**Exit criteria:** The native shell visually follows the current
mobile-web experience.

### Phase 4 --- Offline foundation

-   [ ] Configure Room and migrations.
-   [ ] Create only the cache entities required by audited MVP features.
-   [ ] Implement DAOs and local repositories.
-   [ ] Implement sync metadata.
-   [ ] Implement account-scoped outbox.
-   [ ] Implement idempotent sync worker.
-   [ ] Implement retry and permanent-error handling.
-   [ ] Implement conflict detection for approved mutable entities.
-   [ ] Test reconnect and account switching.

**Exit criteria:** Offline reads and explicitly approved offline writes
are reliable and isolated.

### Phase 5 --- Feature parity modules

Build modules in the order established by the actual web audit. For
every module: - \[ \] Map web screen and workflows. - \[ \] Map backend
schema and permissions. - \[ \] Implement Compose UI. - \[ \] Implement
ViewModel and repository. - \[ \] Implement local cache where
approved. - \[ \] Implement remote synchronization. - \[ \] Implement
all states and validations. - \[ \] Test each role. - \[ \] Compare
visual and functional behavior with web. - \[ \] Mark the parity matrix
only after verification.

Initial candidate modules (confirm against actual web): - \[ \]
Dashboard - \[ \] Members - \[ \] Worship schedules - \[ \] Donations -
\[ \] Announcements - \[ \] Media - \[ \] Profile - \[ \]
Management/settings - \[ \] Global search and other existing web
workflows

### Phase 6 --- Native integrations

-   [ ] FCM device registration and token lifecycle.
-   [ ] Trusted backend notification sender.
-   [ ] Notification deep links.
-   [ ] WorkManager reminders/synchronization.
-   [ ] Photo Picker and profile upload.
-   [ ] Biometric local unlock.
-   [ ] Share Intent where required.
-   [ ] CameraX/maps only when confirmed by product requirements.

### Phase 7 --- QA and release

-   [ ] Unit, repository, database, and UI tests.
-   [ ] RLS and tenant-isolation testing.
-   [ ] Offline/conflict/account-switch testing.
-   [ ] Crashlytics setup.
-   [ ] Performance profiling.
-   [ ] Physical-device regression test.
-   [ ] Signed APK build.
-   [ ] Signed AAB build.
-   [ ] Internal testing.
-   [ ] Privacy and Play Store compliance review.

## 13. Suggested repository documentation

``` text
church-managements-android/
├── app/
├── core/
├── data/
├── domain/
├── feature/
├── docs/
│   ├── ANDROID_PARITY_IMPLEMENTATION.md
│   ├── WEB_PARITY_MATRIX.md
│   ├── BACKEND_INTEGRATION_AUDIT.md
│   ├── DATABASE_MAPPING.md
│   ├── PERMISSION_MATRIX.md
│   ├── OFFLINE_SYNC.md
│   ├── SECURITY.md
│   ├── DESIGN_SYSTEM.md
│   ├── ARCHITECTURE_DECISIONS.md
│   └── RELEASE_CHECKLIST.md
├── gradle/
│   └── libs.versions.toml
├── .github/
│   ├── workflows/
│   └── ISSUE_TEMPLATE/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── README.md
```

## 14. Git and issue conventions

Suggested branches: - `main`: production-ready code. - `develop`:
integration branch, if the team needs it. - `feature/<short-name>`:
feature implementation. - `fix/<short-name>`: bug fixes. -
`experiments`: optional experimentation branch.

Use the team's existing commit convention where practical. If using the
existing deployment-trigger convention, remember Android CI and Vercel
web deployment are separate pipelines; an Android commit does not deploy
the web app.

Issue labels: - `android` - `ui` - `backend` - `offline` - `security` -
`testing` - `ci-cd` - `priority:P0` - `priority:P1` - `priority:P2`

Every issue must include: - Context and linked web reference. - Exact
expected behavior. - Relevant role/permission. - Acceptance criteria. -
Test cases. - Dependencies/blockers.

## 15. Risks and mitigations

  -----------------------------------------------------------------------
  Risk                                Mitigation
  ----------------------------------- -----------------------------------
  Web feature list is incomplete      Complete route/workflow audit first

  Native UI drifts from web           Shared design tokens and screenshot
                                      comparison

  Backend schema guessed incorrectly  Audit actual Supabase schema before
                                      DTO/entity work

  Permissions differ                  Central permission matrix and RLS
                                      tests

  Offline queue duplicates writes     Idempotency keys and unique work

  Old cache overwrites newer data     Version/timestamp conflict
                                      detection

  Account data leaks locally          Account-scoped cache/outbox and
                                      switching tests

  Financial changes replay offline    Require online confirmation
                                      initially

  Dependency incompatibility          Verify version matrix during
                                      bootstrap

  Free-tier limits exceeded           Monitor usage and define upgrade
                                      thresholds
  -----------------------------------------------------------------------

## 16. Final implementation rules

1.  **Parity before enhancement.** Do not redesign or remove existing
    web behavior in the first release.
2.  **Audit before assumptions.** Actual web source and Supabase schema
    determine feature and data mapping.
3.  **Native implementation.** Use Compose, not a WebView wrapper.
4.  **One backend.** Reuse the existing Supabase project and business
    rules.
5.  **Secure by default.** RLS and server-side authorization are
    mandatory.
6.  **Offline incrementally.** Start with cache, then add offline writes
    per feature after backend support is verified.
7.  **Test every role.** A screen that works for an administrator may be
    wrong for staff or other users.
8.  **No fake completion.** A UI control is complete only when its real
    workflow works.
9.  **Keep web stable.** Android development must not break or
    unnecessarily modify the production web app.
10. **Document deviations.** Any difference from web parity must be
    intentional, explained, and approved.

------------------------------------------------------------------------

## 17. Immediate next actions

The next work should be done in this order:

1.  Create `church-managements-android` as a separate GitHub repository.
2.  Inspect the existing `church-managements` web repository and its
    current mobile UI.
3.  Complete `WEB_PARITY_MATRIX.md`.
4.  Audit the existing Supabase schema, RLS, Auth, Storage, Realtime,
    and server logic.
5.  Only then bootstrap the Android project and implement the first
    screen.

**Current status:** Configuration decisions are set. Project bootstrap
can begin. Full feature implementation is gated by the web and backend
audit.

**Product direction:** Church Managements Android is a native Kotlin
application that reproduces the existing mobile-web product first, then
adds Android-native capabilities without changing the underlying
business behavior.
