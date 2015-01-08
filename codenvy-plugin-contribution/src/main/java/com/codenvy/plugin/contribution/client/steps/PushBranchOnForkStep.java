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
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent.Step.PUSH_BRANCH;

/**
 * Push the local contribution branch on the user fork.
 */
public class PushBranchOnForkStep implements Step {
    /** The next step. */
    private final Step nextStep;

    /** The vcs service. */
    private final VcsService vcsService;

    /** The notification helper. */
    private final NotificationHelper notificationHelper;

    /** The i18-n messages. */
    private final ContributeMessages messages;

    /** The event bus. */
    private final EventBus eventBus;

    @Inject
    public PushBranchOnForkStep(@Nonnull final IssuePullRequestStep nextStep,
                                @Nonnull final VcsService vcsService,
                                @Nonnull final NotificationHelper notificationHelper,
                                @NotNull final ContributeMessages messages,
                                @NotNull final EventBus eventBus) {
        this.nextStep = nextStep;
        this.vcsService = vcsService;
        this.notificationHelper = notificationHelper;
        this.messages = messages;
        this.eventBus = eventBus;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        final Notification notification = new Notification(messages.pushingWorkingBranchToFork(), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        vcsService.pushBranch(context.getProject(), context.getForkedRemoteName(), context.getWorkBranchName(), new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                eventBus.fireEvent(new StepDoneEvent(PUSH_BRANCH, true));

                notificationHelper.finishNotification(messages.successPushingBranchToFork(), notification);
                nextStep.execute(context, config);
            }

            @Override
            public void onFailure(final Throwable exception) {
                eventBus.fireEvent(new StepDoneEvent(PUSH_BRANCH, false));

                final String errorMessage = messages.failedPushingBranchToFork(exception.getMessage());
                notificationHelper.finishNotificationWithError(PushBranchOnForkStep.class, errorMessage, notification);
            }
        });
    }
}
