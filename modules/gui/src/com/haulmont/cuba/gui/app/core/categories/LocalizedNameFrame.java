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

import com.haulmont.cuba.core.entity.AttributeLocaleData;
import com.haulmont.cuba.gui.components.*;

import java.util.*;

public class LocalizedNameFrame extends AbstractLocalizedTextFieldsFrame {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
    }

    @Override
    protected void configureColumns(DataGrid<AttributeLocaleData> dataGrid) {
        DataGrid.Column<AttributeLocaleData> langColumn = dataGrid.getColumnNN(LANGUAGE);
        DataGrid.Column<AttributeLocaleData> nameColumn = dataGrid.getColumnNN(NAME);

        nameColumn.setDescriptionProvider(attributeLocaleData -> "Double click to edit the value");

        dataGrid.removeColumn(LANGUAGE);
        dataGrid.removeColumn(NAME);
        dataGrid.removeColumn(DESCRIPTION);

        langColumn.setResizable(false);
        nameColumn.setResizable(false);

        langColumn.setExpandRatio(1);
        nameColumn.setExpandRatio(3);

        langColumn.setEditable(false);
        nameColumn.setEditable(true);

        dataGrid.addColumn(langColumn, 0);
        dataGrid.addColumn(nameColumn, 1);
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
        for (DataGrid.Column column : dataGrid.getColumns()) {
            column.setEditable(editable);
        }
    }
}
