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
        final String originRepositoryOwner = context.getOriginRepositoryOwner();
        final String originRepositoryName = context.getOriginRepositoryName();
        final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
        final String upstreamRepositoryName = context.getUpstreamRepositoryName();

        // the upstream repository has been cloned a fork must be created
        if (originRepositoryOwner.equalsIgnoreCase(upstreamRepositoryOwner) &&
            originRepositoryName.equalsIgnoreCase(upstreamRepositoryName)) {
            vcsHostingService.getUserFork(context.getHostUserLogin(), upstreamRepositoryOwner, upstreamRepositoryName,
                                          new AsyncCallback<Repository>() {
                                              @Override
                                              public void onSuccess(final Repository fork) {
                                                  notificationHelper.showInfo(messages.stepCreateForkUseExistingFork());
                                                  proceed(fork.getName(), workflow);
                                              }

                                              @Override
                                              public void onFailure(final Throwable exception) {
                                                  if (exception instanceof NoUserForkException) {
                                                      createFork(workflow, upstreamRepositoryOwner, upstreamRepositoryName);
                                                      return;
                                                  }

                                                  workflow.fireStepErrorEvent(CREATE_FORK);
                                                  notificationHelper.showError(CreateForkStep.class, exception);
                                              }
                                          });


            // user fork has been cloned
        } else {
            notificationHelper.showInfo(messages.stepCreateForkUseExistingFork());
            proceed(originRepositoryName, workflow);
        }
    }

    private void createFork(final ContributorWorkflow workflow, final String upstreamRepositoryOwner, final String upstreamRepositoryName) {
        final Notification notification =
                new Notification(messages.stepCreateForkCreateFork(upstreamRepositoryOwner, upstreamRepositoryName), INFO, PROGRESS);
        notificationHelper.showNotification(notification);

        vcsHostingService.fork(upstreamRepositoryOwner, upstreamRepositoryName, new AsyncCallback<Repository>() {
            @Override
            public void onSuccess(final Repository result) {
                notificationHelper.finishNotification(
                        messages.stepCreateForkRequestForkCreation(upstreamRepositoryOwner, upstreamRepositoryName),
                        notification);

                proceed(result.getName(), workflow);
            }

            @Override
            public void onFailure(final Throwable exception) {
                workflow.fireStepErrorEvent(CREATE_FORK);

                final String errorMessage =
                        messages.stepCreateForkErrorCreatingFork(upstreamRepositoryOwner, upstreamRepositoryName, exception.getMessage());
                notificationHelper.finishNotificationWithError(CreateForkStep.class, errorMessage, notification);
            }
        });
    }

    private void proceed(final String forkName, final ContributorWorkflow workflow) {
        workflow.getContext().setForkedRepositoryName(forkName);
        workflow.fireStepDoneEvent(CREATE_FORK);
        workflow.setStep(checkoutBranchToPushStep);
        workflow.executeStep();
    }
}
