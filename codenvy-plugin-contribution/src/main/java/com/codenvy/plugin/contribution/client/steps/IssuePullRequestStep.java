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

import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;

import javax.inject.Inject;

/**
 * Create the pull request on the remote VCS repository.
 */
public class IssuePullRequestStep implements Step {

    /**
     * The following step.
     */
    private final Step nextStep;

    @Inject
    public IssuePullRequestStep(final GenerateReviewFactory nextStep) {
        this.nextStep = nextStep;
    }

    @Override
    public void execute(Context context, Configuration config) {
        // TODO Auto-generated method stub

    }

}
