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
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.plugin.contribution.client.steps.events.StepEvent;
import com.codenvy.plugin.contribution.client.steps.events.WorkflowModeEvent;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.WorkflowModeEvent.Mode;

/**
 * This class is responsible to maintain the context between the different steps and to maintain the state of the contribution workflow.
 *
 * @author Kevin Pollet
 */
public class ContributorWorkflow {
    private final Context       context;
    private final EventBus      eventBus;
    private final Configuration configuration;
    private       Step          step;

    @Inject
    public ContributorWorkflow(@Nonnull final Context context,
                               @Nonnull final DtoFactory dtoFactory,
                               @Nonnull final EventBus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        this.configuration = dtoFactory.createDto(Configuration.class);
    }

    /**
     * Executes the current step.
     */
    public void executeStep() {
        step.execute(this);
    }

    /**
     * Sets the new current step.
     *
     * @param currentStep
     *         the current step.
     */
    public void setStep(@Nonnull final Step currentStep) {
        this.step = currentStep;
    }

    /**
     * Returns the contributor workflow context object.
     *
     * @return the contributor workflow context object.
     */
    @Nonnull
    public Context getContext() {
        return context;
    }

    /**
     * Returns the contributor workflow configuration object.
     *
     * @return the contributor workflow configuration object.
     */
    @Nonnull
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is successfully done.
     *
     * @param step
     *         the successfully done step.
     */
    void fireStepDoneEvent(StepEvent.Step step) {
        eventBus.fireEvent(new StepEvent(step, true));
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is in error.
     *
     * @param step
     *         the step in error.
     */
    void fireStepErrorEvent(StepEvent.Step step) {
        eventBus.fireEvent(new StepEvent(step, false));
    }

    /**
     * Fires an {@link com.codenvy.plugin.contribution.client.steps.events.WorkflowModeEvent} indicating that we have to switch to the
     * update
     * or contribute mode.
     *
     * @param mode
     *         the workflow mode.
     */
    void fireWorkflowModeChangeEvent(Mode mode) {
        eventBus.fireEvent(new WorkflowModeEvent(mode));
    }
}
