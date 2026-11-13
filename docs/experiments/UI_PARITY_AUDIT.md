# Android UI parity audit

Reference:
- Web app: ysufimanuel/church-managements, branch experiments.
- Native app: ysufimanuel/churchmanagement, branch experiments (integrated from integrate-experiments).

## Scope of this pass

The web app's mobile dashboard is the visual source of truth. This pass adjusts the native Compose dashboard and navigation to follow the web mobile patterns without changing backend workflows.

| Area | Web mobile pattern | Native implementation in this pass | Status |
|---|---|---|---|
| Dashboard summary | Six compact tiles in a 3-column grid | Six compact Compose tiles in two 3-column rows, colored icon blocks and count-up values | IMPLEMENTED, screenshot verification pending |
| Dashboard charts | 88%-width horizontal snap rail | Horizontally scrollable chart rail with 88%-width cards | IN_PROGRESS, snap behavior and device comparison pending |
| Dashboard activity | One card with Recent donations / Upcoming events / Recent activity tabs | One native card with the same three tabs and four-row limit | IMPLEMENTED, content comparison pending |
| Bottom navigation | Floating glass dock, animated active state and icon feedback | Existing dock retained; active pill and icon press/selection motion added | IN_PROGRESS, gesture/lens behavior not ported |
| Brand tokens | Dark mesh background, cyan accent, glass surfaces, Plus Jakarta Sans | Existing native tokens and font retained | MAPPED, screenshot verification pending |
| Other feature screens | Mobile layouts in the web feature clients | Existing native feature screens retained | NOT_AUDITED |

## Motion mapping

- Web strong ease: cubic-bezier(0.23, 1, 0.32, 1); native uses EaseOutStrong.
- Dashboard number entrance: web count-up duration about 900 ms; native uses a 900 ms Compose tween.
- Dashboard reveal: native cards use the shared enterAnim modifier.
- Bottom navigation: active surface and icon scale animate; press feedback uses the shared pressScale and iconFx.
- Reduced-motion support and exact animation timing still require device validation.

## Verification still required

1. Build the APK and run lint/unit tests on the updated experiments branch.
2. Capture web and native screenshots at the same viewport/device dimensions and compare all dashboard scroll positions.
3. Compare every feature screen, dialog, sheet, loading/empty/error state, and role-specific state.
4. Validate touch targets, keyboard resize, system bars, and navigation gesture insets on Android.
5. Only mark a feature VERIFIED after visual, interaction, data, and permission checks.

This audit intentionally does not claim 1:1 parity yet. A source-level pass is not a substitute for a real-device screenshot and workflow comparison.
