/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.steps.events;

import com.codenvy.plugin.contribution.client.steps.StepIdentifier;
import com.google.gwt.event.shared.GwtEvent;

import javax.annotation.Nonnull;

/**
 * Event sent when a step is done or in error.
 *
 * @author Kevin Pollet
 */
public class StepEvent extends GwtEvent<StepHandler> {
    public static Type<StepHandler> TYPE = new Type<>();

    private final StepIdentifier    step;
    private final boolean success;
    private final String  message;

    public StepEvent(@Nonnull final StepIdentifier step, final boolean success) {
        this(step, success, null);
    }

    public StepEvent(@Nonnull final StepIdentifier step, final boolean success, final String message) {
        this.step = step;
        this.success = success;
        this.message = message;
    }

    @Override
    public Type<StepHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(@Nonnull final StepHandler handler) {
        if (success) {
            handler.onStepDone(this);

        } else {
            handler.onStepError(this);
        }
    }

    public StepIdentifier getStep() {
        return step;
    }

    public String getMessage() {
        return message;
    }
}
