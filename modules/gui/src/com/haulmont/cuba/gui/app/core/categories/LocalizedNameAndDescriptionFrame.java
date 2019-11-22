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

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.gui.components.*;

import javax.inject.Inject;
import java.util.*;

public class LocalizedNameAndDescriptionFrame extends AbstractLocalizedTextFieldsFrame {

    private static final String MESSAGE_PACK = "msg://com.haulmont.cuba.core.entity/";

    @Inject
    protected ScrollBoxLayout localesScrollBox;

    @Inject
    protected GlobalConfig globalConfig;

    @Inject
    protected MessageTools messageTools;

    protected Map<Locale, TextField> namesTextFieldMap = new HashMap<>();
    protected Map<Locale, TextArea> descriptionsTextFieldMap = new HashMap<>();

    @Override
    public void init(Map<String, Object> params) {
        Map<String, Locale> map = globalConfig.getAvailableLocales();

        GridLayout grid = uiComponents.create(GridLayout.class);
        grid.setSpacing(false);
        grid.setStyleName("v-gridlayout-localization");
        grid.setColumns(3);
        grid.setRows(map.size() + 1);
        grid.setWidth("100%");
        initGridHeaders(grid);
        initGridContent(grid, map);
        localesScrollBox.add(grid);
    }

    protected void initGridHeaders(GridLayout grid) {
        Label langLabel = uiComponents.create(Label.of(String.class));
        Label nameLabel = uiComponents.create(Label.of(String.class));
        Label descLabel = uiComponents.create(Label.of(String.class));
        langLabel.setSizeFull();
        nameLabel.setSizeFull();
        descLabel.setSizeFull();
        langLabel.setStyleName("v-label-localization-header");
        nameLabel.setStyleName("v-label-localization-header");
        descLabel.setStyleName("v-label-localization-header");
        langLabel.setValue("Language");
        nameLabel.setValue("Name");
        descLabel.setValue("Description");
        grid.add(langLabel, 0, 0);
        grid.add(nameLabel, 1, 0);
        grid.add(descLabel, 2, 0);
        grid.setColumnExpandRatio(0,1);
        grid.setColumnExpandRatio(1,4);
        grid.setColumnExpandRatio(2,5);
    }

    protected void initGridContent(GridLayout grid, Map<String, Locale> map) {
        int rowNumber = 0;
        for (Map.Entry<String, Locale> entry : map.entrySet()) {
            rowNumber++;
            Label langLabel = uiComponents.create(Label.of(String.class));
            langLabel.setStyleName("v-label-localization-locale");
            langLabel.setValue(entry.getKey() + "|" + entry.getValue().toString());
            langLabel.setAlignment(Alignment.MIDDLE_LEFT);

            TextField attrName = (TextField) createTextFieldComponent(entry.getValue(),
                    messageTools.loadString(MESSAGE_PACK + "CategoryAttribute.name"), namesTextFieldMap);
            attrName.setStyleName("v-textfield-padding");
            attrName.setCaption(null);
            attrName.setHeight("50px");
            attrName.setWidth("92%");

            TextArea attrDescription = (TextArea) createTextAreaComponent(entry.getValue(),
                    messageTools.loadString(MESSAGE_PACK + "CategoryAttribute.description"), descriptionsTextFieldMap);
            attrDescription.setStyleName("v-textarea-padding");
            attrDescription.setCaption(null);
            attrDescription.setHeight("50px");
            attrDescription.setWidth("92%");

            grid.add(langLabel, 0, rowNumber);
            grid.add(attrName, 1, rowNumber);
            grid.add(attrDescription, 2, rowNumber);
        }
    }

    public String getNamesValue() {
        return getValue(namesTextFieldMap);
    }

    public String getDescriptionsValue() {
        return getValue(descriptionsTextFieldMap);
    }

    public void setNamesValue(String localeBundle) {
        setValue(localeBundle, namesTextFieldMap);
    }

    public void setDescriptionsValue(String localeBundle) {
        setValue(localeBundle, descriptionsTextFieldMap);
    }
}
