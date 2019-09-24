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

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.PersistenceSecurity;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.group.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;

@Component(AccessConstraintsBuilder.NAME)
public class AccessConstraintsBuilder {

    public static final String NAME = "cuba_AccessConstraintsBuilder";

    @Inject
    protected Metadata metadata;

    @Inject
    protected PersistenceSecurity security;

    protected List<SetOfEntityConstraints> joinSets = new ArrayList<>();
    protected Map<String, List<EntityConstraint>> builderConstraints = new HashMap<>();

    public static AccessConstraintsBuilder create() {
        return AppBeans.get(AccessConstraintsBuilder.NAME);
    }

    public AccessConstraintsBuilder join(SetOfEntityConstraints constraints) {
        joinSets.add(constraints);
        return this;
    }

    public AccessConstraintsBuilder withJpql(Class<? extends Entity> target, String where, String join) {
        MetaClass metaClass = metadata.getClassNN(target);

        BasicJpqlEntityConstraint constraint = new BasicJpqlEntityConstraint();
        constraint.setEntityType(metaClass);
        constraint.setWhere(where);
        constraint.setJoin(join);

        addConstraint(metaClass, constraint);

        return this;
    }

    public AccessConstraintsBuilder withJpql(Class<? extends Entity> target, String where) {
        return withJpql(target, where);
    }

    public AccessConstraintsBuilder withInMemory(Class<? extends Entity> target, EntityOp operation, Predicate<? extends Entity> predicate) {
        MetaClass metaClass = metadata.getClassNN(target);

        BasicEntityConstraint constraint = new BasicEntityConstraint();
        constraint.setEntityType(metaClass);
        constraint.setOperation(operation);
        constraint.setPredicate(predicate);

        addConstraint(metaClass, constraint);

        return this;
    }

    public AccessConstraintsBuilder withGroovy(Class<? extends Entity> target, EntityOp operation, String groovyScript) {
        MetaClass metaClass = metadata.getClassNN(target);

        BasicEntityConstraint constraint = new BasicEntityConstraint();
        constraint.setEntityType(metaClass);
        constraint.setOperation(operation);
        constraint.setPredicate((Predicate<? extends Entity>) o -> (boolean) security.evaluateConstraintScript(o, groovyScript));

        addConstraint(metaClass, constraint);

        return this;
    }

    public SetOfEntityConstraints build() {
        BasicSetOfEntityConstraints setOfEntityConstraints = new BasicSetOfEntityConstraints();

        Map<String, List<EntityConstraint>> resultConstraints = new HashMap<>();
        for (SetOfEntityConstraints joinSet : joinSets) {
            if (joinSet instanceof BasicSetOfEntityConstraints) {
                Map<String, List<EntityConstraint>> constraints = ((BasicSetOfEntityConstraints) joinSet).getConstraints();
                for (Map.Entry<String, List<EntityConstraint>> entry : constraints.entrySet()) {
                    resultConstraints.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
                }
            }
        }

        for (Map.Entry<String, List<EntityConstraint>> entry : builderConstraints.entrySet()) {
            resultConstraints.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
        }

        setOfEntityConstraints.setConstraints(resultConstraints);

        return setOfEntityConstraints;
    }

    protected void addConstraint(MetaClass metaClass, EntityConstraint constraint) {
        List<EntityConstraint> constraints = builderConstraints.computeIfAbsent(metaClass.getName(), k -> new ArrayList<>());

        EntityConstraint existingConstraint = constraints.stream()
                .filter(c -> Objects.equals(c.getOperation(), constraint.getOperation()))
                .findFirst()
                .orElse(null);
        if (existingConstraint != null) {
            if (constraint instanceof JpqlEntityConstraint) {
                if (existingConstraint instanceof JpqlEntityConstraint) {
                    constraints.add(constraint);
                } else {
                    constraints.remove(existingConstraint);
                    constraints.add(constraint);

                    ((BasicJpqlEntityConstraint) constraint).setPredicate(existingConstraint.getPredicate());
                }
            } else {
                if (existingConstraint instanceof JpqlEntityConstraint && existingConstraint.getPredicate() == null) {
                    ((BasicJpqlEntityConstraint) existingConstraint).setPredicate(constraint.getPredicate());
                } else {
                    constraints.add(constraint);
                }
            }
        } else {
            constraints.add(constraint);
        }
    }
}
