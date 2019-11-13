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

package com.haulmont.cuba.gui.screen;

public enum CloseResult {

    CLOSED(FrameOwner.WINDOW_CLOSE_ACTION),

    COMMITTED(FrameOwner.WINDOW_COMMIT_AND_CLOSE_ACTION),

    DISCARDED(FrameOwner.WINDOW_DISCARD_AND_CLOSE_ACTION),

    SELECTED(LookupScreen.LOOKUP_SELECT_CLOSE_ACTION);

    private CloseAction closeAction;

    CloseResult(CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public CloseAction getCloseAction() {
        return closeAction;
    }
}
