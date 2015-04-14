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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Contract for a step in the contribution workflow.
 */
public interface Step {
    /**
     * Execute this step.
     *
     * @param workflow
     *         the contributors workflow.
     */
    void execute(@Nonnull final ContributorWorkflow workflow);

    /**
     * Returns the prerequisites for this step.
     * 
     * @return the prerequisites
     */
    @Nullable
    Collection< ? extends Prerequisite> getPrerequisites();

    StepIdentifier getStepId();
}
