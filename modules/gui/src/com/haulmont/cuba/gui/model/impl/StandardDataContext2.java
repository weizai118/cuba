/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.gui.model.impl;

import com.google.common.collect.Sets;
import com.haulmont.bali.events.EventHub;
import com.haulmont.bali.events.Subscription;
import com.haulmont.bali.util.Numbers;
import com.haulmont.bali.util.Preconditions;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.impl.AbstractInstance;
import com.haulmont.cuba.core.entity.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.persistence.FetchGroupUtils;
import com.haulmont.cuba.gui.model.DataContext;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class StandardDataContext2 implements DataContext {

    private static final Logger log = LoggerFactory.getLogger(StandardDataContext2.class);

    private ApplicationContext applicationContext;

    protected EventHub events = new EventHub();

    protected Map<Class<?>, Map<Object, Entity>> content = new HashMap<>();

    protected Set<Entity> modifiedInstances = new HashSet<>();

    protected Set<Entity> removedInstances = new HashSet<>();

    protected PropertyChangeListener propertyChangeListener = new PropertyChangeListener();

    protected boolean disableListeners;

    protected StandardDataContext parentContext;

    protected Function<CommitContext, Set<Entity>> commitDelegate;

    protected Map<Entity, Map<String, EmbeddedPropertyChangeListener>> embeddedPropertyListeners = new WeakHashMap<>();

    public StandardDataContext2(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected Metadata getMetadata() {
        return applicationContext.getBean(Metadata.NAME, Metadata.class);
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.NAME, MetadataTools.class);
    }

    protected EntityStates getEntityStates() {
        return applicationContext.getBean(EntityStates.NAME, EntityStates.class);
    }

    protected DataManager getDataManager() {
        return applicationContext.getBean(DataManager.NAME, DataManager.class);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends Entity<K>, K> T find(Class<T> entityClass, K entityId) {
        Map<Object, Entity> entityMap = content.get(entityClass);
        if (entityMap != null)
            return (T) entityMap.get(entityId);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Entity entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        return find(entity.getClass(), entity.getId()) != null;
    }

    @Override
    public <T extends Entity> T merge(T entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");

        disableListeners = true;
        T result;
        try {
            Set<Entity> merged = Sets.newIdentityHashSet();
            result = (T) internalMerge(entity, merged);
        } finally {
            disableListeners = false;
        }
        return result;
    }

    @Override
    public EntitySet merge(Collection<? extends Entity> entities) {
        return null;
    }

    @Override
    public void remove(Entity entity) {

    }

    @Override
    public void evict(Entity entity) {

    }

    @Override
    public boolean hasChanges() {
        return false;
    }

    @Override
    public boolean isModified(Entity entity) {
        return false;
    }

    @Override
    public boolean isRemoved(Entity entity) {
        return false;
    }

    @Override
    public void commit() {

    }

    @Nullable
    @Override
    public DataContext getParent() {
        return parentContext;
    }

    @Override
    public void setParent(DataContext parentContext) {
        Preconditions.checkNotNullArgument(parentContext, "parentContext is null");
        if (!(parentContext instanceof StandardDataContext))
            throw new IllegalArgumentException("Unsupported DataContext type: " + parentContext.getClass().getName());
        this.parentContext = (StandardDataContext) parentContext;

        for (Entity entity : this.parentContext.getAll()) {
//            Entity copy = copyGraph(entity, new HashMap<>());
//            merge(copy);
        }
    }

    @Override
    public Subscription addChangeListener(Consumer<ChangeEvent> listener) {
        return events.subscribe(ChangeEvent.class, listener);
    }

    protected void fireChangeListener(Entity entity) {
        events.publish(ChangeEvent.class, new ChangeEvent(this, entity));
    }

    @Override
    public Subscription addPreCommitListener(Consumer<PreCommitEvent> listener) {
        return events.subscribe(PreCommitEvent.class, listener);
    }

    @Override
    public Subscription addPostCommitListener(Consumer<PostCommitEvent> listener) {
        return events.subscribe(PostCommitEvent.class, listener);
    }

    @Override
    public Function<CommitContext, Set<Entity>> getCommitDelegate() {
        return commitDelegate;
    }

    @Override
    public void setCommitDelegate(Function<CommitContext, Set<Entity>> delegate) {
        this.commitDelegate = delegate;
    }

    protected Entity internalMerge(Entity entity, Set<Entity> mergedSet) {
        Map<Object, Entity> entityMap = content.computeIfAbsent(entity.getClass(), aClass -> new HashMap<>());
        Entity managed = entityMap.get(entity.getId());

        if (mergedSet.contains(entity)) {
            if (managed != null) {
                return managed;
            } else {
                // should never happen
                log.debug("Instance was merged but managed instance is null: {}", entity);
            }
        }
        mergedSet.add(entity);

        if (managed == null) {
            managed = copyEntity(entity);
            entityMap.put(managed.getId(), managed);

            mergeState(entity, managed, mergedSet);

            managed.addPropertyChangeListener(propertyChangeListener);

            if (getEntityStates().isNew(managed)) {
                modifiedInstances.add(managed);
                fireChangeListener(managed);
            }
            return managed;
        } else {
            if (managed != entity) {
                mergeState(entity, managed, mergedSet);
            }
            return managed;
        }
    }

    protected void modified(Entity entity) {
        if (!disableListeners) {
            modifiedInstances.add(entity);
            fireChangeListener(entity);
        }
    }

    private Entity copyEntity(Entity srcEntity) {
        Entity dstEntity;
        try {
            dstEntity = srcEntity.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Cannot create an instance of " + srcEntity.getClass(), e);
        }
        copyIdAndVersion(srcEntity, dstEntity);
        return dstEntity;
    }

    private List<Entity> createObservableList(Entity notifiedEntity) {
        return createObservableList(new ArrayList<>(), notifiedEntity);
    }

    private List<Entity> createObservableList(List<Entity> list, Entity notifiedEntity) {
        return new ObservableList<>(list, (changeType, changes) -> modified(notifiedEntity));
    }

    private Set<Entity> createObservableSet(Entity notifiedEntity) {
        return createObservableSet(new LinkedHashSet<>(), notifiedEntity);
    }

    private ObservableSet<Entity> createObservableSet(Set<Entity> set, Entity notifiedEntity) {
        return new ObservableSet<>(set, (changeType, changes) -> modified(notifiedEntity));
    }

    @SuppressWarnings("unchecked")
    protected void copyIdAndVersion(Entity srcEntity, Entity dstEntity) {
        if (dstEntity instanceof BaseGenericIdEntity)
            ((BaseGenericIdEntity) dstEntity).setId(srcEntity.getId());
        else if (dstEntity instanceof AbstractNotPersistentEntity)
            ((AbstractNotPersistentEntity) dstEntity).setId((UUID) srcEntity.getId());

        if (dstEntity instanceof Versioned) {
            ((Versioned) dstEntity).setVersion(((Versioned) srcEntity).getVersion());
        }
    }

    protected void copySystemState(Entity srcEntity, Entity dstEntity) {
        if (dstEntity instanceof BaseGenericIdEntity) {
            BaseEntityInternalAccess.copySystemState((BaseGenericIdEntity) srcEntity, (BaseGenericIdEntity) dstEntity);

            if (srcEntity instanceof FetchGroupTracker && dstEntity instanceof FetchGroupTracker) {
                FetchGroup srcFetchGroup = ((FetchGroupTracker) srcEntity)._persistence_getFetchGroup();
                FetchGroup dstFetchGroup = ((FetchGroupTracker) dstEntity)._persistence_getFetchGroup();
                if (srcFetchGroup == null || dstFetchGroup == null) {
                    ((FetchGroupTracker) dstEntity)._persistence_setFetchGroup(null);
                } else {
                    ((FetchGroupTracker) dstEntity)._persistence_setFetchGroup(FetchGroupUtils.mergeFetchGroups(srcFetchGroup, dstFetchGroup));
                }
            }
        } else if (dstEntity instanceof AbstractNotPersistentEntity) {
            BaseEntityInternalAccess.setNew((AbstractNotPersistentEntity) dstEntity, BaseEntityInternalAccess.isNew((BaseGenericIdEntity) srcEntity));
        }
    }

    /*
     * (1) src.new -> dst.new : copy all non-null                                   - should not happen (happens in setParent?)
     * (2) src.new -> dst.det : do nothing                                          - should not happen
     * (3) src.det -> dst.new : copy all loaded, make detached                      - normal situation after commit
     * (4) src.det -> dst.det : if src.version >= dst.version, copy all loaded      - normal situation after commit (and in setParent?)
     *                          if src.version < dst.version, do nothing            - should not happen
     */
    protected void mergeState(Entity srcEntity, Entity dstEntity, Set<Entity> mergedSet) {
        EntityStates entityStates = getEntityStates();

        boolean srcNew = entityStates.isNew(srcEntity);
        boolean dstNew = entityStates.isNew(dstEntity);
        if (srcNew && !dstNew) {
            return;
        }

        boolean replaceCollections = dstNew && !srcNew;

        if (!srcNew && !dstNew) {
            if (srcEntity instanceof Versioned) {
                int srcVer = Numbers.nullToZero(((Versioned) srcEntity).getVersion());
                int dstVer = Numbers.nullToZero(((Versioned) dstEntity).getVersion());
                if (srcVer < dstVer) {
                    return;
                }
                replaceCollections = srcVer > dstVer;
            }
        }

        copySystemState(srcEntity, dstEntity);

        for (MetaProperty property : getMetadata().getClassNN(srcEntity.getClass()).getProperties()) {
            String propertyName = property.getName();
            if (!property.isReadOnly()                                                     // read-write
                    && (srcNew || entityStates.isLoaded(srcEntity, propertyName))          // loaded src
                    && (dstNew || entityStates.isLoaded(dstEntity, propertyName))) {       // loaded dst

                Object value = srcEntity.getValue(propertyName);

                // ignore null values in new source entities
                if (srcNew && value == null) {
                    continue;
                }

                if (!property.getRange().isClass() || value == null) {
                    dstEntity.setValue(propertyName, value);
                    continue;
                }

                if (value instanceof Collection) {
                    if (value instanceof List) {
                        mergeList((List) value, dstEntity, property.getName(), replaceCollections, mergedSet);
                    } else if (value instanceof Set) {
                        mergeSet((Set) value, dstEntity, property.getName(), replaceCollections, mergedSet);
                    } else {
                        throw new UnsupportedOperationException("Unsupported collection type: " + value.getClass().getName());
                    }
                } else {
                    Entity srcRef = (Entity) value;
                    if (!mergedSet.contains(srcRef)) {
                        Entity managedRef = internalMerge(srcRef, mergedSet);
                        ((AbstractInstance) dstEntity).setValue(propertyName, managedRef, false);
                        if (getMetadataTools().isEmbedded(property)) {
                            EmbeddedPropertyChangeListener listener = new EmbeddedPropertyChangeListener(dstEntity);
                            managedRef.addPropertyChangeListener(listener);
                            embeddedPropertyListeners.computeIfAbsent(dstEntity, e -> new HashMap<>()).put(propertyName, listener);
                        }
                    } else {
                        Entity managedRef = find(srcRef.getClass(), srcRef.getId());
                        if (managedRef != null) {
                            ((AbstractInstance) dstEntity).setValue(propertyName, managedRef, false);
                        } else {
                            // should never happen
                            log.debug("Instance was merged but managed instance is null: {}", srcRef);
                        }
                    }
                }
            }
        }
    }

    protected void mergeList(List<Entity> list, Entity managedEntity, String propertyName, boolean replace,
                             Set<Entity> mergedSet) {
        if (replace) {
            List<Entity> managedRefs = new ArrayList<>(list.size());
            for (Entity entity : list) {
                Entity managedRef = internalMerge(entity, mergedSet);
                managedRefs.add(managedRef);
            }
            List<Entity> dstList = createObservableList(managedRefs, managedEntity);
            managedEntity.setValue(propertyName, dstList);

        } else {
            List<Entity> dstList = managedEntity.getValue(propertyName);
            if (dstList == null) {
                dstList = createObservableList(managedEntity);
                managedEntity.setValue(propertyName, dstList);
            }
            if (dstList.size() == 0) {
                for (Entity srcRef : list) {
                    dstList.add(internalMerge(srcRef, mergedSet));
                }
            } else {
                for (Entity srcRef : list) {
                    Entity managedRef = internalMerge(srcRef, mergedSet);
                    if (!dstList.contains(managedRef)) {
                        dstList.add(managedRef);
                    }
                }
            }
        }
    }

    protected void mergeSet(Set<Entity> set, Entity managedEntity, String propertyName, boolean replace,
                            Set<Entity> mergedSet) {
        if (replace) {
            Set<Entity> managedRefs = new LinkedHashSet<>(set.size());
            for (Entity entity : set) {
                Entity managedRef = internalMerge(entity, mergedSet);
                managedRefs.add(managedRef);
            }
            Set<Entity> dstList = createObservableSet(managedRefs, managedEntity);
            managedEntity.setValue(propertyName, dstList);

        } else {
            Set<Entity> dstSet = managedEntity.getValue(propertyName);
            if (dstSet == null) {
                dstSet = createObservableSet(managedEntity);
                managedEntity.setValue(propertyName, dstSet);
            }
            if (dstSet.size() == 0) {
                for (Entity srcRef : set) {
                    dstSet.add(internalMerge(srcRef, mergedSet));
                }
            } else {
                for (Entity srcRef : set) {
                    Entity managedRef = internalMerge(srcRef, mergedSet);
                    dstSet.add(managedRef);
                }
            }
        }
    }

    protected class PropertyChangeListener implements Instance.PropertyChangeListener {
        @Override
        public void propertyChanged(Instance.PropertyChangeEvent e) {
            if (!disableListeners) {
                modifiedInstances.add((Entity) e.getItem());
                fireChangeListener((Entity) e.getItem());
            }
        }
    }

    protected class EmbeddedPropertyChangeListener implements Instance.PropertyChangeListener {

        private final Entity entity;

        public EmbeddedPropertyChangeListener(Entity entity) {
            this.entity = entity;
        }

        @Override
        public void propertyChanged(Instance.PropertyChangeEvent e) {
            if (!disableListeners) {
                modifiedInstances.add(entity);
                fireChangeListener(entity);
            }
        }
    }
}
