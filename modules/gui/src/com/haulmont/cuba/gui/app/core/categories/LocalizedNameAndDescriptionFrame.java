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
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.gui.components.*;

import javax.inject.Inject;
import java.util.*;

public class LocalizedNameAndDescriptionFrame extends AbstractLocalizedTextFieldsFrame {

    @Inject
    protected MessageTools messageTools;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        dataGrid.setBodyRowHeight(60f);
    }

    @Override
    protected void configureColumns(DataGrid<AttrLocalizationNameDescr> dataGrid) {
        DataGrid.Column<AttrLocalizationNameDescr> langColumn = dataGrid.getColumnNN(LANGUAGE);
        DataGrid.Column<AttrLocalizationNameDescr> nameColumn = dataGrid.getColumnNN(NAME);
        DataGrid.Column<AttrLocalizationNameDescr> descColumn = dataGrid.getColumnNN(DESCRIPTION);

        langColumn.setResizable(false);
        nameColumn.setResizable(false);
        descColumn.setResizable(false);

        langColumn.setExpandRatio(1);
        nameColumn.setExpandRatio(3);
        descColumn.setExpandRatio(4);

        dataGrid.removeColumn(LANGUAGE);
        dataGrid.removeColumn(NAME);
        dataGrid.removeColumn(DESCRIPTION);

        langColumn.setEditable(false);
        nameColumn.setEditable(true);
        descColumn.setEditable(true);

        dataGrid.addColumn(langColumn, 0);
        dataGrid.addColumn(nameColumn, 1);
        dataGrid.addColumn(descColumn, 2);
    }

    protected String getNamesValue() {
        return getValues(AttrLocalizationNameDescr::getName);
    }

    protected String getDescriptionsValue() {
        return getValues(AttrLocalizationNameDescr::getDescription);
    }

    protected void setNamesValue(String localeBundle) {
        setValues(localeBundle, AttrLocalizationNameDescr::setName);
    }

    protected void setDescriptionsValue(String localeBundle) {
        setValues(localeBundle, AttrLocalizationNameDescr::setDescription);
    }
}
