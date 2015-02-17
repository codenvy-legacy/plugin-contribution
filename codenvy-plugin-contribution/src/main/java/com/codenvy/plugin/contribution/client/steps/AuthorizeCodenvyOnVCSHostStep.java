/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentUser;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.AUTHORIZE_CODENVY_ON_VCS_HOST;

/**
 * This step authorizes Codenvy on the VCS Host.
 *
 * @author Kevin Pollet
 */
public class AuthorizeCodenvyOnVCSHostStep implements Step {
    private static final String BAD_CREDENTIALS_EXCEPTION_MESSAGE = "Bad credentials";

    private final Step               initializeWorkflowContextStep;
    private final NotificationHelper notificationHelper;
    private final VcsHostingService  vcsHostingService;
    private final AppContext         appContext;
    private final ContributeMessages messages;

    @Inject
    public AuthorizeCodenvyOnVCSHostStep(@Nonnull final InitializeWorkflowContextStep initializeWorkflowContextStep,
                                         @Nonnull final NotificationHelper notificationHelper,
                                         @Nonnull final VcsHostingService vcsHostingService,
                                         @Nonnull final AppContext appContext,
                                         @Nonnull final ContributeMessages messages) {
        this.initializeWorkflowContextStep = initializeWorkflowContextStep;
        this.notificationHelper = notificationHelper;
        this.vcsHostingService = vcsHostingService;
        this.appContext = appContext;
        this.messages = messages;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        vcsHostingService.getUserInfo(new AsyncCallback<HostUser>() {
            @Override
            public void onFailure(final Throwable exception) {
                final String exceptionMessage = exception.getMessage();

                if (exceptionMessage != null && exceptionMessage.contains(BAD_CREDENTIALS_EXCEPTION_MESSAGE)) {
                    final CurrentUser currentUser = appContext.getCurrentUser();
                    vcsHostingService.authenticate(currentUser, new AsyncCallback<HostUser>() {
                        @Override
                        public void onFailure(final Throwable exception) {
                            workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);

                            final String exceptionMessage = exception.getMessage();
                            if (exceptionMessage != null && exceptionMessage.contains(BAD_CREDENTIALS_EXCEPTION_MESSAGE)) {
                                notificationHelper
                                        .showError(AuthorizeCodenvyOnVCSHostStep.class,
                                                   messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHost());

                            } else {
                                notificationHelper.showError(AuthorizeCodenvyOnVCSHostStep.class, exception);
                            }
                        }

                        @Override
                        public void onSuccess(final HostUser user) {
                            onVCSHostUserAuthenticated(workflow, user);
                        }
                    });
                } else {
                    workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
                    notificationHelper.showError(AuthorizeCodenvyOnVCSHostStep.class, exception);
                }
            }

            @Override
            public void onSuccess(final HostUser user) {
                onVCSHostUserAuthenticated(workflow, user);
            }
        });
    }

    private void onVCSHostUserAuthenticated(final ContributorWorkflow workflow, final HostUser user) {
        workflow.getContext().setHostUserLogin(user.getLogin());
        workflow.fireStepDoneEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
        workflow.setStep(initializeWorkflowContextStep);
        workflow.executeStep();
    }
}
