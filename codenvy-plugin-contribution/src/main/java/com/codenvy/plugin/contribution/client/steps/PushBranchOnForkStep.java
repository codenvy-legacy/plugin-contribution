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
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;

public class PushBranchOnForkStep implements Step {

    private final Step               pullRequestStep;
    private final VcsService         vcsService;
    private final NotificationHelper notificationHelper;
    private final ContributeMessages messages;

    @Inject
    public PushBranchOnForkStep(@Nonnull final IssuePullRequestStep nextStep,
                                @Nonnull final VcsService vcsService,
                                @Nonnull final NotificationHelper notificationHelper,
                                @NotNull final ContributeMessages messages) {
        this.pullRequestStep = nextStep;
        this.vcsService = vcsService;
        this.notificationHelper = notificationHelper;
        this.messages = messages;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        final Notification notification = new Notification(messages.pushingWorkingBranchToFork(), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        vcsService.pushBranch(context.getProject(), context.getForkedRemoteName(), context.getWorkBranchName(), new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                notificationHelper.finishNotification(messages.successPushingBranchToFork(), notification);
                pullRequestStep.execute(context, config);
            }

            @Override
            public void onFailure(final Throwable exception) {
                final String errorMessage = messages.failedPushingBranchToFork(exception.getMessage());
                notificationHelper.finishNotificationWithError(PushBranchOnForkStep.class, errorMessage, notification);
            }
        });
    }
}
