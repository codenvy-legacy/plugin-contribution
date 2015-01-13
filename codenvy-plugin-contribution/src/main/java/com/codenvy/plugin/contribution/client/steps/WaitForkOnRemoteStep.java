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

import com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.hosting.RepositoryHost;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.Repository;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import java.util.List;

import static com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent.Step.CREATE_FORK;

public class WaitForkOnRemoteStep implements Step {
    /** The frequency of the checks on the remote. */
    private static final int POLL_FREQUENCY_MS = 1000;

    /** The remote repository host. */
    private final RepositoryHost repositoryHost;

    /** The following step. */
    private final Step nextStep;

    /** The event bus. */
    private final EventBus eventBus;

    /** The timer used for waiting. */
    private Timer timer;

    @AssistedInject
    public WaitForkOnRemoteStep(@Nonnull final RepositoryHost host,
                                @Nonnull final @Assisted Step nextStep,
                                @Nonnull final EventBus eventBus) {
        this.repositoryHost = host;
        this.nextStep = nextStep;
        this.eventBus = eventBus;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        check(context, config);
    }

    private void wait(final Context context, final Configuration config) {
        if (timer == null) {
            timer = new Timer() {
                @Override
                public void run() {
                    checkRepository(context, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable caught) {
                            check(context, config);
                        }

                        @Override
                        public void onSuccess(final Void result) {
                            eventBus.fireEvent(new StepDoneEvent(CREATE_FORK, true));

                            context.setForkReady(true);
                            check(context, config);
                        }
                    });
                }
            };
        }
        timer.schedule(POLL_FREQUENCY_MS);
    }

    private void check(final Context context, final Configuration config) {
        if (context.getForkReady()) {
            nextStep.execute(context, config);
        } else {
            wait(context, config);
        }
    }

    private void checkRepository(final Context context, final AsyncCallback<Void> callback) {
        repositoryHost.getRepositoriesList(new AsyncCallback<List<Repository>>() {

            @Override
            public void onSuccess(final List<Repository> result) {
                for (final Repository repo : result) {
                    if (repo.getName().equals(context.getForkedRepositoryName())) {
                        callback.onSuccess(null);
                        return;
                    }
                }
                callback.onFailure(null);
            }

            @Override
            public void onFailure(final Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }
}
