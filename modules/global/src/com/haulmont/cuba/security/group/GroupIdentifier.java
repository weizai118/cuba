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

package com.haulmont.cuba.security.group;

import com.google.common.base.MoreObjects;

import java.util.UUID;

/**
 * Group identifier.
 * UUID identifier - for DB access groups
 * String group name - for annotation bases access groups
 */
public class GroupIdentifier {
    protected String groupName;
    protected UUID dbId;

    public static GroupIdentifier withDbId(UUID id) {
        GroupIdentifier identifier = new GroupIdentifier();
        identifier.dbId = id;
        return identifier;
    }

    public static GroupIdentifier withName(String groupName) {
        GroupIdentifier identifier = new GroupIdentifier();
        identifier.groupName = groupName;
        return identifier;
    }

    private GroupIdentifier() {
    }

    public String getGroupName() {
        return groupName;
    }

    public UUID getDbId() {
        return dbId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("identifier")
                .add("groupName", groupName)
                .add("dbId", dbId)
                .toString();
    }
}
