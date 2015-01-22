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
 * Event sent when a step is done or in error.
 *
 * @author Kevin Pollet
 */
public class StepEvent extends GwtEvent<StepHandler> {
    /** Type class used to register this event. */
    public static Type<StepHandler> TYPE = new Type<>();

    /** The step. */
    private final Step step;

    /** The done step status. */
    private final boolean success;

    public StepEvent(final Step step, final boolean success) {
        this.step = step;
        this.success = success;
    }

    @Override
    public Type<StepHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final StepHandler handler) {
        if (success) {
            handler.onStepDone(this);

        } else {
            handler.onStepError(this);
        }
    }

    public Step getStep() {
        return step;
    }

    public enum Step {
        COMMIT_WORKING_TREE,
        AUTHORIZE_CODENVY_ON_VCS_HOST,
        CREATE_FORK,
        RENAME_WORK_BRANCH,
        ADD_FORK_REMOTE,
        PUSH_BRANCH_ON_FORK,
        ISSUE_PULL_REQUEST,
        GENERATE_REVIEW_FACTORY,
        ADD_REVIEW_FACTORY_LINK
    }
}
