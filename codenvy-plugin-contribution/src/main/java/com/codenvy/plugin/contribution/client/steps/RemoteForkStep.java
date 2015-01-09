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

import com.codenvy.ide.api.notification.Notification;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.NoUserForkException;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.codenvy.plugin.contribution.client.vcshost.dto.Repository;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent.Step.CREATE_FORK;

/**
 * Create a fork of the contributed project (upstream) to push the user's contribution.
 */
public class RemoteForkStep implements Step {
    /** The repository host. */
    private final RepositoryHost repositoryHost;

    /** I18n messages. */
    private final ContributeMessages messages;

    /** The notification helper. */
    private final NotificationHelper notificationHelper;
    private final EventBus           eventBus;

    @Inject
    public RemoteForkStep(@Nonnull final RepositoryHost repositoryHost,
                          @Nonnull final ContributeMessages messages,
                          @Nonnull final NotificationHelper notificationHelper,
                          @Nonnull final EventBus eventBus) {
        this.repositoryHost = repositoryHost;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.eventBus = eventBus;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        final String owner = context.getOriginRepositoryOwner();
        final String repository = context.getOriginRepositoryName();

        // get list of forks existing for origin repository
        repositoryHost.getUserFork(context.getHostUserLogin(), owner, repository, new AsyncCallback<Repository>() {
            @Override
            public void onSuccess(final Repository fork) {
                eventBus.fireEvent(new StepDoneEvent(CREATE_FORK, true));

                context.setForkedRepositoryName(fork.getName());
                notificationHelper.showInfo(messages.useExistingUserFork());
            }

            @Override
            public void onFailure(final Throwable exception) {
                if (exception instanceof NoUserForkException) {
                    createFork(context, owner, repository);
                    return;
                }

                eventBus.fireEvent(new StepDoneEvent(CREATE_FORK, false));
                notificationHelper.showError(RemoteForkStep.class, exception);
            }
        });
    }

    private void createFork(final Context context, final String repositoryOwner, final String repositoryName) {
        final Notification notification = new Notification(messages.creatingFork(repositoryOwner, repositoryName), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        repositoryHost.fork(repositoryOwner, repositoryName, new AsyncCallback<Repository>() {
            @Override
            public void onSuccess(final Repository result) {
                eventBus.fireEvent(new StepDoneEvent(CREATE_FORK, true));

                context.setForkedRepositoryName(result.getName());
                notificationHelper.finishNotification(messages.requestedForkCreation(repositoryOwner, repositoryName), notification);
            }

            @Override
            public void onFailure(final Throwable exception) {
                eventBus.fireEvent(new StepDoneEvent(CREATE_FORK, false));

                final String errorMessage = messages.failedCreatingUserFork(repositoryOwner, repositoryName, exception.getMessage());
                notificationHelper.finishNotificationWithError(RemoteForkStep.class, errorMessage, notification);
            }
        });
    }
}
