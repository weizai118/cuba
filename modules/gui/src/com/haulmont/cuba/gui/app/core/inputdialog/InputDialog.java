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
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Dialogs.DialogActions;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.actions.picker.ClearAction;
import com.haulmont.cuba.gui.actions.picker.LookupAction;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.inputdialog.InputDialogAction;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.screen.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@UiDescriptor("inputdialog.xml")
@UiController("inputDialogScreen")
public class InputDialog extends Screen {

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
    protected ScreenValidation screenValidation;

    @Inject
    protected CssLayout rootLayout;

    @Inject
    protected Form form;

    @Inject
    protected HBoxLayout actionsLayout;

    protected List<InputParameter> parameters = new ArrayList<>(2);
    protected List<Action> actionsList = new ArrayList<>(2);

    protected DialogActions dialogActions = DialogActions.OK_CANCEL;
    protected List<String> fieldIds;
    protected Consumer<InputDialogCloseEvent> closeListener;

    @Subscribe
    private void onBeforeShow(BeforeShowEvent event) {
        initParameters();
        if (actionsList.isEmpty()) {
            initDialogActions();
        } else {
            initActions(actionsList);
        }
    }

    @Subscribe
    protected void onAfterClose(AfterCloseEvent event) {
        if (closeListener != null) {
            InputDialogCloseEvent inputDialogCloseEvent = new InputDialogCloseEvent(getValues(), event.getCloseAction());
            closeListener.accept(inputDialogCloseEvent);
        }
    }

    /**
     * Returns value from field by id.
     *
     * @param id field id
     * @return field value
     * @throws IllegalArgumentException exception if wrong id is sent
     */
    public Object getValue(String id) {
        Component component = form.getComponentNN(id);
        if (component instanceof Field) {
            return ((Field) component).getValue();
        }

        throw new IllegalArgumentException("InputDialog doesn't contains Field with id: " + id);
    }

    /**
     * @return dialog window in which you can set dialog properties (e.g. modal, resizable, etc)
     */
    public DialogWindow getDialogWindow() {
        return (DialogWindow) getWindow();
    }

    /**
     * Returns mapped values from fields. String - field id, Object - field value.
     *
     * @return values
     */
    public Map<String, Object> getValues() {
        ParamsMap paramsMap = ParamsMap.of();

        for (String id : fieldIds) {
            Component component = form.getComponentNN(id);
            paramsMap.pair(id, ((Field) component).getValue());
        }

        return paramsMap.create();
    }

    /**
     * Add input parameter to the dialog. Input parameter will be represented as a field.
     *
     * @param parameter input parameter that will be added to the dialog
     */
    public void setParameter(InputParameter parameter) {
        parameters.add(parameter);
    }

    /**
     * Sets input parameters.
     *
     * @param parameters input parameters
     */
    public void setParameters(InputParameter... parameters) {
        this.parameters.addAll(Arrays.asList(parameters));
    }

    /**
     * @return input parameters from dialog
     */
    public List<InputParameter> getParameters() {
        return parameters;
    }

    /**
     * Add close listener to the dialog.
     *
     * @param listener close listener to add
     */
    public void setCloseListener(Consumer<InputDialogCloseEvent> listener) {
        this.closeListener = listener;
    }

    /**
     * @return close listener
     */
    public Consumer<InputDialogCloseEvent> getCloseListener() {
        return closeListener;
    }

    /**
     * Sets dialog actions. If there is no actions are set input dialog will use {@link Dialogs.DialogActions#OK_CANCEL}.
     *
     * @param actions actions
     * @see InputDialogAction
     */
    public void setActions(Action... actions) {
        this.actionsList.addAll(Arrays.asList(actions));
    }

    /**
     * @return actions list
     */
    public List<Action> getActions() {
        return actionsList;
    }

    /**
     * Sets predefined dialog actions. By default if there is no actions are set using {@link #setActions(Action...)}
     * input dialog will use {@link Dialogs.DialogActions#OK_CANCEL}.
     *
     * @param actions actions
     */
    public void setDialogActions(DialogActions actions) {
        this.dialogActions = actions;
    }

    /**
     * Returns predefined dialog actions. {@link Dialogs.DialogActions#OK_CANCEL} by default.
     *
     * @return dialog actions
     */
    public DialogActions getDialogActions() {
        return dialogActions;
    }

    @SuppressWarnings("unchecked")
    protected void initParameters() {
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
            field.setStyleName("input-parameter");

            if (fieldIds.contains(parameter.getId())) {
                throw new IllegalArgumentException("InputDialog cannot contain fields with the same id: " + parameter.getId());
            }

            fieldIds.add(field.getId());
            form.add(field);
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
        if (type == DialogAction.Type.OK || type == DialogAction.Type.YES) {
            dialogAction.withHandler(event -> {
                if (validateFields()) {
                    close(closeAction);
                }
            });
        } else {
            dialogAction.withHandler(event -> close(closeAction));
        }
        return dialogAction;
    }

    protected boolean validateFields() {
        ValidationErrors validationErrors = screenValidation.validateUiComponents(getWindow());
        if (!validationErrors.isEmpty()) {
            screenValidation.showValidationErrors(this, validationErrors);
            return false;
        }
        return true;
    }

    /**
     * Describes InputDialog close event.
     */
    public static class InputDialogCloseEvent {
        protected CloseAction closeAction;
        protected Map<String, Object> values;

        public InputDialogCloseEvent(Map<String, Object> values, CloseAction closeAction) {
            this.values = values;
            this.closeAction = closeAction;
        }

        /**
         * @return close action
         */
        public CloseAction getCloseAction() {
            return closeAction;
        }

        /**
         * Returns mapped values from fields. String - field id, Object - field value.
         *
         * @return values
         */
        public Map<String, Object> getValues() {
            return values;
        }

        /**
         * Returns value from field by id.
         *
         * @param id field id
         * @return field value
         */
        @Nullable
        public Object getValue(String id) {
            return values.get(id);
        }
    }
}
