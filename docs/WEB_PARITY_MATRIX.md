# Web parity matrix

Source repository: `ysufimanuel/church-managements`, branch `experiments`.
Audit date: 2026-11-11. Statuses are conservative; nothing is VERIFIED.

| Module / workflow | Web source | Android target | Roles / key behavior | Status |
|---|---|---|---|---|
| Login/session | `src/app/login/page.tsx`, `src/lib/actions/auth.ts` | Compose LoginScreen/AuthRepository | Email/password; username lookup needs trusted endpoint | IN_PROGRESS |
| Dashboard | `src/app/dashboard/page.tsx`, DashboardHome | Compose dashboard shell only | Authenticated roles; tenant-filtered data | MAPPED |
| Members | `src/app/dashboard/members`, `src/lib/actions/members.ts` | Pending | user read; admin add/edit; superadmin delete | MAPPED |
| Families | `src/app/dashboard/families` | Pending | inspect exact actions | MAPPED |
| Groups | `src/app/dashboard/groups` | Pending | inspect exact actions | MAPPED |
| Events / worship | `src/app/dashboard/events` | Pending | inspect participant workflows | MAPPED |
| Attendance | `src/app/dashboard/attendance` | Pending | check-in, history, delete policy | MAPPED |
| Donations | `src/app/dashboard/donations` | Pending | online-only financial mutations | MAPPED |
| Finance | `src/app/dashboard/finance`, `src/lib/actions/finance.ts` | Pending | admin creates pending; superadmin approves/rejects | MAPPED |
| Volunteers | `src/app/dashboard/volunteers` | Pending | volunteer CRUD and assignments | MAPPED |
| Communication | `src/app/dashboard/communication` | Pending | messages, announcements, broadcast, realtime | MAPPED |
| Reports | `src/app/dashboard/reports` | Pending | exports/print | MAPPED |
| Users | `src/app/dashboard/users`, `src/lib/actions/users.ts` | Pending | superadmin only; trusted Admin API | MAPPED |
| Web Manager | `src/app/dashboard/web-manager` | Pending | admin/superadmin; draft/publish | MAPPED |
| Profile/preferences | profile actions, `user_preferences` | Pending | own profile; avatar policy to verify | MAPPED |
| Notifications | dashboard notification components | Pending | user-scoped realtime | MAPPED |
| Global search | dashboard search components | Pending | searchable table data, role filtered | NOT_AUDITED |

## Visual tokens from web CSS
- Background `#0B0D12`; card `#1A1D24`; cyan `#00E5FF`; purple `#7C5CFF`.
- White foreground, slate muted text, translucent glass surfaces and thin white borders.
- Mobile compact breakpoint <=639px: horizontally snapping stat cards and table-to-card layout.

A feature can become VERIFIED only after screenshot comparison, workflow testing, permission testing and data verification on representative roles.
