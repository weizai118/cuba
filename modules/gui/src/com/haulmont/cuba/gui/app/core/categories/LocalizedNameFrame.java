/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.gui.app.core.categories;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.AttributeLocaleData;
import com.haulmont.cuba.gui.components.*;

import java.util.*;

public class LocalizedNameFrame extends AbstractLocalizedTextFieldsFrame {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
    }

    @Override
    protected void createColumns(DataGrid<AttributeLocaleData> dataGrid) {
        MetaClass metaClass = metadata.getClass(AttributeLocaleData.class);

        dataGrid.addColumn(LANGUAGE_WITH_CODE, metadataTools.resolveMetaPropertyPath(metaClass, LANGUAGE_WITH_CODE))
                .setCaption(messages.getMessage(AttributeLocaleData.class, LOCALIZATION_PREFIX + LANGUAGE_WITH_CODE));
        dataGrid.addColumn(NAME, metadataTools.resolveMetaPropertyPath(metaClass, NAME))
                .setCaption(messages.getMessage(AttributeLocaleData.class, LOCALIZATION_PREFIX + NAME));
    }

    @Override
    protected void configureColumns(DataGrid<AttributeLocaleData> dataGrid) {
        DataGrid.Column<AttributeLocaleData> langColumn = dataGrid.getColumnNN(LANGUAGE_WITH_CODE);
        DataGrid.Column<AttributeLocaleData> nameColumn = dataGrid.getColumnNN(NAME);

        setDescriptionProviders(langColumn, nameColumn);

        langColumn.setResizable(false);
        nameColumn.setResizable(false);

        langColumn.setExpandRatio(1);
        nameColumn.setExpandRatio(3);

        langColumn.setEditable(false);
    }

    protected void setDescriptionProviders(DataGrid.Column<AttributeLocaleData> langColumn,
                                           DataGrid.Column<AttributeLocaleData> nameColumn) {
        langColumn.setDescriptionProvider(attributeLocaleData -> getMessage("localeDataDescription"));
        nameColumn.setDescriptionProvider(attributeLocaleData -> {
            String nameValue = attributeLocaleData.getName() != null ? attributeLocaleData.getName() + "\n\n" : "";
            return nameValue +  getMessage("localeDataDescription");
        });
    }

    public String getValue() {
        return getValues(AttributeLocaleData::getName);
    }

    public void setValue(String localeBundle) {
        setValues(localeBundle, AttributeLocaleData::setName);
    }

    public void clearFields() {
        for (AttributeLocaleData attributeLocaleData : collectionContainer.getItems()) {
            attributeLocaleData.setName(null);
        }
    }

    public void setEditableFields(boolean editable) {
        dataGrid.getColumns().forEach(column -> column.setEditable(editable));
    }
}
