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
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.NoUserForkException;
import com.codenvy.plugin.contribution.client.vcshost.Repository;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;

/**
 * Create a fork of the contributed project (upstream) to push the user's contribution.
 */
public class RemoteForkStep implements Step {

    private final RepositoryHost     repositoryHost;
    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;

    @Inject
    public RemoteForkStep(@Nonnull final RepositoryHost repositoryHost,
                          @Nonnull final ContributeMessages messages,
                          @Nonnull final NotificationHelper notificationHelper) {
        this.repositoryHost = repositoryHost;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        final String owner = context.getOriginRepositoryOwner();
        final String repository = context.getOriginRepositoryName();

        // get list of forks existing for origin repository
        repositoryHost.getUserFork(context.getHostUserLogin(), owner, repository, new AsyncCallback<Repository>() {
            @Override
            public void onSuccess(Repository fork) {
                notificationHelper.showInfo(messages.useExistingUserFork());
            }

            @Override
            public void onFailure(Throwable exception) {
                if (exception instanceof NoUserForkException) {
                    createFork(owner, repository);
                    return;
                }

                notificationHelper.showError(RemoteForkStep.class, exception);
            }
        });
    }

    private void createFork(final String repositoryOwner, final String repositoryName) {
        final Notification notification = new Notification(messages.creatingFork(repositoryOwner, repositoryName), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        repositoryHost.fork(repositoryOwner, repositoryName, new AsyncCallback<Repository>() {
            @Override
            public void onSuccess(final Repository result) {
                notificationHelper.finishNotification(messages.requestedForkCreation(repositoryOwner, repositoryName), notification);
            }

            @Override
            public void onFailure(final Throwable exception) {
                final String errorMessage = messages.failedCreatingUserFork(repositoryOwner, repositoryName, exception.getMessage());
                notificationHelper.finishNotificationWithError(RemoteForkStep.class, errorMessage, notification);
            }
        });
    }
}
