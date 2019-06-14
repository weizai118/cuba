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

package com.haulmont.cuba.security.entity;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

public abstract class Permissions implements Serializable {

    private Map<String, Integer> permissions = new HashMap<>();

    public Map<String, Integer> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    public Integer getPermissionValue(String target) {
        return permissions.get(target);
    }

    public void addPermission(Permission permission) {
        checkPermission(permission);

        addPermissionWithoutCheck(permission);
    }

    public void addPermission(String target, @Nullable String extTarget, int value) {
        Integer currentValue = permissions.get(target);
        if (currentValue == null || currentValue < value) {
            permissions.put(target, value);
            if (extTarget != null)
                permissions.put(extTarget, value);
        }
    }

    public void addPermissions(Collection<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        for (Permission p : permissions) {
            checkPermission(p);
        }
        for (Permission p : permissions) {
            addPermissionWithoutCheck(p);
        }
    }

    public void addPermissions(Map<String, Integer> permissions) {
        for (Map.Entry<String, Integer> entry : permissions.entrySet()) {
            addPermission(entry.getKey(), null, entry.getValue());
        }
    }

    public void removePermission(String target) {
        permissions.remove(target);
    }

    public void removePermissions() {
        permissions.clear();
    }

    protected void checkPermission(Permission permission) {
        if (permission == null || !getPermissionType().equals(permission.getType())){
            throw new IllegalArgumentException("Permission type must be " + getPermissionType().name());
        }
    }

    protected void addPermissionWithoutCheck(Permission permission) {
        Integer currentValue = permissions.get(permission.getTarget());
        if (currentValue == null || currentValue < permission.getValue()) {
            permissions.put(permission.getTarget(), permission.getValue());
        }
    }

    abstract protected PermissionType getPermissionType();
}
