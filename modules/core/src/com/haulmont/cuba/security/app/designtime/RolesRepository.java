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

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.entity.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Component(RolesRepository.NAME)
public class RolesRepository {
    public static final String NAME = "cuba_RolesRepository";

    @Inject
    protected List<OrdinaryRole> designTimeRoles;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected Metadata metadata;

    @Inject
    protected GlobalConfig config;

    protected Map<String, OrdinaryRole> nameToRoleMapping;

    protected static final int ROLES_FROM_DATABASE_MODE = 1;
    protected static final int ROLES_FROM_CODE_MODE = 2;

    public Collection<OrdinaryRole> getOrdinaryRoles(@Nullable Collection<UserRole> userRoles) {
        if (userRoles == null) {
            return null;
        }

        Map<String, OrdinaryRole> result = new HashMap<>();

        List<UserRole> userRolesWithOrdinaryRole = userRoles.stream()
                .filter(ur -> ur.getOrdinaryRole() != null)
                .collect(Collectors.toList());

        List<UserRole> userRolesWithRoleName = userRoles.stream()
                .filter(ur -> ur.getOrdinaryRole() == null && ur.getRoleName() != null)
                .collect(Collectors.toList());

        List<UserRole> userRolesWithRoleObject = userRoles.stream()
                .filter(ur -> ur.getOrdinaryRole() == null && ur.getRole() != null)
                .collect(Collectors.toList());

        userRolesWithOrdinaryRole.stream()
                .filter(ur -> ur.getOrdinaryRole() != null)
                .forEach(ur -> result.put(ur.getOrdinaryRole().getName(), ur.getOrdinaryRole()));

        if (isPredefinedRolesModeAvailable()) {
            for (UserRole ur : userRolesWithRoleName) {
                OrdinaryRole role = getOrdinaryRoleByName(ur.getRoleName());
                if (role != null) {
                    ur.setOrdinaryRole(role);
                    result.put(role.getName(), role);
                }
            }
        }

        if (isDatabaseModeAvailable()) {
            for (UserRole ur : userRolesWithRoleObject) {
                OrdinaryRole role = RoleBuilder.createRole(ur.getRole()).build();
                ur.setOrdinaryRole(role);
                result.put(role.getName(), role);
            }
        }

        return result.values();
    }

    public OrdinaryRole getOrdinaryRoleByName(String roleName) {
        return getNameToRoleMapping().get(roleName);
    }

    public Map<String, Role> getDefaultRoles(EntityManager em) {

        Map<String, Role> defaultUserRoles = new HashMap<>();

        if (isPredefinedRolesModeAvailable()) {
            for (Map.Entry<String, OrdinaryRole> entry : getNameToRoleMapping().entrySet()) {
                if (entry.getValue().isDefault()) {
                    defaultUserRoles.put(entry.getKey(), null);
                }
            }
        }

        if (isDatabaseModeAvailable()) {
            List<Role> defaultRoles = em.createQuery(
                    "select r from sec$Role r where r.defaultRole = true", Role.class)
                    .getResultList();

            for (Role role : defaultRoles) {
                defaultUserRoles.put(role.getName(), role);
            }
        }

        return defaultUserRoles;
    }

    public Role getRoleWithPermissions(OrdinaryRole ordinaryRole) {
        if (ordinaryRole == null) {
            return null;
        }

        Role role = getRoleWithoutPermissions(ordinaryRole);

        Set<Permission> permissions = new HashSet<>(transformPermissions(PermissionType.ENTITY_OP,
                ordinaryRole.entityAccess(), role));
        permissions.addAll(transformPermissions(PermissionType.ENTITY_ATTR, ordinaryRole.attributeAccess(), role));
        permissions.addAll(transformPermissions(PermissionType.SPECIFIC, ordinaryRole.specificPermissions(), role));
        permissions.addAll(transformPermissions(PermissionType.SCREEN, ordinaryRole.screenAccess(), role));
        permissions.addAll(transformPermissions(PermissionType.UI, ordinaryRole.screenElementsAccess(), role));

        role.setPermissions(permissions);

        return role;
    }

