/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.steps.event;

import com.google.gwt.event.shared.GwtEvent;

public class UpdateModeEvent extends GwtEvent<UpdateModeHandler> {
    /** Type class used to register this event. */
    public static Type<UpdateModeHandler> TYPE = new Type<>();

    private final State state;

    public UpdateModeEvent(final State state) {
        this.state = state;
    }

    @Override
    public Type<UpdateModeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final UpdateModeHandler handler) {
        handler.onUpdateModeChange(this);
    }

    public State getState() {
        return state;
    }

    public enum State {
        START_UPDATE_MODE,
        STOP_UPDATE_MODE
    }
}
