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

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.codenvy.plugin.contribution.client.vcshost.dto.IssueComment;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Adds a factory link to the contribution in a comment of the pull request.
 */
public class AddReviewFactoryLinkStep implements Step {
    /** The following step. */
    private final Step nextStep;

    /** The remote VCS repository. */
    private final RepositoryHost repository;

    /** The i18n-able messages. */
    private final ContributeMessages messages;

    /** Helper to work with notifications. */
    private final NotificationHelper notificationHelper;

    @Inject
    public AddReviewFactoryLinkStep(@Nonnull final ProposePersistStep nextStep,
                                    @Nonnull final RepositoryHost repositoryHost,
                                    @Nonnull final ContributeMessages messages,
                                    @Nonnull final NotificationHelper notificationHelper) {
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.nextStep = nextStep;
        this.repository = repositoryHost;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        if (context.getReviewFactoryUrl() == null) {
            proceed(context, config);

        } else {
            // post the comment
            sendComment(context, config, context.getReviewFactoryUrl());
        }
    }

    /**
     * Post the comment in the pull request.
     *
     * @param context
     *         the context of the contribution
     * @param config
     *         the configuration of the contribution
     * @param factoryUrl
     *         the factory URL to include in the comment
     */
    private void sendComment(final Context context, final Configuration config, final String factoryUrl) {
        final String commentText = messages.pullRequestlinkComment(factoryUrl);
        repository.commentPullRequest(context.getOriginRepositoryOwner(), context.getOriginRepositoryName(),
                                      context.getPullRequestIssueNumber(), commentText, new AsyncCallback<IssueComment>() {
                    @Override
                    public void onSuccess(final IssueComment result) {
                        proceed(context, config);
                    }

                    @Override
                    public void onFailure(final Throwable exception) {
                        notificationHelper.showWarning(messages.warnPostFactoryLinkFailed(factoryUrl));

                        // continue anyway, this is not a hard failure
                        proceed(context, config);
                    }
                });
    }

    /**
     * Continue to the following step.
     *
     * @param context
     *         the context of the contribution
     * @param config
     *         the configuration of the contribution
     */
    private void proceed(final Context context, final Configuration config) {
        nextStep.execute(context, config);
    }
}
