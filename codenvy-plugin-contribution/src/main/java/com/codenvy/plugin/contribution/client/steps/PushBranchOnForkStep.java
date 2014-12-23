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
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class PushBranchOnForkStep implements Step {

    private final Step pullRequestStep;
    private final VcsService           vcsService;
    private final NotificationManager notificationManager;
    private final ContributeMessages messages;

    @Inject
    public PushBranchOnForkStep(final IssuePullRequestStep nextStep, final @Nonnull VcsService vcsService, final @Nonnull NotificationManager notificationManager, final @NotNull ContributeMessages messages) {
        pullRequestStep = nextStep;
        this.vcsService = vcsService;
        this.notificationManager = notificationManager;
        this.messages = messages;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        final Notification notification = new Notification(messages.pushingWorkingBranchToFork(), Notification.Type.INFO);
        notification.setStatus(Status.PROGRESS);
        notificationManager.showNotification(notification);
        vcsService.pushBranch(context.getProject(), context.getForkedRemoteName(), context.getWorkBranchName(), new AsyncCallback<Void>(){
            @Override
            public void onSuccess(Void result) {
                notification.setMessage(messages.successPushingBranchToFork());
                notification.setStatus(Status.FINISHED);
                pullRequestStep.execute(context, config);
            }
            @Override
            public void onFailure(Throwable caught) {
                notification.setMessage(messages.failedPushingBranchToFork(caught.getMessage()));
                notification.setType(Type.ERROR);
                notification.setStatus(Status.FINISHED);
                Log.error(getClass(), caught);
            }
        });
    }
}
