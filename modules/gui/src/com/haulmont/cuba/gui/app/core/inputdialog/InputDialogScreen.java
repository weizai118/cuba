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

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.DatatypeRegistry;
import com.haulmont.chile.core.datatypes.impl.*;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.Dialogs.DialogActions;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.actions.picker.ClearAction;
import com.haulmont.cuba.gui.actions.picker.LookupAction;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.inputdialog.InputDialog;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.screen.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UiDescriptor("inputdialog-screen.xml")
@UiController("cuba_InputDialogScreen")
public class InputDialogScreen extends Screen implements InputDialog {

    @Inject
    protected UiComponents uiComponents;

    @Inject
    protected DatatypeRegistry datatypeRegistry;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Actions actions;

    @Inject
    protected Messages messages;

    @Inject
    protected Icons icons;

    @Inject
    protected CssLayout rootLayout;

    @Inject
    protected Form form;

    @Inject
    protected HBoxLayout actionsLayout;

    protected DialogActions dialogActions = DialogActions.OK_CANCEL;
    protected List<String> fieldIds;

    @Subscribe
    @SuppressWarnings("unchecked")
    private void onInit(InitEvent event) {
        ScreenOptions screenOptions = event.getOptions();
        if (screenOptions instanceof MapScreenOptions) {
            Map<String, Object> values = ((MapScreenOptions) screenOptions).getParams();

            List<InputParameter> parameters = (List<InputParameter>) values.get("parameters");
            if (!parameters.isEmpty()) {
                initParameters(parameters);
            }

            List<Action> actions = (List<Action>) values.get("actions");
            if (!actions.isEmpty()) {
                initActions(actions);
            } else {
                if (values.get("dialogActions") != null) {
                    dialogActions = (DialogActions) values.get("dialogActions");
                }

                initDialogActions();
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void initParameters(List<InputParameter> parameters) {
        fieldIds = new ArrayList<>(parameters.size());

        for (InputParameter parameter : parameters) {
            Field field;
            if (parameter.getField() != null) {
                field = parameter.getField().get();
            } else {
                field = createField(parameter);
            }

            field.setId(parameter.getId());
            field.setCaption(parameter.getCaption());
            field.setWidthFull();
            field.setRequired(parameter.isRequired());
            field.setValue(parameter.getDefaultValue());

            form.add(field);
            fieldIds.add(field.getId());
        }
    }

    @SuppressWarnings("unchecked")
    protected Field createField(InputParameter parameter) {
        Datatype datatype = null;
        if (parameter.getDatatypeJavaClass() != null) {
            datatype = datatypeRegistry.get(parameter.getDatatypeJavaClass());
        } else if (parameter.getDatatype() != null) {
            datatype = parameter.getDatatype();
        } else if (parameter.getEntityClass() == null) {
            datatype = datatypeRegistry.get(String.class);
        }

        if (datatype instanceof NumberDatatype
                || datatype instanceof StringDatatype) {
            TextField field = uiComponents.create(TextField.NAME);
            field.setDatatype(datatype);
            return field;
        } else if (datatype instanceof DateDatatype) {
            DateField dateField = uiComponents.create(DateField.NAME);
            dateField.setDatatype(datatype);
            dateField.setResolution(DateField.Resolution.DAY);
            return dateField;
        } else if (datatype instanceof DateTimeDatatype) {
            DateField dateField = uiComponents.create(DateField.NAME);
            dateField.setDatatype(datatype);
            dateField.setResolution(DateField.Resolution.MIN);
            return dateField;
        } else if (datatype instanceof TimeDatatype) {
            TimeField timeField = uiComponents.create(TimeField.NAME);
            timeField.setDatatype(datatype);
            return timeField;
        } else {
            PickerField pickerField = uiComponents.create(PickerField.NAME);
            pickerField.setMetaClass(metadata.getClass(parameter.getEntityClass()));
            pickerField.addAction(actions.create(LookupAction.ID));
            pickerField.addAction(actions.create(ClearAction.ID));
            return pickerField;
        }
    }

    protected void initActions(List<Action> actions) {
        actionsLayout.setMargin(true, false, false, false);

        for (Action action : actions) {
            Button button = uiComponents.create(Button.NAME);
            button.setAction(action);

            if (action instanceof DialogAction) {
                DialogAction.Type type = ((DialogAction) action).getType();
                button.setCaption(messages.getMainMessage(type.getMsgKey()));

                String iconPath = icons.get(type.getIconKey());
                button.setIcon(iconPath);
            } else {
                button.setIcon(action.getIcon());
                button.setCaption(action.getCaption());
            }

            actionsLayout.add(button);
        }
    }

    protected void initDialogActions() {
        List<Action> actions = new ArrayList<>(2);
        switch (dialogActions) {
            case OK:
                actions.add(createDialogAction(DialogAction.Type.OK, WINDOW_COMMIT_AND_CLOSE_ACTION));
                break;
            case YES_NO:
                actions.add(createDialogAction(DialogAction.Type.YES, WINDOW_COMMIT_AND_CLOSE_ACTION));
                actions.add(createDialogAction(DialogAction.Type.NO, WINDOW_DISCARD_AND_CLOSE_ACTION));
                break;
            case OK_CANCEL:
                actions.add(createDialogAction(DialogAction.Type.OK, WINDOW_COMMIT_AND_CLOSE_ACTION));
                actions.add(createDialogAction(DialogAction.Type.CANCEL, WINDOW_CLOSE_ACTION));
                break;
            case YES_NO_CANCEL:
                actions.add(createDialogAction(DialogAction.Type.OK, WINDOW_COMMIT_AND_CLOSE_ACTION));
                actions.add(createDialogAction(DialogAction.Type.NO, WINDOW_DISCARD_AND_CLOSE_ACTION));
                actions.add(createDialogAction(DialogAction.Type.CANCEL, WINDOW_CLOSE_ACTION));
                break;
        }
        initActions(actions);
    }

    protected DialogAction createDialogAction(DialogAction.Type type, CloseAction closeAction) {
        DialogAction dialogAction = new DialogAction(type);
        dialogAction.withHandler(event -> close(closeAction));
        return dialogAction;
    }

    @Override
    public Object getValue(String id) {
        Component component = form.getComponentNN(id);
        if (component instanceof Field) {
            return ((Field) component).getValue();
        }

        throw new IllegalArgumentException("InputDialog doesn't contains Field with id: '" + id + "'");
    }

    @Override
    public void closeDialog(CloseAction closeAction) {
        close(closeAction);
    }

    @Override
    public InputDialog showDialog() {
        return (InputDialog) super.show();
    }

    @Override
    public Map<String, Object> getValues() {
        ParamsMap paramsMap = ParamsMap.of();

        for (String id : fieldIds) {
            Component component = form.getComponentNN(id);
            paramsMap.pair(id, ((Field) component).getValue());
        }

        return paramsMap.create();
    }
}
