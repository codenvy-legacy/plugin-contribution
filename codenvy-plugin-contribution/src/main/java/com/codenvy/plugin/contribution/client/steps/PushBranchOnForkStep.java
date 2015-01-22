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
import com.codenvy.ide.ui.dialogs.CancelCallback;
import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.steps.events.UpdateModeEvent;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcs.hosting.NoPullRequestException;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.PullRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.PUSH_BRANCH_ON_FORK;
import static com.codenvy.plugin.contribution.client.steps.events.UpdateModeEvent.State.STOP_UPDATE_MODE;

/**
 * Push the local contribution branch on the user fork.
 */
public class PushBranchOnForkStep implements Step {

    private final Step               issuePullRequestStep;
    private final VcsService         vcsService;
    private final VcsHostingService  vcsHostingService;
    private final NotificationHelper notificationHelper;
    private final ContributeMessages messages;
    private final EventBus           eventBus;

    /** The dialog factory. */
    private final DialogFactory dialogFactory;

    @Inject
    public PushBranchOnForkStep(@Nonnull final IssuePullRequestStep issuePullRequestStep,
                                @Nonnull final VcsService vcsService,
                                @Nonnull final VcsHostingService vcsHostingService,
                                @Nonnull final NotificationHelper notificationHelper,
                                @NotNull final ContributeMessages messages,
                                @NotNull final EventBus eventBus,
                                @NotNull final DialogFactory dialogFactory) {
        this.issuePullRequestStep = issuePullRequestStep;
        this.vcsService = vcsService;
        this.vcsHostingService = vcsHostingService;
        this.notificationHelper = notificationHelper;
        this.messages = messages;
        this.eventBus = eventBus;
        this.dialogFactory = dialogFactory;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();

        final Notification notification = new Notification(messages.stepPushBranchPushingBranch(), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        final String owner = context.getOriginRepositoryOwner();
        final String repository = context.getOriginRepositoryName();
        final String headBranch = context.getHostUserLogin() + ":" + context.getWorkBranchName();

        vcsHostingService.getPullRequest(owner, repository, headBranch, new AsyncCallback<PullRequest>() {
            @Override
            public void onSuccess(final PullRequest result) {
                final ConfirmCallback okCallback = new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        pushBranch(workflow, context, notification);
                    }
                };
                final CancelCallback cancelCallback = new CancelCallback() {
                    @Override
                    public void cancelled() {
                        workflow.fireStepErrorEvent(PUSH_BRANCH_ON_FORK);
                        eventBus.fireEvent(new UpdateModeEvent(STOP_UPDATE_MODE));
                        notificationHelper.finishNotificationWithWarning(messages.stepPushBranchCanceling(), notification);
                    }
                };

                dialogFactory.createConfirmDialog(messages.contributePartConfigureContributionDialogUpdateTitle(),
                                                  messages.contributePartConfigureContributionDialogUpdateText(result.getHead().getLabel()),
                                                  okCallback,
                                                  cancelCallback).show();
            }

            @Override
            public void onFailure(Throwable exception) {
                if (exception instanceof NoPullRequestException) {
                    pushBranch(workflow, context, notification);
                    return;
                }

                workflow.fireStepDoneEvent(PUSH_BRANCH_ON_FORK);
                notificationHelper.showError(PushBranchOnForkStep.class, exception);
            }
        });
    }

    protected void pushBranch(final ContributorWorkflow workflow, final Context context, final Notification notification) {
        vcsService.pushBranch(context.getProject(), context.getForkedRemoteName(), context.getWorkBranchName(), new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                workflow.fireStepDoneEvent(PUSH_BRANCH_ON_FORK);
                notificationHelper.finishNotification(messages.stepPushBranchBranchPushed(), notification);

                workflow.setStep(issuePullRequestStep);
                workflow.executeStep();
            }

            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(PUSH_BRANCH_ON_FORK);

                final String errorMessage = messages.stepPushBranchErrorPushingBranch(exception.getMessage());
                notificationHelper.finishNotificationWithError(PushBranchOnForkStep.class, errorMessage, notification);
            }
        });
    }
}
