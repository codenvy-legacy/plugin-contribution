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
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.Branch;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;

/**
 * This step creates the working branch for the user contribution.
 *
 * @author Kevin Pollet
 */
public class CreateWorkBranchStep implements Step {
    private static final String WORKING_BRANCH_NAME_PREFIX = "contrib-";

    private final ContributeMessages      messages;
    private final NotificationHelper      notificationHelper;
    private final VcsService              vcsService;
    private final ContributePartPresenter contributePartPresenter;
    private final Step                    commitWorkingTreeStep;

    @Inject
    public CreateWorkBranchStep(@Nonnull final ContributeMessages messages,
                                @Nonnull final NotificationHelper notificationHelper,
                                @Nonnull final VcsService vcsService,
                                @Nonnull final ContributePartPresenter contributePartPresenter,
                                @Nonnull final CommitWorkingTreeStep commitWorkingTreeStep) {
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.vcsService = vcsService;
        this.contributePartPresenter = contributePartPresenter;
        this.commitWorkingTreeStep = commitWorkingTreeStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();

        final String workingBranchName = generateWorkingBranchName();
        context.setWorkBranchName(workingBranchName);

        final Notification createWorkingBranchNotification =
                new Notification(messages.contributorExtensionCreatingWorkBranch(workingBranchName), INFO, PROGRESS);
        notificationHelper.showNotification(createWorkingBranchNotification);

        // the working branch is only created if it doesn't exist
        vcsService.listLocalBranches(context.getProject(), new AsyncCallback<List<Branch>>() {
            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.finishNotificationWithError(CreateWorkBranchStep.class, exception, createWorkingBranchNotification);
            }

            @Override
            public void onSuccess(final List<Branch> branches) {
                boolean workingBranchExists = false;

                for (final Branch oneBranch : branches) {
                    if (workingBranchName.equals(oneBranch.getDisplayName())) {
                        workingBranchExists = true;
                        break;
                    }
                }

                // shorthand for create + checkout new temporary working branch -> checkout -b branchName
                vcsService.checkoutBranch(context.getProject(), workingBranchName, !workingBranchExists, new AsyncCallback<String>() {
                    @Override
                    public void onSuccess(final String result) {
                        contributePartPresenter.open();
                        contributePartPresenter.showContributePart();
                        notificationHelper.finishNotification(messages.contributorExtensionWorkBranchCreated(workingBranchName),
                                                              createWorkingBranchNotification);

                        // the step is executed when the user click on the contribute button
                        workflow.setStep(commitWorkingTreeStep);
                    }

                    @Override
                    public void onFailure(final Throwable exception) {
                        notificationHelper.finishNotificationWithError(CreateWorkBranchStep.class, exception,
                                                                       createWorkingBranchNotification);
                    }
                });
            }
        });
    }

    /**
     * Generates the working branch name used for the contribution.
     *
     * @return the working branch name, never {@code null}.
     */
    private String generateWorkingBranchName() {
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMddyyyy");
        return WORKING_BRANCH_NAME_PREFIX + dateTimeFormat.format(new Date());
    }
}
