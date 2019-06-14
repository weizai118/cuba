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

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.app.designtime.AnnotationPermissionsBuilder;
import com.haulmont.cuba.security.app.designtime.annotation.*;
import com.haulmont.cuba.security.entity.*;
import com.haulmont.cuba.testsupport.TestContainer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.*;

public class AnnotationPermissionsBuilderTest {

    protected AnnotationPermissionsBuilder builder;
    protected Metadata metadata;
    protected TestPredefinedRole role;

    @ClassRule
    public static TestContainer cont = TestContainer.Common.INSTANCE;

    @Before
    public void setUp() throws Exception {
        builder = AppBeans.get(AnnotationPermissionsBuilder.class);
        metadata = cont.metadata();
        role = new TestPredefinedRole();
    }

    @Test
    public void testGettingInfoFromClassAnnotation() {
        assertEquals("TestPredefinedRole", builder.getNameFromAnnotation(role));
        assertEquals(RoleType.STANDARD, builder.getTypeFromAnnotation(role));
        assertEquals("Test role", builder.getDescriptionFromAnnotation(role));
        assertFalse(builder.getIsDefaultFromAnnotation(role));
    }

    @Test
    public void testPermissionsBuilding() {
        EntityAccessPermissions entityAccessPermissions = builder.buildEntityAccessPermissions(role);
        String entityTarget1 = metadata.getClassNN(User.class).getName() + Permission.TARGET_PATH_DELIMETER + EntityOp.CREATE.getId();
        String entityTarget2 = metadata.getClassNN(User.class).getName() + Permission.TARGET_PATH_DELIMETER + EntityOp.READ.getId();
        String entityTarget3 = metadata.getClassNN(User.class).getName() + Permission.TARGET_PATH_DELIMETER + EntityOp.UPDATE.getId();
        String entityTarget4 = metadata.getClassNN(Role.class).getName() + Permission.TARGET_PATH_DELIMETER + EntityOp.READ.getId();

        assertEquals(4, entityAccessPermissions.getPermissions().size());
        assertEquals(1, (Object) entityAccessPermissions.getPermissionValue(entityTarget1));
        assertEquals(1, (Object) entityAccessPermissions.getPermissionValue(entityTarget2));
        assertEquals(0, (Object) entityAccessPermissions.getPermissionValue(entityTarget3));
        assertEquals(1, (Object) entityAccessPermissions.getPermissionValue(entityTarget4));

        EntityAttributeAccessPermissions entityAttributeAccessPermissions = builder.buildEntityAttributeAccessPermissions(role);
        String attributeTarget1 = metadata.getClassNN(User.class).getName() + Permission.TARGET_PATH_DELIMETER + "login";
        String attributeTarget2 = metadata.getClassNN(Role.class).getName() + Permission.TARGET_PATH_DELIMETER + "name";
        String attributeTarget3 = metadata.getClassNN(Role.class).getName() + Permission.TARGET_PATH_DELIMETER + "description";

        assertEquals(3, entityAttributeAccessPermissions.getPermissions().size());
        assertEquals(2, (Object) entityAttributeAccessPermissions.getPermissionValue(attributeTarget1));
        assertEquals(1, (Object) entityAttributeAccessPermissions.getPermissionValue(attributeTarget2));
        assertEquals(0, (Object) entityAttributeAccessPermissions.getPermissionValue(attributeTarget3));

        SpecificPermissions specificPermissions = builder.buildSpecificPermissions(role);

        assertEquals(2, specificPermissions.getPermissions().size());
        assertEquals(1, (Object) specificPermissions.getPermissionValue("specificPermission2"));
        assertEquals(0, (Object) specificPermissions.getPermissionValue("specificPermission1"));

        ScreenPermissions screenPermissions = builder.buildScreenPermissions(role);

        assertEquals(3, screenPermissions.getPermissions().size());
        assertEquals(1, (Object) screenPermissions.getPermissionValue("sec$Role.edit"));
        assertEquals(1, (Object) screenPermissions.getPermissionValue("sec$User.edit"));
        assertEquals(0, (Object) screenPermissions.getPermissionValue("sec$Role.browse"));

        ScreenElementsPermissions screenElementsPermissions = builder.buildScreenElementsPermissions(role);
        String elementTarget = "sec$Role.edit" + Permission.TARGET_PATH_DELIMETER + "roleGroupBox";

        assertEquals(1, screenElementsPermissions.getPermissions().size());
        assertEquals(1, (Object) screenElementsPermissions.getPermissionValue(elementTarget));

    }

    @DesignTimeRole(name = "TestPredefinedRole", type = RoleType.STANDARD,
            isDefault = false, description = "Test role")
    protected class TestPredefinedRole implements OrdinaryRole {

        @Override
        public RoleType getRoleType() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @EntityAccess(target = User.class,
                allow = {EntityOp.CREATE, EntityOp.READ}, deny = {EntityOp.UPDATE})
        @EntityAccess(target = Role.class,
                allow = {EntityOp.READ})
        @Override
        public EntityAccessPermissions entityAccess() {
            return null;
        }

        @EntityAttributeAccess(target = User.class, allow = {"login"})
        @EntityAttributeAccess(target = Role.class, readOnly = {"name"}, deny = {"description"})
        @Override
        public EntityAttributeAccessPermissions attributeAccess() {
            return null;
        }

        @SpecificPermission(target = "specificPermission2", access = AccessOperation.ALLOW)
        @SpecificPermission(target = "specificPermission1", access = AccessOperation.DENY)
        @Override
        public SpecificPermissions specificPermissions() {
            return null;
        }

        @ScreenAccess(allow = {"sec$Role.edit", "sec$User.edit"}, deny = {"sec$Role.browse"})
        @Override
        public ScreenPermissions screenAccess() {
            return null;
        }

        @ScreenElementAccess(screen = "sec$Role.edit", allow = {"roleGroupBox"})
        @Override
        public ScreenElementsPermissions screenElementsAccess() {
            return null;
        }
    }
}
