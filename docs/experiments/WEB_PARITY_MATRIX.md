# WEB_PARITY_MATRIX
Source: web repo `church-managements` (branch `experiments`). Status: DONE = implemented, **not compile- or device-verified**; PARTIAL; BLOCKED = deliberately not implemented.

| Module | Status | Notes |
|---|---|---|
| Login (email) / logout / session | DONE | Supabase Auth, anon key only |
| Login by username | BLOCKED | Needs server resolver; works only if `LOGIN_RESOLVER_URL` is set |
| Register church | PARTIAL | Opens web link (needs service role) |
| Dashboard (6 stats, 3 charts, activity, events, donations) | DONE | Same 10 queries as web |
| Members / Families / Groups | DONE | CRUD, filters, export, group member mgmt |
| Events + participants | DONE | Manual participants supported |
| Attendance (check-in, data, report) | DONE | UTC date key like web |
| Donations + donors | DONE | Donor totals refresh |
| Finance (6 tabs, approve/reject RPC, opening balance) | DONE | Writes need connectivity |
| Volunteers (list/schedule/assignments) | DONE | |
| Communication (broadcast, realtime chat, announcements) | DONE | Broadcast recipients = all (backend RPC limit) |
| Reports + export (CSV/XLS/TXT/PDF-print) | DONE | Sharesheet; PDF via print renderer |
| Notifications (realtime) | DONE | No FCM push |
| Global search | PARTIAL | Chat-message search omitted |
| Profile (photo, name, password; demo lock) | DONE | |
| Users | PARTIAL | Read-only list; create/delete need service role |
| Web Manager | DONE (native) | Editor Visual, Header & Navigasi, Tema, Section Lanjutan, publish via RPC (anon key + RLS). Limits: preview shows the published site in a WebView; Draft preview opens in the browser (needs web login); no drag-drop WYSIWYG canvas (up/down reorder instead); color picker is HSV+hex, no alpha |
| i18n id/en | DONE | Dictionaries generated from web + `extra-*.json` for Android-only strings |
| UI | PARTIAL | Dark tokens from web CSS; no backdrop-blur (translucent gradient); tab styling approximated |
| Offline (Room) / FCM / Crashlytics | NOT YET | See OFFLINE_SYNC.md |
