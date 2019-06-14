/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.app.designtime.RoleBuilder;
import com.haulmont.cuba.security.entity.*;
import com.haulmont.cuba.testsupport.TestContainer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class RoleBuilderTest {

    protected Metadata metadata;

    @ClassRule
    public static TestContainer cont = TestContainer.Common.INSTANCE;

    @Before
    public void setUp() throws Exception {
        metadata = cont.metadata();
    }

    @Test
    public void createNewRole() {
        OrdinaryRole role = RoleBuilder.createRole().build();

        assertNotNull(role);
        assertEquals(RoleType.STANDARD, role.getRoleType());
        assertEquals(0, role.entityAccess().getPermissions().size());
        assertEquals(0, role.attributeAccess().getPermissions().size());
        assertEquals(0, role.specificPermissions().getPermissions().size());
        assertEquals(0, role.screenAccess().getPermissions().size());
        assertEquals(0, role.screenElementsAccess().getPermissions().size());
    }

    @Test
    public void createRoleWithDuplicatePermissions() {
        OrdinaryRole role = RoleBuilder.createRole()
                .withName("testRole")
                .withScreenPermission("sec$Role.browse", AccessOperation.ALLOW)
                .withScreenPermission("sec$Role.browse", AccessOperation.ALLOW)
                .withScreenPermission("sec$Role.browse", AccessOperation.DENY)
                .build();

        assertEquals("testRole", role.getName());
        assertEquals(1, role.screenAccess().getPermissions().size());
        assertEquals(1, (Object) role.screenAccess().getPermissionValue("sec$Role.browse"));
    }

    @Test
    public void createOrdinaryRoleBasedOnRoleEntity() {
        Role roleEntity = createRoleEntityWithPermissions();

        OrdinaryRole role = RoleBuilder.createRole(roleEntity).build();

        assertEquals("roleEntity", role.getName());
        assertEquals(RoleType.DENYING, role.getRoleType());
        assertFalse(role.isDefault());
        assertEquals("test description", role.getDescription());
        assertEquals(0, role.entityAccess().getPermissions().size());
        assertEquals(0, role.attributeAccess().getPermissions().size());
        assertEquals(0, role.screenAccess().getPermissions().size());
        assertEquals(0, role.screenElementsAccess().getPermissions().size());
        assertEquals(2, role.specificPermissions().getPermissions().size());
        assertEquals(1, (Object) role.specificPermissions().getPermissionValue("specificPermission1"));
        assertEquals(0, (Object) role.specificPermissions().getPermissionValue("specificPermission2"));
    }

    @Test
    public void joinRole() {
        OrdinaryRole role1 = RoleBuilder.createRole()
                .withPermission(PermissionType.SCREEN, "sec$Role.browse", 1)
                .build();

        OrdinaryRole role2 = RoleBuilder.createRole(RoleType.DENYING)
                .withName("ordinaryRole")
                .withDescription("description")
                .withPermission(createPermission(null, PermissionType.SPECIFIC, "specificPermission3", 1))
                .join(createRoleEntityWithPermissions())
                .join(role1)
                .build();

        assertEquals("ordinaryRole", role2.getName());
        assertEquals(RoleType.DENYING, role2.getRoleType());
        assertFalse(role2.isDefault());
        assertEquals("description", role2.getDescription());
        assertEquals(0, role2.entityAccess().getPermissions().size());
        assertEquals(0, role2.attributeAccess().getPermissions().size());
        assertEquals(1, role2.screenAccess().getPermissions().size());
        assertEquals(0, role2.screenElementsAccess().getPermissions().size());
        assertEquals(3, role2.specificPermissions().getPermissions().size());
        assertEquals(1, (Object) role2.specificPermissions().getPermissionValue("specificPermission1"));
        assertEquals(0, (Object) role2.specificPermissions().getPermissionValue("specificPermission2"));
        assertEquals(1, (Object) role2.specificPermissions().getPermissionValue("specificPermission3"));
        assertEquals(1, (Object) role2.screenAccess().getPermissionValue("sec$Role.browse"));
    }

    @Test
    public void createRoleWithMultiplePermissions() {
        OrdinaryRole role = RoleBuilder.createRole()
                .withName("role")
                .withRoleType(RoleType.STANDARD)
                .withEntityAccessPermission(metadata.getClassNN(User.class), EntityOp.CREATE, AccessOperation.ALLOW)
                .withEntityAttrAccessPermission(metadata.getClassNN(User.class), "login", EntityAttrAccess.MODIFY)
                .withSpecificPermission("specificPermission1", AccessOperation.ALLOW)
                .withScreenPermission("sec$Role.browse", AccessOperation.ALLOW)
                .withScreenElementPermission("sec$Role.browse", "roleGroupBox", AccessOperation.ALLOW)
                .withPermission(createPermission(null, PermissionType.SPECIFIC, "specificPermission2", 1))
                .withPermission(PermissionType.SPECIFIC, "specificPermission3", 1)
                .build();

        assertEquals("role", role.getName());
        assertEquals(RoleType.STANDARD, role.getRoleType());
        assertEquals(1, role.entityAccess().getPermissions().size());
        assertEquals(1, role.attributeAccess().getPermissions().size());
        assertEquals(1, role.screenAccess().getPermissions().size());
        assertEquals(1, role.screenElementsAccess().getPermissions().size());
        assertEquals(3, role.specificPermissions().getPermissions().size());
        assertEquals(1, (Object) role.specificPermissions().getPermissionValue("specificPermission1"));
        assertEquals(1, (Object) role.specificPermissions().getPermissionValue("specificPermission2"));
        assertEquals(1, (Object) role.specificPermissions().getPermissionValue("specificPermission3"));
        assertEquals(1, (Object) role.screenAccess().getPermissionValue("sec$Role.browse"));
    }

    protected Role createRoleEntityWithPermissions() {
        Role roleEntity = metadata.create(Role.class);
        roleEntity.setName("roleEntity");
        roleEntity.setType(RoleType.DENYING);
        roleEntity.setDefaultRole(true);
        roleEntity.setDescription("test description");

        Set<Permission> permissionSet = new HashSet<>();
        permissionSet.add(createPermission(roleEntity, PermissionType.SPECIFIC, "specificPermission1", 1));
        permissionSet.add(createPermission(roleEntity, PermissionType.SPECIFIC, "specificPermission2", 0));
        roleEntity.setPermissions(permissionSet);

        return roleEntity;
    }

    protected Permission createPermission(Role role, PermissionType type, String target, Integer value) {
        Permission permission = metadata.create(Permission.class);
        permission.setRole(role);
        permission.setType(type);
        permission.setTarget(target);
        permission.setValue(value);

        return permission;
    }
}
