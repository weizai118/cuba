/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.security.entity;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;

import java.util.Objects;
import java.util.Set;

/**
 * Role type.
 */
public enum RoleType implements EnumClass<Integer> {

    STANDARD(0) {
        @Override
        public Integer permissionValue(PermissionType type, String target) {
            return null;
        }
    },
    SUPER(10) {
        @Override
        public Integer permissionValue(PermissionType type, String target) {
            return Integer.MAX_VALUE;
        }
    },
    READONLY(20) {
        @Override
        public Integer permissionValue(PermissionType type, String target) {
            switch (type) {
                case ENTITY_ATTR:
                case SCREEN:
                case SPECIFIC:
                    return null;
                case ENTITY_OP:
                    if (target.endsWith(EntityOp.CREATE.getId())
                            || target.endsWith(EntityOp.UPDATE.getId())
                            || target.endsWith(EntityOp.DELETE.getId()))
                        return 0;
                    else
                        return null;
                default:
                    return null;
            }
        }
    },
    DENYING(30) {
        @Override
        public Integer permissionValue(PermissionType type, String target) {
            switch (type) {
                case ENTITY_ATTR:
                    return null;
                case SCREEN:
                case SPECIFIC:
                case ENTITY_OP:
                    return 0;
                default:
                    return null;
            }
        }
    },
    STRICTLY_DENYING(40) {

        protected final Set<String> systemAttributes = ImmutableSet.of("deleteTs", "version");

        @Override
        public Integer permissionValue(PermissionType type, String target) {
            switch (type) {
                case ENTITY_ATTR:
                    return entityAttributePermission(target);
                case SCREEN:
                case SPECIFIC:
                case ENTITY_OP:
                    return 0;
                default:
                    return null;
            }
        }

        protected Integer entityAttributePermission(String target) {
            Iterable<String> paths = Splitter.on(Permission.TARGET_PATH_DELIMETER)
                    .omitEmptyStrings()
                    .trimResults()
                    .split(target);
            if (Iterables.size(paths) == 2) {
                String entityName = Iterables.get(paths, 0);
                String attribute = Iterables.get(paths, 1);

                if (systemAttributes.contains(attribute)) {
                    return 1;
                }

                Metadata metadata = AppBeans.get(Metadata.class);
                MetaClass metaClass = metadata.getClass(entityName);

                if (metaClass != null) {
                    String identifier = metadata.getTools().getPrimaryKeyName(metaClass);
                    if (Objects.equals(identifier, attribute)) {
                        return 1;
                    }
                }
            }
            return 0;
        }
    };

    private int id;

    RoleType(int id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public static RoleType fromId(Integer id) {
        if (id == null)
            return STANDARD; // for backward compatibility, just in case
        for (RoleType type : RoleType.values()) {
            if (Objects.equals(id, type.getId()))
                return type;
        }
        return null; // unknown id
    }

    public abstract Integer permissionValue(PermissionType type, String target);
}
