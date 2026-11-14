# DATABASE_MAPPING
Tables used (all tenant-scoped by `church_id`, inserted from the caller's profile): profiles, members, families, groups, group_members, events, event_participants, attendance, donors, donations, volunteers, volunteer_assignments, announcements, messages, notifications, activities, finance_transactions, finance_categories, finance_settings (id=1), approval_history.
RPCs: `process_finance_transaction(p_transaction_id,p_action)`, `create_broadcast_with_notifications(p_subject,p_message,p_recipients,p_send)`.
Storage: avatars (profile photo, resized to ≤512px JPEG). Realtime: `messages`, `notifications` inserts.
DTOs: `data/model/Models.kt`; repositories: `data/repo/*`.
