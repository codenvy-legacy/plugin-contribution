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
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.hosting.NoUserForkException;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.Repository;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.CREATE_FORK;

/**
 * Create a fork of the contributed project (upstream) to push the user's contribution.
 */
public class CreateForkStep implements Step {
    private final VcsHostingService  vcsHostingService;
    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;
    private final Step               checkoutBranchToPushStep;

    @Inject
    public CreateForkStep(@Nonnull final VcsHostingService vcsHostingService,
                          @Nonnull final ContributeMessages messages,
                          @Nonnull final NotificationHelper notificationHelper,
                          @Nonnull final CheckoutBranchToPushStep checkoutBranchToPushStep) {
        this.vcsHostingService = vcsHostingService;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.checkoutBranchToPushStep = checkoutBranchToPushStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final String owner = context.getOriginRepositoryOwner();
        final String repository = context.getOriginRepositoryName();

        // get list of forks existing for origin repository
        vcsHostingService.getUserFork(context.getHostUserLogin(), owner, repository, new AsyncCallback<Repository>() {
            @Override
            public void onSuccess(final Repository fork) {
                context.setForkedRepositoryName(fork.getName());

                workflow.fireStepDoneEvent(CREATE_FORK);
                notificationHelper.showInfo(messages.stepCreateForkUseExistingFork());

                workflow.setStep(checkoutBranchToPushStep);
                workflow.executeStep();
            }

            @Override
            public void onFailure(final Throwable exception) {
                if (exception instanceof NoUserForkException) {
                    createFork(workflow, owner, repository);
                    return;
                }

                workflow.fireStepErrorEvent(CREATE_FORK);
                notificationHelper.showError(CreateForkStep.class, exception);
            }
        });
    }

    private void createFork(final ContributorWorkflow workflow, final String repositoryOwner, final String repositoryName) {
        final Notification notification =
                new Notification(messages.stepCreateForkCreateFork(repositoryOwner, repositoryName), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        vcsHostingService.fork(repositoryOwner, repositoryName, new AsyncCallback<Repository>() {
            @Override
            public void onSuccess(final Repository result) {
                workflow.getContext().setForkedRepositoryName(result.getName());

                workflow.fireStepDoneEvent(CREATE_FORK);
                notificationHelper
                        .finishNotification(messages.stepCreateForkRequestForkCreation(repositoryOwner, repositoryName), notification);

                workflow.setStep(checkoutBranchToPushStep);
                workflow.executeStep();
            }

            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(CREATE_FORK);

                final String errorMessage =
                        messages.stepCreateForkErrorCreatingFork(repositoryOwner, repositoryName, exception.getMessage());
                notificationHelper.finishNotificationWithError(CreateForkStep.class, errorMessage, notification);
            }
        });
    }
}
