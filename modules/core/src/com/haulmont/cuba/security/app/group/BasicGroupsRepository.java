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

package com.haulmont.cuba.security.app.group;

import com.google.common.base.Strings;
import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.DatatypeRegistry;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.entity.Constraint;
import com.haulmont.cuba.security.entity.ConstraintOperationType;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.entity.SessionAttribute;
import com.haulmont.cuba.security.group.GroupDef;
import com.haulmont.cuba.security.group.GroupIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component(GroupsRepository.NAME)
public class BasicGroupsRepository implements GroupsRepository {
    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected DatatypeRegistry datatypes;

    @Inject
    protected GlobalConfig config;

    @Inject
    protected List<GroupDef> groupDefinitions;

    protected Map<String, GroupDef> groupDefinitionsByName;

    private final Logger log = LoggerFactory.getLogger(BasicGroupsRepository.class);

    @PostConstruct
    protected void init() {
        groupDefinitionsByName = new ConcurrentHashMap<>();
        if (groupDefinitions != null) {
            for (GroupDef groupDefinition : groupDefinitions) {
                groupDefinitionsByName.put(groupDefinition.getName(), groupDefinition);
            }
        }
    }

    @Override
    public GroupDef getGroupDefinition(GroupIdentifier identifier) {
        if (identifier.getDbId() != null) {
            return getGroupDefinitionFromDB(identifier.getDbId());
        } else if (identifier.getGroupName() != null) {
            return getGroupDefinitionFromAnnotations(identifier.getGroupName());
        }
        throw new IllegalArgumentException(String.format("%s isn't valid", identifier));
    }

    @Override
    public void registerGroup(GroupDef groupDef) {
        groupDefinitionsByName.put(groupDef.getName(), groupDef);
    }

    protected GroupDef getGroupDefinitionFromAnnotations(String groupName) {
        GroupDef groupDefinition = groupDefinitionsByName.get(groupName);
        if (groupDefinition == null) {
            throw new IllegalStateException(String.format("Unable to find predefined group definition %s", groupName));
        }
        return groupDefinition;
    }

    protected GroupDef getGroupDefinitionFromDB(UUID groupId) {
        return persistence.callInTransaction(em -> {
            GroupDefBuilder groupDefBuilder = GroupDefBuilder.create();

            List<Constraint> constraints = em.createQuery("select c from sec$GroupHierarchy h join h.parent.constraints c " +
                    "where h.group.id = ?1", Constraint.class)
                    .setParameter(1, groupId)
                    .getResultList();

            for (Constraint constraint : constraints) {
                processConstraints(constraint, groupDefBuilder);
            }

            List<SessionAttribute> attributes = em.createQuery("select a from sec$GroupHierarchy h join h.parent.sessionAttributes a " +
                    "where h.group.id = ?1 order by h.level desc", SessionAttribute.class)
                    .setParameter(1, groupId)
                    .getResultList();

            Set<String> attributeKeys = new HashSet<>();
            for (SessionAttribute attribute : attributes) {
                Datatype datatype = datatypes.get(attribute.getDatatype());
                try {
                    if (attributeKeys.contains(attribute.getName())) {
                        log.warn("Duplicate definition of '{}' session attribute in the group hierarchy", attribute.getName());
                    }

                    groupDefBuilder.withSessionAttribute(attribute.getName(), (Serializable) datatype.parse(attribute.getStringValue()));

                    attributeKeys.add(attribute.getName());
                } catch (ParseException e) {
                    throw new RuntimeException(String.format("Unable to load session attribute %s", attribute.getName()), e);
                }
            }

            return groupDefBuilder.build();
        });
    }

    protected void processConstraints(Constraint constraint, GroupDefBuilder groupDefBuilder) {
        if (Boolean.TRUE.equals(constraint.getIsActive())) {
            Class<? extends Entity> targetClass = metadata.getClassNN(constraint.getEntityName()).getJavaClass();
            for (EntityOp operation : constraint.getOperationType().toEntityOps()) {

                if (EntityOp.READ == operation && !Strings.isNullOrEmpty(constraint.getWhereClause())) {
                    groupDefBuilder.withJpqlConstraint(targetClass, constraint.getWhereClause(), constraint.getJoinClause());
                }

                if (!Strings.isNullOrEmpty(constraint.getGroovyScript())) {
                    groupDefBuilder.withGroovyConstraint(targetClass, operation, constraint.getGroovyScript());
                }

            }
        }
    }
}
