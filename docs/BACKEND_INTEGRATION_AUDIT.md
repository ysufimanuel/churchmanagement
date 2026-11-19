# Backend integration audit

Supabase project: `church-managements` (`bpubxeohlsskvsuhsqcw`), region `ap-south-1`, PostgreSQL 17, status ACTIVE_HEALTHY. Audited 2026-11-11.

## Confirmed
- Live public table metadata retrieved. All discovered public tables reported RLS enabled.
- Production is multi-tenant: profiles and business records carry `church_id`.
- Profile roles: `user`, `admin`, `superadmin`.
- Migration history includes initial schema, multi-tenant RBAC, Web Manager draft/publish, tenant-consistent foreign keys, RLS optimization, atomic finance/broadcast workflows and avatar bucket.

## Critical findings
1. Web repository `supabase/schema.sql` explicitly identifies itself as LEGACY BASELINE ONLY. It is not a production bootstrap source.
2. Never ship service-role/secret keys in Android.
3. RLS and trusted server operations remain authoritative; UI role checks only control presentation.
4. Cache/outbox must be scoped to account and church.
5. Web username login performs username-to-email lookup in a Next.js server action with service-role privileges. Android must not reproduce that as a public profile query. A trusted backend lookup is required.
6. Google OAuth/deep-link configuration is not yet verified for Android.
7. Storage bucket policy/limits, detailed RLS semantics, realtime publication membership and RPC signatures still require audit.
8. Finance mutations must remain online and use the atomic backend workflow.

## Migration history observed
Initial schema/chat/finance/notification/audit migrations; multi-tenant church RBAC; Web Manager draft/navigation/theme/media/publish; tenant-consistent foreign keys and optimized RLS; atomic finance approval and broadcast delivery; final Web Manager deletion fixes; profile avatars bucket.

No Supabase schema or production data was changed during this audit.
