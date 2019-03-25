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

import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.screen.CloseAction;

import javax.annotation.Nullable;
import java.util.Map;

public interface InputDialog {

    Map<String, Object> getValues();

    Object getValue(String id);

    void closeDialog(CloseAction closeAction);

    InputDialog showDialog();

    Window getWindow();

    class InputDialogCloseEvent {
        protected CloseAction closeAction;
        protected Map<String, Object> values;

        public InputDialogCloseEvent(Map<String, Object> values, CloseAction closeAction) {
            this.values = values;
            this.closeAction = closeAction;
        }

        public CloseAction getCloseAction() {
            return closeAction;
        }

        public Map<String, Object> getValues() {
            return values;
        }

        @Nullable
        public Object getValue(String key) {
            return values.get(key);
        }
    }
}