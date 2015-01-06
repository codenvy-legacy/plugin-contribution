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
import com.codenvy.plugin.contribution.client.vcshost.PullRequest;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent.Step.ISSUE_PULL_REQUEST;

/**
 * Create the pull request on the remote VCS repository.
 */
public class IssuePullRequestStep implements Step {
    private static final String DEFAULT_BASE_BRANCH           = "master";
    private static final String EXISTING_PULL_REQUEST_MESSAGE = "A pull request already exists for ";

    /** The host repository. */
    private final RepositoryHost repositoryHost;

    /** The following step. */
    private final Step nextStep;

    /** The notification helper. */
    private final NotificationHelper notificationHelper;

    /** The internationalizable messages. */
    private final ContributeMessages messages;

    /** The event bus. */
    private final EventBus eventBus;

    @Inject
    public IssuePullRequestStep(@Nonnull final RepositoryHost repositoryHost,
                                @Nonnull final GenerateReviewFactory nextStep,
                                @Nonnull final NotificationHelper notificationHelper,
                                @Nonnull final ContributeMessages messages,
                                @Nonnull final EventBus eventBus) {
        this.repositoryHost = repositoryHost;
        this.nextStep = nextStep;
        this.notificationHelper = notificationHelper;
        this.messages = messages;
        this.eventBus = eventBus;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        final String owner = context.getOriginRepositoryOwner();
        final String repository = context.getOriginRepositoryName();
        final String title = config.getContributionTitle();
        final String baseBranch = context.getClonedBranchName();
        final String headBranch = context.getHostUserLogin() + ":" + context.getWorkBranchName();
        final String body = config.getPullRequestComment();

        final Notification notification = new Notification(messages.issuingPullRequest(), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        repositoryHost.createPullRequest(owner, repository, title, headBranch, (baseBranch != null ? baseBranch : DEFAULT_BASE_BRANCH),
                                         body,
                                         new AsyncCallback<PullRequest>() {
                                             @Override
                                             public void onSuccess(final PullRequest result) {
                                                 eventBus.fireEvent(new StepDoneEvent(ISSUE_PULL_REQUEST));
                                                 context.setPullRequestIssueNumber(result.getNumber());
                                                 notificationHelper
                                                         .finishNotification(messages.successIssuingPullRequest(result.getHtmlUrl()),
                                                                             notification);
                                                 onPullRequestCreated(context, config);
                                             }

                                             @Override
                                             public void onFailure(final Throwable exception) {
                                                 if (exception.getMessage().contains(EXISTING_PULL_REQUEST_MESSAGE + headBranch)) {
                                                     notificationHelper
                                                             .finishNotificationWithWarning(messages.warnPullRequestUpdated(headBranch),
                                                                                            notification);

                                                 } else {
                                                     notificationHelper
                                                             .finishNotificationWithError(IssuePullRequestStep.class,
                                                                                          messages.errorPullRequestFailed(), notification);
                                                 }
                                             }
                                         });

    }

    protected void onPullRequestCreated(final Context context, final Configuration config) {
        nextStep.execute(context, config);
    }
}
