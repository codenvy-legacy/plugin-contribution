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
import com.codenvy.plugin.contribution.client.steps.event.UpdateModeEvent;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.PullRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent.Step.ISSUE_PULL_REQUEST;
import static com.codenvy.plugin.contribution.client.steps.event.UpdateModeEvent.State.START_UPDATE_MODE;

/**
 * Create the pull request on the remote VCS repository.
 */
public class IssuePullRequestStep implements Step {
    private static final String DEFAULT_BASE_BRANCH           = "master";
    private static final String EXISTING_PULL_REQUEST_MESSAGE = "A pull request already exists for ";

    private final VcsHostingService  vcsHostingService;
    private final Step               generateReviewFactoryStep;
    private final NotificationHelper notificationHelper;
    private final ContributeMessages messages;
    private final EventBus           eventBus;

    @Inject
    public IssuePullRequestStep(@Nonnull final VcsHostingService vcsHostingService,
                                @Nonnull final GenerateReviewFactoryStep generateReviewFactoryStepStep,
                                @Nonnull final NotificationHelper notificationHelper,
                                @Nonnull final ContributeMessages messages,
                                @Nonnull final EventBus eventBus) {
        this.vcsHostingService = vcsHostingService;
        this.generateReviewFactoryStep = generateReviewFactoryStepStep;
        this.notificationHelper = notificationHelper;
        this.messages = messages;
        this.eventBus = eventBus;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final Configuration configuration = workflow.getConfiguration();
        final String owner = context.getOriginRepositoryOwner();
        final String repository = context.getOriginRepositoryName();
        final String title = configuration.getContributionTitle();
        final String baseBranch = context.getClonedBranchName() != null ? context.getClonedBranchName() : DEFAULT_BASE_BRANCH;
        final String headBranch = context.getHostUserLogin() + ":" + context.getWorkBranchName();
        final String body = configuration.getContributionComment();

        final Notification notification = new Notification(messages.stepIssuePullRequestIssuingPullRequest(), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        vcsHostingService.createPullRequest(owner, repository, title, headBranch, baseBranch, body, new AsyncCallback<PullRequest>() {
            @Override
            public void onSuccess(final PullRequest result) {
                eventBus.fireEvent(new StepDoneEvent(ISSUE_PULL_REQUEST, true));
                eventBus.fireEvent(new UpdateModeEvent(START_UPDATE_MODE));

                context.setPullRequestIssueNumber(result.getNumber());
                notificationHelper.finishNotification(messages.stepIssuePullRequestPullRequestCreated(result.getHtmlUrl()), notification);

                workflow.setStep(generateReviewFactoryStep);
                workflow.executeStep();
            }

            @Override
            public void onFailure(final Throwable exception) {
                final boolean isExistingPullRequest = exception.getMessage().contains(EXISTING_PULL_REQUEST_MESSAGE + headBranch);
                eventBus.fireEvent(new StepDoneEvent(ISSUE_PULL_REQUEST, isExistingPullRequest));

                if (isExistingPullRequest) {
                    notificationHelper.finishNotificationWithWarning(messages.stepIssuePullRequestExistingPullRequestUpdated(headBranch),
                                                                     notification);

                    workflow.setStep(generateReviewFactoryStep);
                    workflow.executeStep();

                } else {
                    notificationHelper.finishNotificationWithError(IssuePullRequestStep.class,
                                                                   messages.stepIssuePullRequestErrorCreatePullRequest(),
                                                                   notification);
                }
            }
        });

    }
}
