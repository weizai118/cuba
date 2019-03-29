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

package spec.cuba.web.dialogs

import com.haulmont.chile.core.datatypes.impl.BigDecimalDatatype
import com.haulmont.chile.core.datatypes.impl.DateDatatype
import com.haulmont.chile.core.datatypes.impl.DateTimeDatatype
import com.haulmont.chile.core.datatypes.impl.DoubleDatatype
import com.haulmont.chile.core.datatypes.impl.IntegerDatatype
import com.haulmont.chile.core.datatypes.impl.StringDatatype
import com.haulmont.chile.core.datatypes.impl.TimeDatatype
import com.haulmont.cuba.gui.ComponentsHelper
import com.haulmont.cuba.gui.Dialogs
import com.haulmont.cuba.gui.app.core.inputdialog.InputDialog
import com.haulmont.cuba.gui.components.Button
import com.haulmont.cuba.gui.components.CheckBox
import com.haulmont.cuba.gui.components.DateField
import com.haulmont.cuba.gui.components.DialogAction
import com.haulmont.cuba.gui.components.Form
import com.haulmont.cuba.gui.components.HBoxLayout
import com.haulmont.cuba.gui.components.PickerField
import com.haulmont.cuba.gui.components.TextField
import com.haulmont.cuba.gui.components.TimeField
import com.haulmont.cuba.gui.components.inputdialog.InputDialogAction
import com.haulmont.cuba.gui.screen.OpenMode
import com.haulmont.cuba.security.app.UserManagementService
import com.haulmont.cuba.web.testmodel.sample.GoodInfo
import com.haulmont.cuba.web.testsupport.TestServiceProxy
import spec.cuba.web.UiScreenSpec

import static com.haulmont.cuba.gui.app.core.inputdialog.InputDialog.INPUT_DIALOG_CANCEL_ACTION
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.bigDecimalParamater
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.booleanParameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.dateParameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.dateTimeParameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.doubleParameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.entityParameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.intParameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.longParameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.parameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.stringParameter
import static com.haulmont.cuba.gui.app.core.inputdialog.InputParameter.timeParameter

class InputDialogTest extends UiScreenSpec {

    void setup() {
        TestServiceProxy.mock(UserManagementService, Mock(UserManagementService) {
            getSubstitutedUsers(_) >> Collections.emptyList()
        })
    }

    def cleanup() {
        TestServiceProxy.clear()

        resetScreensConfig()
    }

    def "check input parameter ids"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def dialogs = ComponentsHelper.getScreenContext(mainWindow.getWindow().getFrame()).getDialogs()

        when: "the same id is used"
        dialogs.createInputDialog(mainWindow)
                .withParameters(
                parameter("same"),
                parameter("same"),
                parameter("not the same"))
                .show()
        then:
        thrown(IllegalArgumentException)

        when: "different ids are used"
        InputDialog dialog = dialogs.createInputDialog(mainWindow)
                .withParameters(
                parameter("not the same 1"),
                parameter("not the same 2"),
                parameter("not the same 3"))
                .build()
        then:
        dialog.show()
    }

    /*
    def "check input parameter types are presented"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def dialogs = ComponentsHelper.getScreenContext(mainWindow.getWindow().getFrame()).getDialogs()

        when: "all types are used"
        InputDialog dialog = dialogs.createInputDialog(mainWindow)
                .withParameters(
                parameter("default"),
                stringParameter("string"),
                intParameter("int"),
                doubleParameter("double"),
                longParameter("long"),
                bigDecimalParamater("bigDecimal"),
                booleanParameter("boolean"),
                entityParameter("entity", GoodInfo),
                timeParameter("time"),
                dateParameter("date"),
                dateTimeParameter("dateTime"))
                .show()
        then:
        def form = dialog.getWindow().getComponentNN("form") as Form

        def defaultField = form.getComponentNN("default") as TextField
        defaultField.getDatatype().getClass() == StringDatatype

        def stringField = form.getComponentNN("string") as TextField
        stringField.getDatatype().getClass() == StringDatatype

        def intField = form.getComponentNN("int") as TextField
        intField.getDatatype().getClass() == IntegerDatatype

        def doubleField = form.getComponentNN("double") as TextField
        doubleField.getDatatype().getClass() == DoubleDatatype

        def bigDecimalField = form.getComponentNN("bigDecimal") as TextField
        bigDecimalField.getDatatype().getClass() == BigDecimalDatatype

        form.getComponentNN("bigDecimal") as CheckBox
        form.getComponentNN("entity") as PickerField

        def timeField = form.getComponentNN("time") as TimeField
        timeField.getDatatype().getClass() == TimeDatatype

        def dateField = form.getComponentNN("date") as DateField
        dateField.getDatatype().getClass() == DateDatatype

        def dateTimeField = form.getComponentNN("dateTime") as DateField
        dateTimeField.getDatatype().getClass() == DateTimeDatatype
    }

    def "check default actions are created"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def dialogs = ComponentsHelper.getScreenContext(mainWindow.getWindow().getFrame()).getDialogs()

        when: "YES NO CANCEL are created"
        InputDialog dialog = dialogs.createInputDialog(mainWindow)
                .withParameters(
                parameter("default"),
                stringParameter("string"))
                .withActions(Dialogs.DialogActions.YES_NO_CANCEL)
                .show()
        then:
        def actionsLayout = dialog.getWindow().getComponentNN("actionsLayout") as HBoxLayout
        actionsLayout.getComponents().size() == 4

        // YES action
        def yesBtn = actionsLayout.getComponent(1) as Button
        def yesAction = yesBtn.getAction() as DialogAction
        yesAction.getType() == DialogAction.Type.YES

        // NO action
        def noBtn = actionsLayout.getComponent(2) as Button
        def noAction = noBtn.getAction() as DialogAction
        noAction.getType() == DialogAction.Type.NO

        // CANCEL action
        def cancelBtn = actionsLayout.getComponent(3) as Button
        def cancelAction = cancelBtn.getAction() as DialogAction
        cancelAction.getType() == DialogAction.Type.CANCEL
    }

    def "check default actions with result handler"() {

    }

    def "check custom input dialog actions"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def dialogs = ComponentsHelper.getScreenContext(mainWindow.getWindow().getFrame()).getDialogs()
        def dateValue = new Date()
        def stringValue = "Default value"

        when: "created custom action"
        InputDialog dialog = dialogs.createInputDialog(mainWindow)
                .withParameters(
                parameter("string").withDefaultValue(),
                dateParameter("date").withDefaultValue(dateValue))
                .withActions(
                new InputDialogAction("ok").withHandler({
                    InputDialogAction.InputDialogActionPerformed event ->
                        InputDialog dialog = event.getInputDialog()

                        dialog.getValue("string") == stringValue
                        dialog.getValue("date") == dateValue
                }),
                new InputDialogAction("cancel").withHandler({
                    InputDialogAction.InputDialogActionPerformed event ->
                        event.getInputDialog().close(INPUT_DIALOG_CANCEL_ACTION)
                }))
                .show()

        then:
        def actionsLayout = dialog.getWindow().getComponentNN("actionsLayout") as HBoxLayout
        actionsLayout.getComponents().size() == 3

        def okBtn = actionsLayout.getComponent(1) as Button
        okBtn.getAction().actionPerform(okBtn)

        def cancelBtn = actionsLayout.getComponent(2) as Button
        cancelBtn.getAction().actionPerform(cancelBtn)

        !screens.getOpenedScreens().getActiveScreens().contains(dialog)
    }
    */
}