    public Role getRoleWithoutPermissions(OrdinaryRole ordinaryRole) {
        if (ordinaryRole == null) {
            return null;
        }
        Role role = metadata.create(Role.class);
        role.setPredefined(true);
        role.setName(ordinaryRole.getName());
        role.setDescription(ordinaryRole.getDescription());
        role.setType(ordinaryRole.getRoleType());
        role.setDefaultRole(ordinaryRole.isDefault());

        return role;
    }

    protected Set<Permission> transformPermissions(PermissionType type, Permissions permissions, Role role) {
        if (permissions == null || role == null || type == null) {
            return Collections.emptySet();
        }
        Set<Permission> result = new HashSet<>();

        for (Map.Entry<String, Integer> entry : permissions.getPermissions().entrySet()) {
            Permission permission = metadata.create(Permission.class);
            permission.setTarget(entry.getKey());
            permission.setValue(entry.getValue());
            permission.setType(type);
            permission.setRole(role);

            result.add(permission);
        }

        return result;
    }

    public Collection<Permission> getPermissions(String predefinedRoleName, PermissionType permissionType) {
        Permissions permissions;
        OrdinaryRole ordinaryRole = getOrdinaryRoleByName(predefinedRoleName);
        switch (permissionType) {
            case ENTITY_OP:
                permissions = ordinaryRole.entityAccess();
                break;
            case ENTITY_ATTR:
                permissions = ordinaryRole.attributeAccess();
                break;
            case SPECIFIC:
                permissions = ordinaryRole.specificPermissions();
                break;
            case SCREEN:
                permissions = ordinaryRole.screenAccess();
                break;
            case UI:
                permissions = ordinaryRole.screenElementsAccess();
                break;
            default:
                permissions = null;
        }

        return transformPermissions(permissionType, permissions, getRoleWithoutPermissions(ordinaryRole));
    }

    public boolean isDatabaseModeAvailable() {
        return (getMode() & ROLES_FROM_DATABASE_MODE) == ROLES_FROM_DATABASE_MODE;
    }

    public boolean isPredefinedRolesModeAvailable() {
        return (getMode() & ROLES_FROM_CODE_MODE) == ROLES_FROM_CODE_MODE;
    }

    protected int getMode() {
        int valueFromConfig = config.getRolesStorageMode();
        if (valueFromConfig < 1 || valueFromConfig > 3) {
            return 3;
        }
        return valueFromConfig;
    }

    protected Map<String, OrdinaryRole> getNameToRoleMapping() {
        if (nameToRoleMapping == null) {
            nameToRoleMapping = new HashMap<>();

            for (OrdinaryRole role : designTimeRoles) {
                nameToRoleMapping.put(role.getName(), role);
            }
        }
        return nameToRoleMapping;
    }

    public Collection<Role> getRolesForUi() {
        Map<String, Role> rolesForGui = new HashMap<>();

        if (isPredefinedRolesModeAvailable()) {
            for (Map.Entry<String, OrdinaryRole> entry : getNameToRoleMapping().entrySet()) {
                rolesForGui.put(entry.getKey(), getRoleWithoutPermissions(entry.getValue()));
            }
        }

        if (isDatabaseModeAvailable()) {
            List<Role> roles = dataManager.load(Role.class)
                    .query("select r from sec$Role r order by r.name")
                    .list();

            for (Role role : roles) {
                rolesForGui.put(role.getName(), role);
            }
        }

        return new ArrayList<>(rolesForGui.values());
    }

    public Role getRoleByNameForUi(String predefinedRoleName) {
        return getRoleWithoutPermissions(getOrdinaryRoleByName(predefinedRoleName));
    }
}
