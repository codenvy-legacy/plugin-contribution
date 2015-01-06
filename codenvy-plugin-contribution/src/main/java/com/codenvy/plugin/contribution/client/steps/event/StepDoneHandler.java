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

import com.google.gwt.event.shared.EventHandler;

import javax.annotation.Nonnull;

import static com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent.Step;

/**
 * Handler for step event.
 *
 * @author Kevin Pollet
 */
public interface StepDoneHandler extends EventHandler {
    /**
     * Called when a step is done.
     *
     * @param step
     *         the finished step.
     */
    void onStepDone(@Nonnull Step step);
}
