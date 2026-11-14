# BACKEND_INTEGRATION_AUDIT
- Same Supabase project as the web; only the **anon** key is shipped (Gradle props `SUPABASE_URL`, `SUPABASE_ANON_KEY`).
- No schema changes were made. Column names were taken from the web code; they must be re-verified against the live schema (`list_tables`) before release.
- Attendance and group_members use check-then-write instead of upsert (supabase-kt upsert API unverified).
- Open items: confirm RLS on every table for the `user`/`admin` roles; confirm the `avatars` bucket policy; confirm RPC signatures.
