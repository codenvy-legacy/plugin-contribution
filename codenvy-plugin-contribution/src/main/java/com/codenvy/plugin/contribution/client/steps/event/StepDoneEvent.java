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

/**
 * Event sent when a step is successfully done.
 *
 * @author Kevin Pollet
 */
public class StepDoneEvent extends GwtEvent<StepDoneHandler> {
    /** Type class used to register this event. */
    public static Type<StepDoneHandler> TYPE = new Type<>();

    /** The step. */
    private final Step step;

    /** The done step status. */
    private final boolean success;

    public StepDoneEvent(final Step step, final boolean success) {
        this.step = step;
        this.success = success;
    }

    @Override
    public Type<StepDoneHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final StepDoneHandler handler) {
        handler.onStepDone(this);
    }

    public boolean isSuccess() {
        return success;
    }

    public Step getStep() {
        return step;
    }

    public enum Step {
        CREATE_FORK,
        PUSH_BRANCH,
        ISSUE_PULL_REQUEST
    }
}
