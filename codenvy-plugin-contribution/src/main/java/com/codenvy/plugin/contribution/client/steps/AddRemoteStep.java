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

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.VcsService;

/**
 * Adds the forked remote repository to the remotes of the project.
 */
public class AddRemoteStep implements Step {

    /**
     * The service for VCS operations.
     */
    private final VcsService vcsService;

    /**
     * The factory used to create "wait" steps.
     */
    private final WaitForForOnRemoteStepFactory waitRemoteStepFactory;

    /**
     * The following step.
     */
    private final PushBranchOnForkStep pushStep;

    @Inject
    public AddRemoteStep(final @Nonnull VcsService vcsService,
                         final @Nonnull PushBranchOnForkStep pushStep,
                         final @Nonnull WaitForForOnRemoteStepFactory waitRemoteStepFactory) {
        this.vcsService = vcsService;
        this.pushStep = pushStep;
        this.waitRemoteStepFactory = waitRemoteStepFactory;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        // should call proceed at some point

    }

    private void proceed(final Context context, final Configuration config) {
        final Step waitStep = this.waitRemoteStepFactory.create(this.pushStep);
        waitStep.execute(context, config);
    }
}
