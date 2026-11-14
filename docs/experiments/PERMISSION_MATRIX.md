# PERMISSION_MATRIX (mirrors web `src/lib/rbac.ts`; RLS remains authoritative)
| Action | user | admin | superadmin |
|---|---|---|---|
| View / export | yes | yes | yes |
| Add | no | yes | yes |
| Edit | no | yes | yes |
| Delete | no | no | yes |
| Approve/reject finance | no | no | yes |
| Finance entry status | — | pending | approved |
| Manage users (list) | no | no | yes |
UI gating is convenience only; server errors are surfaced as toasts.
