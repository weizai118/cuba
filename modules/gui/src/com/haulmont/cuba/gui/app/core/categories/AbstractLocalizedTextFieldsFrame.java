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

package com.haulmont.cuba.gui.app.core.categories;

import com.haulmont.cuba.core.entity.AttrLocalizationNameDescr;
import com.haulmont.cuba.core.entity.LocaleHelper;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.actions.list.EditAction;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.data.DataGridItems;
import com.haulmont.cuba.gui.components.data.datagrid.ContainerDataGridItems;
import com.haulmont.cuba.gui.model.impl.CollectionContainerImpl;

import javax.inject.Inject;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractLocalizedTextFieldsFrame extends AbstractFrame {

    @Inject
    protected GlobalConfig globalConfig;
    @Inject
    protected ScrollBoxLayout localesScrollBox;
    @Inject
    protected UiComponents uiComponents;
    @Inject
    private Actions actions;
    @Inject
    private Notifications notifications;
    @Inject
    protected Metadata metadata;

    protected static String LANGUAGE = "language";
    protected static String NAME = "name";
    protected static String DESCRIPTION = "description";
    protected static String DATAGRID_LOCALIZATION_HEADER_STYLENAME = "datagrid-localization-header";

    protected List<AttrLocalizationNameDescr> attrLocalizationNameDescrList = new ArrayList<>();
    DataGrid<AttrLocalizationNameDescr> dataGrid;

    @Override
    public void init(Map<String, Object> params) {
        Map<String, Locale> map = globalConfig.getAvailableLocales();

        dataGrid = uiComponents.create(DataGrid.NAME);
        initEditAction(dataGrid);
        dataGrid.setWidth("100%");

        dataGrid.setItems(getDataGridItems(map));
        dataGrid.setSortable(false);
        dataGrid.setColumnReorderingAllowed(false);

        configureColumns(dataGrid);

        dataGrid.getDefaultHeaderRow().setStyleName(DATAGRID_LOCALIZATION_HEADER_STYLENAME);
        localesScrollBox.add(dataGrid);
    }

    protected DataGridItems<AttrLocalizationNameDescr> getDataGridItems(Map<String, Locale> map) {
        CollectionContainerImpl<AttrLocalizationNameDescr> collectionContainer =
                new CollectionContainerImpl<>(metadata.getClass(AttrLocalizationNameDescr.class));
        for (Map.Entry<String, Locale> entry : map.entrySet()) {
            AttrLocalizationNameDescr attrLocalizationNameDescr = metadata.create(AttrLocalizationNameDescr.class);
            attrLocalizationNameDescr.setLanguage(entry.getKey());
            attrLocalizationNameDescr.setLocale(entry.getValue());
            attrLocalizationNameDescrList.add(attrLocalizationNameDescr);
        }
        collectionContainer.setItems(attrLocalizationNameDescrList);
        return new ContainerDataGridItems<>(collectionContainer);
    }

    protected void initEditAction(DataGrid<AttrLocalizationNameDescr> dataGrid) {
        dataGrid.setEditorEnabled(true);
        EditAction editAction = (EditAction) actions.create(EditAction.ID);
        editAction.withHandler(actionPerformedEvent -> {
            AttrLocalizationNameDescr selected = dataGrid.getSingleSelected();
            if (selected != null) {
                dataGrid.edit(selected);
            } else {
                notifications.create()
                        .withCaption("Item is not selected")
                        .show();
            }
        });
        dataGrid.addAction(editAction);
    }

    protected void setValues(String localeBundle, BiConsumer<AttrLocalizationNameDescr, String> reference) {
        Map<String, String> localizedNamesMap = LocaleHelper.getLocalizedValuesMap(localeBundle);
        for (AttrLocalizationNameDescr attrLocalizationNameDescr : attrLocalizationNameDescrList) {
            reference.accept(attrLocalizationNameDescr,
                    localizedNamesMap.get(attrLocalizationNameDescr.getLocale().toString()));
        }
    }

    protected String getValues(Function<AttrLocalizationNameDescr, String> reference) {
        Properties properties = new Properties();
        for (AttrLocalizationNameDescr attrLocalizationNameDescr : attrLocalizationNameDescrList) {
            if (attrLocalizationNameDescr.getName() != null) {
                properties.put(attrLocalizationNameDescr.getLocale().toString(), reference.apply(attrLocalizationNameDescr));
            }
        }
        return LocaleHelper.convertPropertiesToString(properties);
    }

    protected abstract void configureColumns(DataGrid<AttrLocalizationNameDescr> dataGrid);
}
