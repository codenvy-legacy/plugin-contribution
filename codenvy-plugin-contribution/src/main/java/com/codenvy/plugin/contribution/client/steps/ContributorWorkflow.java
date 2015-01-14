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
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * This class is responsible to maintain the context between the different steps and to maintain the state of the contribution workflow.
 *
 * @author Kevin Pollet
 */
public class ContributorWorkflow {
    private final Context       context;
    private final Configuration configuration;
    private       Step          step;

    @Inject
    public ContributorWorkflow(@Nonnull final Context context,
                               @Nonnull final DtoFactory dtoFactory,
                               @Nonnull final AuthenticateUserStep authenticateUserStep) {
        this.context = context;
        this.configuration = dtoFactory.createDto(Configuration.class);
        this.step = authenticateUserStep; //initial state
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
}
