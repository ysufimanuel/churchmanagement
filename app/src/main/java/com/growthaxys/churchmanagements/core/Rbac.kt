package com.growthaxys.churchmanagements.core

/**
 * Mirrors web `src/lib/rbac.ts`.
 *  user       -> view only
 *  admin      -> view + add + update, finance create (pending), NO delete
 *  superadmin -> full access + finance approval + user management
 *
 * These checks only control UI visibility. Supabase RLS stays the authority.
 */
enum class Role(val raw: String) {
    USER("user"), ADMIN("admin"), SUPERADMIN("superadmin");

    companion object {
        fun from(raw: String?): Role = entries.firstOrNull { it.raw == raw } ?: USER
    }
}

object Rbac {
    fun canAdd(role: Role) = role == Role.ADMIN || role == Role.SUPERADMIN
    fun canEdit(role: Role) = role == Role.ADMIN || role == Role.SUPERADMIN
    fun canDelete(role: Role) = role == Role.SUPERADMIN
    fun canApprove(role: Role) = role == Role.SUPERADMIN
    fun canManageUsers(role: Role) = role == Role.SUPERADMIN
    fun canUseWebManager(role: Role) = role == Role.ADMIN || role == Role.SUPERADMIN

    fun roleLabel(role: Role) = when (role) {
        Role.SUPERADMIN -> "Super Admin"
        Role.ADMIN -> "Admin"
        Role.USER -> "User (View Only)"
    }
}
