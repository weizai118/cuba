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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.DatatypeRegistry;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.MetadataTools;
import com.haulmont.cuba.security.app.group.annotation.Constraint;
import com.haulmont.cuba.security.app.group.annotation.Group;
import com.haulmont.cuba.security.app.group.annotation.JpqlConstraint;
import com.haulmont.cuba.security.app.group.annotation.SessionAttribute;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.group.GroupDef;
import com.haulmont.cuba.security.group.SetOfEntityConstraints;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

@Component(AnnotationGroupDefBuilder.NAME)
public class AnnotationGroupDefBuilder {

    public static final String NAME = "cuba_AnnotationGroupDefBuilder";

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected DatatypeRegistry datatypes;

    protected Map<Class<? extends Annotation>, AnnotationProcessor> processors;

    protected Cache<Method, Predicate<? extends Entity>> predicateCache;

    protected static final Set<String> FILTERED_METHOD_NAMES = ImmutableSet.of("getName", "getSessionAttributes");

    protected interface AnnotationProcessor<T extends AnnotationContext> {
        void processAnnotation(T annotationContext);
    }

    protected class AnnotationContext {
        protected Annotation annotation;
        protected Method method;
        protected Class ownerClass;

        public AnnotationContext(Annotation annotation, Method method, Class ownerClass) {
            this.annotation = annotation;
            this.method = method;
            this.ownerClass = ownerClass;
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public Method getMethod() {
            return method;
        }

        public Class getOwnerClass() {
            return ownerClass;
        }
    }

    protected class ConstraintsAnnotationContext extends AnnotationContext {
        protected AccessConstraintsBuilder constraintsBuilder;

        public ConstraintsAnnotationContext(Annotation annotation, Method method, Class groupClass, AccessConstraintsBuilder constraintsBuilder) {
            super(annotation, method, groupClass);
            this.constraintsBuilder = constraintsBuilder;
        }

        public AccessConstraintsBuilder getConstraintsBuilder() {
            return constraintsBuilder;
        }
    }

    protected class SessionAttributesContext extends AnnotationContext {
        Map<String, Serializable> sessionAttributes;

        public SessionAttributesContext(Annotation annotation, Method method, Class ownerClass, Map<String, Serializable> sessionAttributes) {
            super(annotation, method, ownerClass);
            this.sessionAttributes = sessionAttributes;
        }

        public Map<String, Serializable> getSessionAttributes() {
            return sessionAttributes;
        }
    }

    @PostConstruct
    protected void init() {
        registerAnnotationProcessor(JpqlConstraint.class, new JpqlAnnotationProcessor());
        registerAnnotationProcessor(Constraint.class, new ConstraintAnnotationProcessor());
        registerAnnotationProcessor(SessionAttribute.class, new SessionAttributesAnnotationProcessor());

        predicateCache = CacheBuilder.newBuilder().maximumSize(100).build();
    }

    public String getNameFromAnnotation(GroupDef group) {
        return getGroupAnnotationNN(group.getClass()).name();
    }

    public SetOfEntityConstraints buildSetOfEntityConstraints(GroupDef group) {
        Class<? extends GroupDef> clazz = group.getClass();

        AccessConstraintsBuilder constraintsBuilder = AccessConstraintsBuilder.create();

        for (Method method : clazz.getDeclaredMethods()) {
            if (isConstraintMethod(method)) {
                for (Annotation annotation : method.getDeclaredAnnotations()) {
                    AnnotationProcessor<ConstraintsAnnotationContext> processor = findAnnotationProcessor(annotation);
                    if (processor != null) {
                        processor.processAnnotation(new ConstraintsAnnotationContext(annotation, method, clazz, constraintsBuilder));
                    }
                }
            }
        }

        return constraintsBuilder.build();
    }

    public Map<String, Serializable> buildSessionAttributes(GroupDef group) {
        Class<? extends GroupDef> clazz = group.getClass();

        Map<String, Serializable> sessionAttributes = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (isSessionAttributesMethod(method)) {
                for (Annotation annotation : method.getDeclaredAnnotations()) {
                    AnnotationProcessor<SessionAttributesContext> processor = findAnnotationProcessor(annotation);
                    if (processor != null) {
                        processor.processAnnotation(new SessionAttributesContext(annotation, method, clazz, sessionAttributes));
                    }
                }
            }
        }

        return sessionAttributes;
    }

    protected <T extends AnnotationContext> AnnotationProcessor<T> findAnnotationProcessor(Annotation annotation) {
        //noinspection unchecked
        return (AnnotationProcessor<T>) processors.get(annotation.getClass());
    }

