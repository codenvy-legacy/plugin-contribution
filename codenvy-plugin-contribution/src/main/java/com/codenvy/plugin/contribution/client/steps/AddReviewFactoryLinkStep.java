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
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.IssueComment;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.ADD_REVIEW_FACTORY_LINK;

/**
 * Adds a factory link to the contribution in a comment of the pull request.
 */
public class AddReviewFactoryLinkStep implements Step {
    private final VcsHostingService  vcsHostingService;
    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;

    @Inject
    public AddReviewFactoryLinkStep(@Nonnull final VcsHostingService vcsHostingService,
                                    @Nonnull final ContributeMessages messages,
                                    @Nonnull final NotificationHelper notificationHelper) {
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.vcsHostingService = vcsHostingService;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final String reviewFactoryUrl = workflow.getContext().getReviewFactoryUrl();
        if (reviewFactoryUrl != null) {
            sendComment(workflow, reviewFactoryUrl);
        }
    }

    /**
     * Post the comment in the pull request.
     *
     * @param workflow
     *         the contributor workflow.
     * @param factoryUrl
     *         the factory URL to include in the comment
     */
    private void sendComment(final ContributorWorkflow workflow, final String factoryUrl) {
        final Context context = workflow.getContext();
        final String commentText = messages.stepAddReviewFactoryLinkPullRequestComment(factoryUrl);

        vcsHostingService.commentPullRequest(
                context.getUpstreamRepositoryOwner(),
                context.getUpstreamRepositoryName(),
                context.getPullRequestIssueNumber(),
                commentText,
                new AsyncCallback<IssueComment>() {
                    @Override
                    public void onSuccess(final IssueComment result) {
                        workflow.fireStepDoneEvent(ADD_REVIEW_FACTORY_LINK);
                    }

                    @Override
                    public void onFailure(final Throwable exception) {
                        workflow.fireStepErrorEvent(ADD_REVIEW_FACTORY_LINK);
                        notificationHelper.showWarning(messages.stepAddReviewFactoryLinkErrorPostingFactoryLink(factoryUrl));
                    }
                });
    }
}
