# Web parity matrix

Source repository: `ysufimanuel/church-managements`, branch `experiments`.
Audit date: 2026-11-12. Statuses are conservative; no feature is marked VERIFIED until it passes device, data, and permission tests.

## Current verified baseline

- APK installs and email/password login has been confirmed by the project owner on a physical Android device.
- Supabase project endpoint and public anon key are configured in the Android build.
- Android currently restores an authenticated session and reads the signed-in user's `profiles` row.
- Android dashboard currently renders a module menu only. Module cards are not yet connected to feature screens or data workflows. Do not describe the app as feature-complete.
- The screenshot supplied on 2026-11-12 is the visual reference for the post-login module-menu screen.

## Visual parity requirements (mobile)

| Area | Web/mobile reference | Android acceptance criteria | Status |
|---|---|---|---|
| Overall canvas | `src/app/globals.css` | Background `#0B0D12`, same mesh/gradient atmosphere, edge-to-edge with safe insets | MAPPED |
| Surfaces | `.glass`, `.glass-strong`, `.glass-subtle` | Translucent dark cards, thin white border, equivalent blur/shadow and radius | MAPPED |
| Brand/header | Dashboard shell + mobile navigation | Same title/subtitle, spacing, logout action and cyan icon treatment | IN_PROGRESS |
| Welcome panel | Supplied Android screenshot | Same gradient direction/colors, border, greeting, display name and role chip | IN_PROGRESS |
| Module list | Supplied Android screenshot + `src/components/dashboard/nav-items.ts` | Same order, icon meaning, title/description, card height, margins, chevron and tap target | IN_PROGRESS |
| Typography | Web CSS and component styles | Match web font family, weights, sizes, line-height, letter spacing and wrapping | NOT_AUDITED |
| Motion | `src/app/globals.css`, `src/components/dashboard/mobile-nav.tsx`, dashboard GSAP animations | Equivalent entrance, press, tab/sheet and list transitions; honor reduced-motion | NOT_AUDITED |
| Responsive behavior | `.dash-compact` rules at <=639px | Match phone viewport, scroll behavior, safe areas, keyboard resize and landscape fallback | NOT_AUDITED |
| Loading/error/empty | Per-module loading and error states | Match web skeletons, error feedback, empty states and retry behavior | NOT_AUDITED |

## Feature parity matrix

| Module / workflow | Web source / actions | Expected behavior | Roles / constraints | Status |
|---|---|---|---|---|
| Login/session | `src/app/login/page.tsx`, `src/lib/actions/auth.ts` | Email/password, session restore, sign out; username requires trusted lookup endpoint | Never ship service-role key | IN_PROGRESS |
| Dashboard | `src/app/dashboard/page.tsx`, `src/components/dashboard/dashboard-home.tsx` | Stats, attendance chart, donation trend, member distribution, recent activity, upcoming events, recent donations | Authenticated, church-scoped data | MENU_ONLY |
| Members | `src/components/dashboard/members-client.tsx`, `src/lib/actions/members.ts` | Search/filter, add/edit/delete, details | user read; admin add/edit; superadmin delete | MAPPED |
| Families | `src/components/dashboard/families-client.tsx`, `src/lib/actions/families.ts` | Family CRUD and member association | user read; admin add/edit; superadmin delete | MAPPED |
| Groups | `src/components/dashboard/groups-client.tsx`, `src/lib/actions/groups.ts` | Group CRUD and add/remove members | user read; admin add/edit; superadmin delete | MAPPED |
| Events / worship | `src/components/dashboard/events-client.tsx`, `src/lib/actions/events.ts` | Event CRUD and participant management | user read; admin add/edit; superadmin delete | MAPPED |
| Attendance | `src/components/dashboard/attendance-client.tsx`, `src/lib/actions/attendance.ts` | Member check-in and attendance history | check-in permission; server/RLS authoritative | MAPPED |
| Donations | `src/components/dashboard/donations-client.tsx`, `src/lib/actions/donations.ts` | Donor/donation records, search and filters | Financial writes online only | MAPPED |
| Finance | `src/components/dashboard/finance-client.tsx`, `src/lib/actions/finance.ts` | Transactions, categories, opening balance, approve/reject | admin creates; superadmin approves/rejects/deletes; online atomic workflow only | MAPPED |
| Volunteers | `src/components/dashboard/volunteers-client.tsx`, `src/lib/actions/volunteers.ts` | Volunteer CRUD and assignments | user read; admin add/edit; superadmin delete | MAPPED |
| Communication | `src/components/dashboard/communication-client.tsx`, `src/lib/actions/communication.ts` | Messages, announcements, broadcast, read state | role-gated writes; online delivery | MAPPED |
| Reports | `src/components/dashboard/reports-client.tsx` | Report views, filtering, export/print | respect role and church scope | MAPPED |
| Users | `src/components/dashboard/users-client.tsx`, `src/lib/actions/users.ts` | User list, create/update/delete and role assignment | superadmin only; trusted Admin API, never client service key | MAPPED |
| Web Manager | `src/components/dashboard/web-manager-client.tsx`, `src/lib/actions/webmanager.ts` | Navigation, theme, section drafts and publishing | admin/superadmin per web policy | MAPPED |
| Profile/preferences | `src/components/dashboard/profile-modal.tsx`, profile actions | Own profile/preferences and avatar policy | user may edit own profile only | MAPPED |
| Notifications | `src/components/dashboard/notification-bell.tsx` | User-scoped notification list/read state and realtime updates | authenticated user scope | MAPPED |
| Global search | `src/components/search/global-search.tsx` and search actions | Search actual table data and role-allowed features | role-filtered; never leak cross-church records | NOT_AUDITED |

## Implementation order

1. Match the supplied post-login menu screenshot: layout, colors, typography, icon assets, spacing, safe areas and motion.
2. Build shared native navigation, loading/error/empty states, search, dialogs, form validation and permission-aware action controls.
3. Implement data-backed modules in web order: members/families/groups, events/attendance, donations/finance, volunteers/communication, reports, users, Web Manager.
4. Implement profile, notifications, and global search.
5. Validate each module against the web on physical Android devices with representative `user`, `admin`, and `superadmin` accounts.

## Security and offline rules

- Supabase RLS and trusted server endpoints remain authoritative; client-side role checks are presentation only.
- Never embed Supabase service-role credentials in the APK.
- Read-only/cacheable data may be available offline with explicit freshness indicators.
- Financial writes, approvals/rejections, user administration, broadcasts, and other privileged mutations must remain online and go through trusted/atomic backend workflows.
- Never claim parity from a successful APK build alone.

A feature becomes VERIFIED only after screenshot comparison, real workflow testing, role/permission testing, and data verification on a physical Android device.
