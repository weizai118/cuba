/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.gui.builders;

import com.haulmont.cuba.gui.screen.CloseAction;
import com.haulmont.cuba.gui.screen.FrameOwner;
import com.haulmont.cuba.gui.screen.LookupScreen;
import com.haulmont.cuba.gui.screen.Screen;

import java.util.EventObject;

public class AfterScreenCloseEvent<S extends Screen> extends EventObject {

    protected final CloseAction closeAction;

    public AfterScreenCloseEvent(S source, CloseAction closeAction) {
        super(source);
        this.closeAction = closeAction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public S getSource() {
        return (S) super.getSource();
    }

    @SuppressWarnings("unchecked")
    public S getScreen() {
        return (S) super.getSource();
    }

    /**
     * @return action passed to the {@link Screen#close(CloseAction)} method of the screen.
     */
    public CloseAction getCloseAction() {
        return closeAction;
    }

    public boolean closedWithCommit() {
        return closeAction.equals(FrameOwner.WINDOW_COMMIT_AND_CLOSE_ACTION);
    }

    public boolean closedWithDiscard() {
        return closeAction.equals(FrameOwner.WINDOW_DISCARD_AND_CLOSE_ACTION);
    }

    public boolean closedWithSelect() {
        return closeAction.equals(LookupScreen.LOOKUP_SELECT_CLOSE_ACTION);
    }
}