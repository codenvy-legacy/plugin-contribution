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
package com.codenvy.plugin.contribution.client.steps.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event sent when the workflow switch between the contribute and update mode.
 */
public class WorkflowModeEvent extends GwtEvent<WorkflowModeHandler> {
    /** Type class used to register this event. */
    public static Type<WorkflowModeHandler> TYPE = new Type<>();

    private final Mode mode;

    public WorkflowModeEvent(final Mode mode) {
        this.mode = mode;
    }

    @Override
    public Type<WorkflowModeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final WorkflowModeHandler handler) {
        handler.onWorkflowModeChange(this);
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        UPDATE,
        CONTRIBUTE
    }
}
