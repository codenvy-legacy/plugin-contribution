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
import com.codenvy.ide.api.notification.Notification.Status;
import com.codenvy.ide.api.notification.Notification.Type;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.NoUserForkException;
import com.codenvy.plugin.contribution.client.vcshost.Repository;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.inject.Inject;

/**
 * Create a fork of the contributed project (upstream) to push the user's contribution.
 */
public class RemoteForkStep implements Step {

    private final RepositoryHost      repositoryHost;
    private final NotificationManager notificationManager;
    private final ContributeMessages  messages;

    @Inject
    public RemoteForkStep(RepositoryHost repositoryHost, NotificationManager notificationManager, ContributeMessages messages) {
        this.repositoryHost = repositoryHost;
        this.notificationManager = notificationManager;
        this.messages = messages;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        final String owner = context.getOriginRepositoryOwner();
        final String repository = context.getOriginRepositoryName();
        // get list of forks existing for origin repository
        repositoryHost.getUserFork(context.getHostUserLogin(), owner, repository, new AsyncCallback<Repository>() {

            @Override
            public void onSuccess(Repository fork) {
                notificationManager.showNotification(
                        new Notification(messages.prefixNotification(messages.useExistingUserFork()), Notification.Type.INFO));
            }

            @Override
            public void onFailure(Throwable exception) {
                if (exception instanceof NoUserForkException) {
                    createFork(context, owner, repository);
                    return;
                }
                notificationManager
                        .showNotification(new Notification(messages.prefixNotification(exception.getMessage()), Notification.Type.ERROR));
                Log.error(RemoteForkStep.class, exception);
            }
        });
    }

    private void createFork(final Context context, final String repositoryOwner, final String repositoryName) {
        final Notification notification =
                new Notification(messages.prefixNotification(messages.creatingFork(repositoryOwner, repositoryName)),
                                 Notification.Type.INFO);
        notification.setStatus(Status.PROGRESS);
        notificationManager.showNotification(notification);

        repositoryHost.fork(repositoryOwner, repositoryName, new AsyncCallback<Repository>() {

            @Override
            public void onSuccess(Repository result) {
                notification.setStatus(Status.FINISHED);
                notification.setMessage(messages.prefixNotification(messages.requestedForkCreation(repositoryOwner, repositoryName)));
            }

            @Override
            public void onFailure(Throwable exception) {
                notification.setType(Type.ERROR);
                notification.setStatus(Status.FINISHED);
                String errorMessage = messages.failedCreatingUserFork(repositoryOwner, repositoryName, exception.getMessage());
                notification.setMessage(messages.prefixNotification(errorMessage));

                Log.error(RemoteForkStep.class, exception);
            }
        });
    }
}
