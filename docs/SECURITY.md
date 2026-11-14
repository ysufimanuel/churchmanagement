# Android security baseline

- Only Supabase publishable key in the app; never service-role or secret keys.
- RLS is authoritative. Role checks in Compose are presentation only.
- Never log tokens, passwords, member PII or donation information.
- HTTPS only.
- Scope local data and pending operations to authenticated account and church.
- Logout/account switching must stop subscriptions and prevent cross-account cache/outbox reuse.
- Do not queue financial writes for offline replay.
- Username-to-email lookup requires a trusted backend endpoint.
- Review secure session persistence and native OAuth redirects before production.
