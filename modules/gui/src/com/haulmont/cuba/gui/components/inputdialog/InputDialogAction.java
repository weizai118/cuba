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

package com.haulmont.cuba.gui.components.inputdialog;

import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.app.core.inputdialog.InputDialog;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.actions.BaseAction;

/**
 * Action can be used in {@link InputDialog}. It can handle specific {@link InputDialogActionPerformed} event for
 * managing opened dialog.
 *
 * @see InputDialog
 * @see Dialogs.InputDialogBuilder
 */
public class InputDialogAction extends BaseAction {

    public InputDialogAction(String id) {
        super(id);
    }

    @Override
    public void actionPerform(Component component) {
        if (eventHub != null) {

            InputDialog inputDialog = null;
            if (component instanceof Component.BelongToFrame) {
                Window window = ComponentsHelper.getWindow((Component.BelongToFrame) component);
                if (window != null) {
                    inputDialog = (InputDialog) window.getFrameOwner();
                }
            }

            ActionPerformedEvent event = new InputDialogActionPerformed(this, component, inputDialog);
            eventHub.publish(ActionPerformedEvent.class, event);
        }
    }

    /**
     * Describes action performed event from {@link InputDialogAction}. It contains opened {@link InputDialog} .
     */
    static public class InputDialogActionPerformed extends Action.ActionPerformedEvent {

        protected InputDialog inputDialog;

        public InputDialogActionPerformed(Action source, Component component, InputDialog inputDialog) {
            super(source, component);

            this.inputDialog = inputDialog;
        }

        /**
         * @return opened input dialog
         */
        public InputDialog getInputDialog() {
            return inputDialog;
        }
    }
}
