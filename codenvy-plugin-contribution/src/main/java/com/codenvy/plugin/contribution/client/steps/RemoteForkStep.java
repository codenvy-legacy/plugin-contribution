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

import static com.codenvy.plugin.contribution.client.ContributorExtension.VCS_LOCATION_KEY;

import java.util.List;

import javax.inject.Inject;

import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.Repository;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Create a fork of the contributed project (upstream) to push the user's contribution.
 */
public class RemoteForkStep implements Step {

    private final RepositoryHost       repositoryHost;
    private final NotificationManager  notificationManager;

    @Inject
    public RemoteForkStep(RepositoryHost repositoryHost, NotificationManager notificationManager) {
        this.repositoryHost = repositoryHost;
        this.notificationManager = notificationManager;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        // get URL of project on VCS
        List<String> locationAttributeValues = context.getProject().getAttributes().get(VCS_LOCATION_KEY);
        if (locationAttributeValues != null && !locationAttributeValues.isEmpty()) {
            String locationURL = locationAttributeValues.get(0);

            final String repository = locationURL.substring(locationURL.lastIndexOf('/') + 1);
            final String locationURLSubstring = locationURL.substring(0, locationURL.length() - repository.length() - 1);
            final String username = locationURLSubstring.substring(locationURLSubstring.lastIndexOf('/') + 1);
            // get list of forks existing for origin repository
            repositoryHost.getForks(username, repository, new AsyncCallback<List<Repository>>() {

                @Override
                public void onSuccess(List<Repository> result) {
                    if (context.getHostUserLogin() != null) {
                        // find out if current user has a fork
                        Repository fork = getUserFork(context.getHostUserLogin(), result);
                        if (fork != null) {
                            Log.info(RemoteForkStep.class, "Fork already exist.");
                            context.setRepositoryName(fork.getName());
                        } else {
                            // create a fork on current user's VCS account
                            createFork(context, username, repository);
                        }
                    } else {
                        Log.error(RemoteForkStep.class, "No VCS user available.");
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    notificationManager.showNotification(new Notification(exception.getMessage(), Notification.Type.ERROR));
                    Log.error(RemoteForkStep.class, exception.getMessage());
                }
            });
        }
    }

    private Repository getUserFork(String login, List<Repository> forks) {
        Repository userFork = null;
        for (Repository repository : forks) {
            String forkURL = repository.getUrl();
            if (forkURL.toLowerCase().contains("/repos/" + login + "/")) {
                userFork = repository;
            }
        }
        return userFork;
    }

    private boolean createFork(final Context context, String username, String repository) {
        repositoryHost.fork(username, repository, new AsyncCallback<Repository>() {

            @Override
            public void onSuccess(Repository result) {
                Log.info(RemoteForkStep.class, "Fork creation started.");
                context.setRepositoryName(result.getName());
            }

            @Override
            public void onFailure(Throwable exception) {
                notificationManager.showNotification(new Notification(exception.getMessage(), Notification.Type.ERROR));
                Log.error(RemoteForkStep.class, exception.getMessage());
            }
        });
        return true;
    }
}
