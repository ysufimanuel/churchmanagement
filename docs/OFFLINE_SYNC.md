# Offline synchronization plan

The first native increment establishes Room outbox metadata only. Feature caches and writes are enabled only after the corresponding web data contract and RLS behavior have been audited.

## Rules
- Room-backed Flow is the read source after feature cache implementation.
- Remote refresh validates and upserts canonical server records.
- Outbox mutations are account-scoped and church-scoped; each mutation has a unique idempotency operation ID.
- WorkManager retries transient failures with bounded exponential backoff.
- Authorization and validation failures are permanent until corrected.
- Conflicts must not overwrite newer server data.
- Financial writes are online-only and never enter the outbox.
- Logout/account switching stops subscriptions and prevents replay under another identity.

## Current state
- Room entity shapes for sync metadata and pending outbox are present.
- No business table cache or sync worker is enabled yet.
- Pending operation retention/discard policy and encrypted persistence require product/security review.
