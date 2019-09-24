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

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.group.BasicGroupDef;
import com.haulmont.cuba.security.group.GroupDef;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Component(GroupDefBuilder.NAME)
public class GroupDefBuilder {

    public static final String NAME = "cuba_GroupDefBuilder";

    @Inject
    protected AccessConstraintsBuilder accessConstraintsBuilder;

    protected String name;
    protected Map<String, Serializable> sessionAttributes;

    public static GroupDefBuilder create() {
        return AppBeans.get(GroupDefBuilder.NAME);
    }

    public GroupDefBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public GroupDefBuilder withJpqlConstraint(Class<? extends Entity> target, String where, String join) {
        return withConstraints(builder -> builder.withJpql(target, where, join));
    }

    public GroupDefBuilder withJpqlConstraint(Class<? extends Entity> target, String where) {
        return withConstraints(builder -> builder.withJpql(target, where));
    }

    public GroupDefBuilder withInMemoryConstraint(Class<? extends Entity> target, EntityOp operation, Predicate<? extends Entity> predicate) {
        return withConstraints(builder -> builder.withInMemory(target, operation, predicate));
    }

    public GroupDefBuilder withGroovyConstraint(Class<? extends Entity> target, EntityOp operation, String groovyScript) {
        return withConstraints(builder -> builder.withGroovy(target, operation, groovyScript));
    }

    public GroupDefBuilder withConstraints(Consumer<AccessConstraintsBuilder> constraintsConsumer) {
        constraintsConsumer.accept(accessConstraintsBuilder);
        return this;
    }

    public GroupDefBuilder withSessionAttribute(String key, Serializable value) {
        if (sessionAttributes == null) {
            sessionAttributes = new HashMap<>();
        }
        sessionAttributes.put(key, value);
        return this;
    }

    public GroupDef build() {
        BasicGroupDef groupDef = new BasicGroupDef();
        groupDef.setName(name);
        groupDef.setEntityConstraints(accessConstraintsBuilder.build());
        groupDef.setSessionAttributes(sessionAttributes);
        return groupDef;
    }
}
