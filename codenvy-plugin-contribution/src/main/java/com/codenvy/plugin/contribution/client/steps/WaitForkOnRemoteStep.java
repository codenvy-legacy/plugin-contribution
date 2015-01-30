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

import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.Repository;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import javax.annotation.Nonnull;
import java.util.List;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.CREATE_FORK;

public class WaitForkOnRemoteStep implements Step {
    private static final int POLL_FREQUENCY_MS = 1000;

    private final VcsHostingService vcsHostingService;
    private final Step              nextStep;
    private       Timer             timer;

    @AssistedInject
    public WaitForkOnRemoteStep(@Nonnull final VcsHostingService host, @Nonnull final @Assisted Step nextStep) {
        this.vcsHostingService = host;
        this.nextStep = nextStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        if (timer == null) {
            timer = new Timer() {
                @Override
                public void run() {
                    checkRepository(workflow.getContext(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable caught) {
                            timer.schedule(POLL_FREQUENCY_MS);
                        }

                        @Override
                        public void onSuccess(final Void result) {
                            workflow.fireStepDoneEvent(CREATE_FORK);

                            workflow.setStep(nextStep);
                            workflow.executeStep();
                        }
                    });
                }
            };
        }

        timer.schedule(POLL_FREQUENCY_MS);
    }

    private void checkRepository(final Context context, final AsyncCallback<Void> callback) {
        vcsHostingService.getRepositories(new AsyncCallback<List<Repository>>() {

            @Override
            public void onSuccess(final List<Repository> repositories) {
                for (final Repository oneRepository : repositories) {
                    if (oneRepository.getName().equals(context.getForkedRepositoryName())) {
                        callback.onSuccess(null);
                        return;
                    }
                }
                callback.onFailure(null);
            }

            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }
}
