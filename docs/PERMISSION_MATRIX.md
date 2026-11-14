# Permission matrix

Confirmed web roles: `user`, `admin`, `superadmin`.

| Capability | user | admin | superadmin |
|---|---:|---:|---:|
| View | Yes | Yes | Yes |
| Add | No | Yes | Yes |
| Edit | No | Yes | Yes |
| Delete | No | No | Yes |
| Finance approve/reject | No | No | Yes |
| Manage users | No | No | Yes |
| Web Manager | No | Yes | Yes |

Finance behavior from `src/lib/actions/finance.ts`: admin-created transactions are pending; superadmin-created transactions auto-approved; approval/rejection calls `process_finance_transaction`; only superadmin can process or delete transactions.

This is the web application's role baseline, not a substitute for live RLS enforcement. Audit each table policy and action before release.
