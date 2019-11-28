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

import com.haulmont.cuba.core.entity.AttrLocalizationNameDescr;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.data.DataGridItems;
import com.haulmont.cuba.gui.components.data.datagrid.ContainerDataGridItems;
import com.haulmont.cuba.gui.model.impl.CollectionContainerImpl;

import javax.inject.Inject;
import java.util.*;

public class LocalizedNameFrame extends AbstractLocalizedTextFieldsFrame {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        dataGrid.setBodyRowHeight(40f);
    }

    @Override
    protected void configureColumns(DataGrid<AttrLocalizationNameDescr> dataGrid) {
        DataGrid.Column<AttrLocalizationNameDescr> langColumn = dataGrid.getColumnNN(LANGUAGE);
        DataGrid.Column<AttrLocalizationNameDescr> nameColumn = dataGrid.getColumnNN(NAME);

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
        return getValues(AttrLocalizationNameDescr::getName);
    }

    public void setValue(String localeBundle) {
        setValues(localeBundle, AttrLocalizationNameDescr::setName);
    }

    public void clearFields() {
        for (AttrLocalizationNameDescr attrLocalizationNameDescr : attrLocalizationNameDescrList) {
            attrLocalizationNameDescr.setName("");
        }
    }

    public void setEditableFields(boolean editable) {
        for (DataGrid.Column column : dataGrid.getColumns()) {
            column.setEditable(editable);
        }
    }
}
