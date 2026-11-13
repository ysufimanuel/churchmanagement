# Database mapping — live production metadata

Supabase project: `church-managements`, ref `bpubxeohlsskvsuhsqcw`. Public metadata captured 2026-11-11; every discovered table reported RLS enabled.

| Table | Key fields / notes |
|---|---|
| profiles | id uuid, nama, username, email, role app_role, avatar_url, status, church_id |
| churches | id uuid, slug, nama and church contact/address fields |
| church_profile | church_id, nama, address/contact fields |
| members | id bigint, nama/contact, jk, birth/address, status, group_id, church_id |
| families / family_members | family records and member relation, church_id |
| groups / group_members | group, leader_id, schedule, show_phone, church_id |
| events / event_participants | event timing/status/location and participants, church_id |
| attendance | event_id, member_id, tanggal, waktu, status, church_id |
| donors / donations | donor information, amount/type/date, church_id |
| volunteers / volunteer_assignments | internal/external volunteer, schedules and event assignment, church_id |
| announcements | judul, konten, tanggal, expiry, important, author_id, church_id |
| messages | sender_id, receiver_id, content, read, church_id |
| broadcasts | subject, message, recipients, group_ids, status, created_by, church_id |
| activities / notifications | activity and notification records, church_id |
| finance_settings / finance_categories / finance_transactions / approval_history | finance settings, categories, status, approver/history, church_id |
| site_nav_items / site_theme / site_media / site_sections | Web Manager draft/published records, church_id |
| user_preferences | user_id, preferences jsonb |

Preserve PostgreSQL UUID, bigint, numeric, date, timestamptz, enum, jsonb and array types. Never infer production columns from the legacy SQL file. Local caches must preserve church/account scope. Financial writes are not queued offline.
