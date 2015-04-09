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
package com.codenvy.plugin.contribution.client.steps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.Call;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.dto.DtoFactory;

import com.codenvy.plugin.contribution.client.steps.events.StepEvent;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.web.bindery.event.shared.EventBus;

/**
 * This class is responsible to maintain the context between the different steps and to maintain the state of the contribution workflow.
 *
 * @author Kevin Pollet
 */
public class ContributorWorkflow {
    private final EventBus          eventBus;
    private final Step              initialStep;
    private final DtoFactory        dtoFactory;
    private       Provider<Context> contextProvider;
    private       Context           context;
    private       Configuration     configuration;
    private       Step              step;

    @Inject
    public ContributorWorkflow(@Nonnull final Provider<Context> contextProvider,
                               @Nonnull final EventBus eventBus,
                               @Nonnull final InitializeWorkflowContextStep initializeWorkflowContextStep,
                               @Nonnull final DtoFactory dtoFactory) {
        this.contextProvider = contextProvider;
        this.eventBus = eventBus;
        this.dtoFactory = dtoFactory;
        this.initialStep = initializeWorkflowContextStep;
    }

    /**
     * Initialize the contributor workflow to it's initial state.
     */
    public void init() {
        setStep(initialStep);
        context = contextProvider.get();
        configuration = dtoFactory.createDto(Configuration.class);
    }

    /**
     * Executes the current step.
     */
    public void executeStep() {
        final Collection< ? extends Prerequisite> prerequisites = this.step.getPrerequisites();
        if (prerequisites != null) {
            final List<Promise<Void>> prereqPromises = new ArrayList<>();
            for (final Prerequisite prereq : prerequisites) {
                if (prereq == null) {
                    continue;
                }
                final Promise<Void> promise = CallbackPromiseHelper.createFromCallback(new Call<Void, Throwable>() {
                    @Override
                    public void makeCall(final Callback<Void, Throwable> callback) {
                        prereq.fulfill(context, callback);
                    }
                });
                prereqPromises.add(promise);
            }

            @SuppressWarnings("rawtypes")
            final Promise[] promiseArray = prereqPromises.toArray(new Promise[prereqPromises.size()]);

            final Promise<JsArrayMixed> allPrereqs = Promises.all(promiseArray);
            allPrereqs.then(new Operation<JsArrayMixed>() {
                @Override
                public void apply(final JsArrayMixed notUsed) throws OperationException {
                    executeStepReady();
                }
            });
            allPrereqs.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(final PromiseError error) throws OperationException {
                    fireStepErrorEvent(step.getStepId());
                }
            });
        }
    }

    private void executeStepReady() {
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
    void fireStepDoneEvent(@Nonnull final StepIdentifier step) {
        eventBus.fireEvent(new StepEvent(step, true));
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is in error.
     *
     * @param step
     *         the step in error.
     */
    void fireStepErrorEvent(@Nonnull final StepIdentifier step) {
        fireStepErrorEvent(step, null);
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is in error.
     *
     * @param step
     *         the step in error.
     * @param errorMessage
     *         the error message.
     */
    void fireStepErrorEvent(@Nonnull final StepIdentifier step, final String errorMessage) {
        eventBus.fireEvent(new StepEvent(step, false, errorMessage));
    }
}
