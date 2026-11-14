package com.growthaxys.churchmanagements.core

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class RbacTest {
    @Test fun userIsReadOnly() { assertFalse(Rbac.canAdd(Role.USER)); assertFalse(Rbac.canEdit(Role.USER)); assertFalse(Rbac.canDelete(Role.USER)) }
    @Test fun adminCanAddEditButNotDeleteOrApprove() {
        assertTrue(Rbac.canAdd(Role.ADMIN)); assertTrue(Rbac.canEdit(Role.ADMIN))
        assertFalse(Rbac.canDelete(Role.ADMIN)); assertFalse(Rbac.canApprove(Role.ADMIN))
    }
    @Test fun superadminCanEverything() {
        assertTrue(Rbac.canDelete(Role.SUPERADMIN)); assertTrue(Rbac.canApprove(Role.SUPERADMIN)); assertTrue(Rbac.canManageUsers(Role.SUPERADMIN))
    }
    @Test fun unknownRoleFallsBackToUser() { assertTrue(Role.from("???") == Role.USER) }
}
