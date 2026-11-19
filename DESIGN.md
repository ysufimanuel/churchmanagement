# Church Management — Liquid Glass Bottom Navigation

> Design specification based on the supplied 25.9-second SwiftUI Instagram tab-bar reference.
> Scope: bottom navigation component and its interaction states. The reference demonstrates a tab bar, not the full application UI.

## 1. Design intent

Create a floating, pill-shaped bottom navigation dock with a moving active indicator. The dock should feel light and tactile: the glass container stays in place while the selected-state bubble travels horizontally to the tapped destination.

The implementation target is **Kotlin + Jetpack Compose**. Preserve Church Management's existing navigation routes, destinations, dark theme, and cyan accent unless the app's current design tokens specify otherwise.

## 2. What is visible in the reference

- A compact floating navigation capsule near the bottom edge of the phone.
- A pale, translucent-looking rounded container with fully rounded ends.
- Five evenly spaced icon destinations.
- A circular/pill-like selected background behind the active icon.
- On selection, the active background moves to another destination rather than each item independently displaying a static selected block.
- The selected icon appears visually emphasized against the inactive icons.
- The selected indicator and icon state change with a smooth animated transition.
- The dock remains anchored while the content above it changes.
- The video does not establish the exact blur radius, opacity, spring constants, haptic type, or accessibility behavior. Those are implementation decisions, not facts from the reference.

## 3. Adaptation for Church Management

Use the same interaction pattern, but retain Church Management's own information architecture.

Suggested destinations, subject to the routes already present in the codebase:

1. Dashboard
2. Finance
3. Search
4. Web Manager (only when enabled/available in the current build)
5. Menu

Do not add, remove, or reorder destinations without checking the existing route model. If Web Manager is conditionally hidden, calculate item positions from the visible destinations so the active indicator always aligns correctly.

## 4. Component anatomy

`LiquidGlassBottomBar`
- Floating rounded container.
- Subtle translucent surface.
- Thin low-contrast outline.
- Soft elevation/shadow separating it from page content.
- Horizontal row of equal-width destination hit areas.

`MovingSelectionIndicator`
- One shared animated shape, positioned beneath the selected destination.
- Rounded/circular shape sized to the visual treatment.
- Moves between item centers when selection changes.
- Must not be implemented as five unrelated selected backgrounds.

`NavigationItem`
- Icon centered in a consistent hit area.
- Optional short label only if the current app navigation already uses labels.
- Active icon uses the active tint and a modest scale increase.
- Inactive icons use a lower-emphasis tint.
- Pressed feedback is separate from the selected state.

## 5. Visual tokens

Use existing project design tokens as the source of truth. The following are behavioral and visual targets, not hard-coded color mandates.

| Token | Target |
|---|---|
| Container shape | Fully rounded capsule |
| Container surface | Translucent / glass-like |
| Container border | Very subtle, low contrast |
| Container elevation | Soft, restrained |
| Active indicator | Rounded capsule or circle |
| Active icon | Existing primary accent |
| Inactive icon | Muted foreground |
| Item width | Equal distribution |
| Touch target | At least 48 dp in both dimensions |
| Dock placement | Floating above system navigation/gesture inset |

For the current dark Church Management theme, keep the dock readable against both dark surfaces and scrolling content. Avoid a bright white bar if it clashes with the app's dark visual system. A dark translucent surface with a restrained cyan active treatment is the safer adaptation.

## 6. Interaction and motion specification

### Tap destination
1. Accept the tap across the full item hit area, not only the icon glyph.
2. Navigate to the destination using the existing navigation controller.
3. Animate the shared selection indicator from the previous item center to the new item center.
4. Animate the selected icon to a modestly larger scale.
5. Animate active/inactive tint and optional label emphasis.
6. Trigger a light selection haptic where supported.
7. Prevent rapid repeated taps from creating duplicate navigation destinations.

### Motion character
- Use a spring-like transition with a small amount of natural overshoot, not a bouncy toy effect.
- Indicator movement should be the dominant motion.
- Icon scale and tint should follow the indicator, not compete with it.
- Keep the dock's outer container stationary.
- Avoid animating the entire bottom bar's width or vertical position during ordinary tab changes.

