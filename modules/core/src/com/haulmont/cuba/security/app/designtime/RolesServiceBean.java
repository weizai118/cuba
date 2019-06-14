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

import com.haulmont.cuba.security.designtime.RolesService;
import com.haulmont.cuba.security.entity.Permission;
import com.haulmont.cuba.security.entity.PermissionType;
import com.haulmont.cuba.security.entity.Role;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

@Service(RolesService.NAME)
public class RolesServiceBean implements RolesService {

    @Inject
    protected RolesRepository rolesRepository;

    @Override
    public Collection<Role> getAllRoles() {
        return rolesRepository.getRolesForUi();
    }

    @Override
    public Role getRoleByName(String predefinedRoleName) {
        return rolesRepository.getRoleByNameForUi(predefinedRoleName);
    }

    @Override
    public Collection<Permission> getPermissions(String predefinedRoleName, PermissionType permissionType) {
        return rolesRepository.getPermissions(predefinedRoleName, permissionType);
    }

    @Override
    public boolean isDatabaseModeAvailable() {
        return rolesRepository.isDatabaseModeAvailable();
    }

    @Override
    public boolean isPredefinedRolesModeAvailable() {
        return rolesRepository.isPredefinedRolesModeAvailable();
    }
}
