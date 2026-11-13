# Architecture decisions

## ADR-001 — Native Compose, not WebView
Accepted. Reproduce mobile-web workflows in Jetpack Compose, styled from extracted web tokens.

## ADR-002 — Existing Supabase project
Accepted. Reuse the existing production project and RLS. Do not create another production database.

## ADR-003 — Multi-tenant local isolation
Accepted. Room cache/outbox records must be scoped by account and church.

## ADR-004 — Offline finance
Accepted. Financial mutations require online server confirmation and are never queued.

## ADR-005 — Username login
Blocked pending trusted backend endpoint. The web uses a server-only service-role lookup; Android must not expose it as an unauthenticated query.
