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

package com.haulmont.cuba.gui.app.core.inputdialog;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.Field;

import java.util.Date;
import java.sql.Time;
import java.util.function.Supplier;

public class InputParameter {

    protected String id;
    protected String caption;
    protected boolean required;
    protected Datatype datatype;
    protected Supplier<Field> field;
    protected Object defaultValue;
    protected Class<? extends Entity> entityClass;

    protected Class datatypeJavaClass;

    public InputParameter(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public InputParameter withId(String id) {
        this.id = id;
        return this;
    }

    public String getCaption() {
        return caption;
    }

    public InputParameter withCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public InputParameter withRequired(boolean required) {
        this.required = required;
        return this;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public InputParameter withDatatype(Datatype datatype) {
        checkNullEntityClass("InputParameter cannot contain Datatype and entity class at the same time");
        checkNullDatatypeJavaClass("Datatype cannot be used with a parameter that has already data type");

        this.datatype = datatype;
        return this;
    }

    protected InputParameter withDatatypeJavaClass(Class javaClass) {
        this.datatypeJavaClass = javaClass;
        return this;
    }

    protected Class getDatatypeJavaClass() {
        return datatypeJavaClass;
    }

    public Supplier<Field> getField() {
        return field;
    }

    public InputParameter withField(Supplier<Field> field) {
        this.field = field;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public InputParameter withDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    public InputParameter withEntityClass(Class<? extends Entity> entityClass) {
        checkNullDatatype("InputParameter cannot contain entity class and Datatype at the same time");
        checkNullDatatypeJavaClass("Entity class cannot be used with a parameter that has data type");

        this.entityClass = entityClass;
        return this;
    }

    public static InputParameter stringParameter(String id) {
        return new InputParameter(id).withDatatypeJavaClass(String.class);
    }

    public static InputParameter intParameter(String id) {
        return new InputParameter(id).withDatatypeJavaClass(Integer.class);
    }

    public static InputParameter doubleParameter(String id) {
        return new InputParameter(id).withDatatypeJavaClass(Double.class);
    }

    public static InputParameter longParameter(String id) {
        return new InputParameter(id).withDatatypeJavaClass(Long.class);
    }

    public static InputParameter dateParameter(String id) {
        return new InputParameter(id).withDatatypeJavaClass(java.sql.Date.class);
    }

    public static InputParameter timeParameter(String id) {
        return new InputParameter(id).withDatatypeJavaClass(Time.class);
    }

    public static InputParameter dateTimeParameter(String id) {
        return new InputParameter(id).withDatatypeJavaClass(Date.class);
    }

    public static InputParameter entityParameter(String id, Class<? extends Entity> entityClass) {
        return new InputParameter(id).withEntityClass(entityClass);
    }


    protected void checkNullDatatype(String message) {
        if (datatype != null) {
            throw new IllegalStateException(message);
        }
    }

    protected void checkNullEntityClass(String message) {
        if (entityClass != null) {
            throw new IllegalStateException(message);
        }
    }

    protected void checkNullDatatypeJavaClass(String message) {
        if (datatypeJavaClass != null) {
            throw new IllegalStateException(message);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        InputParameter inputParameter = (InputParameter) obj;
        return id.equals(inputParameter.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