    protected void registerAnnotationProcessor(Class<? extends Annotation> annotation, AnnotationProcessor processor) {
        processors.put(annotation, processor);
    }

    protected boolean isConstraintMethod(Method method) {
        return !FILTERED_METHOD_NAMES.contains(method.getName());
    }

    protected boolean isSessionAttributesMethod(Method method) {
        return "getSessionAttributes".equals(method.getName());
    }

    protected Class<? extends GroupDef> getParentFromAnnotation(Class<? extends GroupDef> clazz) {
        return getGroupAnnotationNN(clazz).parent();
    }

    protected Group getGroupAnnotationNN(Class<? extends GroupDef> clazz) {
        Group annotation = clazz.getAnnotation(Group.class);
        if (annotation == null) {
            throw new IllegalStateException("The class must have @Group annotation.");
        }
        return annotation;
    }

    protected class JpqlAnnotationProcessor implements AnnotationProcessor<ConstraintsAnnotationContext> {
        @Override
        public void processAnnotation(ConstraintsAnnotationContext context) {
            JpqlConstraint constraint = (JpqlConstraint) context.getAnnotation();
            Class<? extends Entity> targetClass = !Entity.class.equals(constraint.target()) ? constraint.target() : resolveTargetClass(context.getMethod());
            if (!Entity.class.equals(targetClass)) {
                String where = Strings.emptyToNull(constraint.value());
                if (where == null) {
                    where = Strings.emptyToNull(constraint.where());
                }
                context.getConstraintsBuilder().withJpql(targetClass, where, Strings.emptyToNull(constraint.join()));
            }
        }
    }

    protected class ConstraintAnnotationProcessor implements AnnotationProcessor<ConstraintsAnnotationContext> {
        @Override
        public void processAnnotation(ConstraintsAnnotationContext context) {
            Constraint constraint = (Constraint) context.getAnnotation();
            Class<? extends Entity> targetClass = resolveTargetClass(context.getMethod());
            if (!Entity.class.equals(targetClass)) {
                for (EntityOp operation : constraint.operations()) {
                    context.getConstraintsBuilder().withInMemory(targetClass, operation, createConstraintPredicate(context.getMethod(), context.getOwnerClass()));
                }
            }
        }
    }

    protected class SessionAttributesAnnotationProcessor implements AnnotationProcessor<SessionAttributesContext> {
        @Override
        public void processAnnotation(SessionAttributesContext context) {
            SessionAttribute attribute = (SessionAttribute) context.getAnnotation();
            Map<String, Serializable> sessionAttributes = context.getSessionAttributes();
            Datatype datatype = datatypes.getNN(attribute.javaClass());
            try {
                sessionAttributes.put(attribute.name(), (Serializable) datatype.parse(attribute.value()));
            } catch (ParseException e) {
                throw new RuntimeException(String.format("Unable to load session attribute %s for group %s",
                        attribute.name(), context.getOwnerClass().getSimpleName()), e);
            }
        }
    }

    protected Class<? extends Entity> resolveTargetClass(Method method) {
        if (method.getParameterTypes().length == 1) {
            Class<?> parameterType = method.getParameterTypes()[0];
            if (Entity.class.isAssignableFrom(parameterType)) {
                //noinspection unchecked
                return (Class<? extends Entity>) parameterType;
            }
        }
        throw new IllegalStateException(
                String.format("Method [%s] must have only one parameter with Entity argument", method.getName()));
    }

    protected Predicate<? extends Entity> createConstraintPredicate(Method method, Class clazz) {
        try {
            return predicateCache.get(method, () -> {
                Predicate<? extends Entity> result;
                try {
                    MethodHandles.Lookup caller = MethodHandles.lookup();
                    CallSite site = LambdaMetafactory.metafactory(caller,
                            "test",
                            MethodType.methodType(Predicate.class),
                            MethodType.methodType(boolean.class, Object.class),
                            caller.findVirtual(clazz, method.getName(), MethodType.methodType(method.getReturnType())),
                            MethodType.methodType(method.getReturnType(), clazz));
                    MethodHandle factory = site.getTarget();
                    //noinspection unchecked
                    result = (Predicate<? extends Entity>) factory.invoke();
                } catch (Throwable e) {
                    throw new IllegalStateException("Can't create in-memory constraint predicate", e);
                }
                return result;
            });
        } catch (ExecutionException e) {
            throw new IllegalStateException("Can't create in-memory constraint predicate", e);
        }
    }
}
