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

package com.haulmont.cuba.security.app.designtime;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.security.entity.*;

/**
 * Builder class that helps to create custom predefined roles.
 */
public class OrdinaryRoleBuilder {

    private EntityAccessPermissions entityAccessPermissions;
    private EntityAttributeAccessPermissions entityAttributeAccessPermissions;
    private SpecificPermissions specificPermissions;
    private ScreenPermissions screenPermissions;
    private ScreenElementsPermissions screenElementsPermissions;
    private RoleType roleType;
    private String name;
    private String description;

    private OrdinaryRoleBuilder() {
        entityAccessPermissions = new EntityAccessPermissions();
        entityAttributeAccessPermissions = new EntityAttributeAccessPermissions();
        specificPermissions = new SpecificPermissions();
        screenPermissions = new ScreenPermissions();
        screenElementsPermissions = new ScreenElementsPermissions();
        roleType = RoleType.STANDARD;
        description = "";
    }

    /**
     * @return new builder with default role parameters: {@code roleType = RoleType.STANDARD},
     * {@code description} and {@code name} are empty.
     */
    public static OrdinaryRoleBuilder createRole() {
        return new OrdinaryRoleBuilder();
    }

    /**
     * @param roleType {@link RoleType}, default value is {@code RoleType.STANDARD}
     * @return new builder with empty role name and description
     */
    public static OrdinaryRoleBuilder createRole(RoleType roleType) {
        OrdinaryRoleBuilder builder = new OrdinaryRoleBuilder();
        return builder.withRoleType(roleType);
    }

    /**
     * @param role source {@link Role} object. Following parameters are taken from this role: name,
     *             description, roleType, permissions
     * @return new builder
     */
    public static OrdinaryRoleBuilder createRole(Role role) {
        return new OrdinaryRoleBuilder()
                .withRoleType(role.getType())
                .withName(role.getName())
                .withDescription(role.getDescription())
                .join(role);
    }

    /**
     * @param role source {@link OrdinaryRole} object. Following parameters are taken from this role: name,
     *             description, roleType, permissions
     * @return new builder
     */
    public static OrdinaryRoleBuilder createRole(OrdinaryRole role) {
        return new OrdinaryRoleBuilder()
                .withRoleType(role.getRoleType())
                .withName(role.getName())
                .withDescription(role.getDescription())
                .join(role);
    }

    public OrdinaryRoleBuilder withName(String name) {
        this.name = name;

        return this;
    }

    public OrdinaryRoleBuilder withDescription(String description) {
        this.description = description;

        return this;
    }

    public OrdinaryRoleBuilder withRoleType(RoleType roleType) {
        this.roleType = roleType;
        return this;
    }

    public OrdinaryRoleBuilder withPermission(Permission permission) {
        joinPermission(permission);

        return this;
    }

    public OrdinaryRoleBuilder withPermission(PermissionType permissionType, String target, int access) {
        Permission permission = new Permission();
        permission.setType(permissionType);
        permission.setValue(access);
        permission.setTarget(target);

        joinPermission(permission);

        return this;
    }

    public OrdinaryRoleBuilder withEntityAccessPermission(MetaClass targetClass, EntityOp operation, AccessOperation access) {
        return withPermission(PermissionType.ENTITY_OP,
                targetClass.getName() + Permission.TARGET_PATH_DELIMETER + operation.getId(),
                access.getId());
    }

    public OrdinaryRoleBuilder withEntityAttrAccessPermission(MetaClass targetClass, String property, EntityAttrAccess access) {
        return withPermission(PermissionType.ENTITY_ATTR,
                targetClass.getName() + Permission.TARGET_PATH_DELIMETER + property,
                access.getId());
    }

    public OrdinaryRoleBuilder withSpecificPermission(String target, AccessOperation access) {
        return withPermission(PermissionType.SPECIFIC, target, access.getId());
    }

    public OrdinaryRoleBuilder withScreenPermission(String windowAlias, AccessOperation access) {
        return withPermission(PermissionType.SCREEN, windowAlias, access.getId());
    }

    public OrdinaryRoleBuilder withScreenElementPermission(String windowAlias, String component, AccessOperation access) {
        return withPermission(PermissionType.UI,
                windowAlias + Permission.TARGET_PATH_DELIMETER + component,
                access.getId());
    }

    public OrdinaryRoleBuilder join(OrdinaryRole role) {
        joinApplicationRole(role);
        joinGenericUiRole(role);

        return this;
    }

    public OrdinaryRoleBuilder join(ApplicationRole role) {
        joinApplicationRole(role);

        return this;
    }

    public OrdinaryRoleBuilder join(GenericUiRole role) {
        joinGenericUiRole(role);

        return this;
    }

    public OrdinaryRoleBuilder join(Role role) {
        if (role.getPermissions() != null) {
            for (Permission permission : role.getPermissions()) {
                joinPermission(permission);
            }
        }
        return this;
    }

    public OrdinaryRole build() {
        return new BasicUserRole(name, description, roleType, entityAccessPermissions,
                entityAttributeAccessPermissions, specificPermissions, screenPermissions, screenElementsPermissions);
    }


    protected void joinApplicationRole(ApplicationRole role) {
        entityAccessPermissions.addPermissions(role.entityAccess().getPermissions());
        entityAttributeAccessPermissions.addPermissions(role.attributeAccess().getPermissions());
        specificPermissions.addPermissions(role.specificPermissions().getPermissions());
    }

    protected void joinGenericUiRole(GenericUiRole role) {
        screenPermissions.addPermissions(role.screenAccess().getPermissions());
        screenElementsPermissions.addPermissions(role.screenElementsAccess().getPermissions());
    }

    protected void joinPermission(Permission permission) {
        switch (permission.getType()) {
            case ENTITY_OP:
                entityAccessPermissions.addPermission(permission);
                break;
            case ENTITY_ATTR:
                entityAttributeAccessPermissions.addPermission(permission);
                break;
            case SPECIFIC:
                specificPermissions.addPermission(permission);
                break;
            case SCREEN:
                screenPermissions.addPermission(permission);
                break;
            case UI:
                screenElementsPermissions.addPermission(permission);
                break;
            default:
                throw new IllegalArgumentException("Unsupported permission type.");
        }
    }

}