### Press state
- On touch-down, apply a subtle scale or tonal response to the pressed item.
- On release, settle into the selected or inactive state.
- Press feedback must not be confused with selection: a pressed inactive item should not look permanently selected.

### Content transition
- The reference video focuses on the tab bar. It does not provide enough evidence to prescribe a specific page-transition animation.
- Keep the existing content/navigation transition unless a separate reference is supplied.

## 7. Compose implementation guidance

Recommended building blocks:
- `Row` for equal-width navigation items.
- `Box` for the dock surface and overlay indicator.
- `Modifier.onSizeChanged` or measured item coordinates to determine item centers.
- `Animatable` or `animateDpAsState` for indicator offset.
- `animateFloatAsState` for icon scale.
- `animateColorAsState` for selected and unselected tint.
- `MutableInteractionSource` and `collectIsPressedAsState` for press feedback.
- `LocalHapticFeedback` for selection feedback.
- `WindowInsets` / navigation-bar inset handling to keep the dock clear of system gestures.

Implementation rules:
- Use stable destination IDs/routes, not list indices as navigation identity.
- Derive indicator position from the currently visible destination list.
- Recompute positions when the number or width of visible items changes.
- Keep navigation state as the single source of truth. Do not maintain a second independent selected-tab state that can drift from the navigation back stack.
- Preserve accessibility semantics: each item needs a meaningful content description, selected state, and a sufficiently large touch target.
- Respect reduced-motion/accessibility settings where practical.
- Ensure the indicator does not intercept taps; destination hit targets should remain clickable.

## 8. Suggested motion values

Starting points for implementation and tuning, not values measured from the reference:

| Property | Suggested starting point |
|---|---:|
| Indicator travel | 300–420 ms |
| Icon scale | 1.00 → 1.10 |
| Press scale | 1.00 → 0.94 |
| Corner radius | Fully rounded |
| Haptic | Light selection feedback |
| Spring | Medium stiffness, low-to-medium damping |

Tune on a real Android device. Avoid excessive bounce, long delays, or motion that makes tab switching feel slower than the content itself.

## 9. Responsive behavior

- Keep equal spacing across all currently visible destinations.
- Maintain minimum 48 dp touch targets.
- Respect display cutouts and bottom system insets.
- Avoid placing the dock over important scroll content; provide bottom content padding equal to dock height plus safe inset.
- Test narrow screens and larger font/display scaling.
- If a destination is conditionally hidden, animate/recalculate the indicator against the new visible item positions.

## 10. Acceptance criteria

- [ ] Dock floats above the bottom edge and has a capsule silhouette.
- [ ] Dock remains stationary when changing tabs.
- [ ] Exactly one shared active indicator is visible.
- [ ] Indicator glides to the selected destination.
- [ ] Active icon receives a restrained scale/tint emphasis.
- [ ] Press feedback works on the full touch target.
- [ ] Pressed state and selected state are visually distinct.
- [ ] Navigation still uses the existing routes and back-stack behavior.
- [ ] Conditional destinations do not misalign the indicator.
- [ ] Dock does not overlap system gesture navigation or page content.
- [ ] All items expose accessible labels and selected semantics.
- [ ] No layout jump, clipping, or asymmetric spacing during animation.
- [ ] Motion remains smooth during rapid tab changes.

## 11. Explicit non-goals

This reference does **not** define:
- Dashboard card layout or dashboard content.
- Finance screen layout.
- Search screen layout.
- Menu screen layout.
- Full-screen page transitions.
- Exact colors, blur values, or typography for the whole application.

Those should be specified from separate screen references or the existing Church Management design system rather than guessed from a bottom-navigation demo.

## 12. Reference summary

The key idea to reproduce is the **moving selected glass bubble inside a stationary floating capsule**. The interaction should feel continuous: tap → indicator glides → icon emphasizes → destination changes. The visual effect is successful only if it remains readable, responsive, and consistent with Church Management's existing navigation architecture.
