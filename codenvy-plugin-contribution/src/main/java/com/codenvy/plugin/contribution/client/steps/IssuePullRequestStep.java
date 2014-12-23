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


import javax.inject.Inject;

import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.PullRequest;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Create the pull request on the remote VCS repository.
 */
public class IssuePullRequestStep implements Step {

    private static final String BASE_BRANCH = "master";

    private final RepositoryHost     repositoryHost;

    /**
     * The following step.
     */
    private final Step nextStep;

    /**
     * The notification manager.
     */
    private final NotificationManager notificationManager;

    /**
     * The internationalizable messages.
     */
    private final ContributeMessages messages;

    @Inject
    public IssuePullRequestStep(final RepositoryHost repositoryHost,
                                final GenerateReviewFactory nextStep,
                                final NotificationManager notificationmanager,
                                final ContributeMessages messages) {
        this.repositoryHost = repositoryHost;
        this.nextStep = nextStep;
        this.notificationManager = notificationmanager;
        this.messages = messages;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        String owner = context.getOriginRepositoryOwner();
        String repository = context.getOriginRepositoryName();
        String title = config.getContributionTitle();
        String headBranch = context.getHostUserLogin() + ":" + context.getWorkBranchName();
        String body = config.getPullRequestComment();
        repositoryHost.createPullRequest(owner, repository, title, headBranch, BASE_BRANCH, body, new AsyncCallback<PullRequest>() {

            @Override
            public void onSuccess(PullRequest result) {
                onPullRequestCreated(context, config);
            }

            @Override
            public void onFailure(Throwable caught) {
                notificationManager.showError(messages.errorPullRequestFailed());
                Log.error(RemoteForkStep.class, caught.getMessage());
            }
        });
    }

    protected void onPullRequestCreated(final Context context, final Configuration config) {
        nextStep.execute(context, config);
    }
}
