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
import com.codenvy.ide.util.Config;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.HostUser;
import com.codenvy.security.oauth.JsOAuthWindow;
import com.codenvy.security.oauth.OAuthCallback;
import com.codenvy.security.oauth.OAuthStatus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.name.Named;

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

    private final String             baseUrl;
    private final Step               initializeWorkflowContextStep;
    private final NotificationHelper notificationHelper;
    private final VcsHostingService  vcsHostingService;
    private final AppContext         appContext;
    private final ContributeMessages messages;

    @Inject
    public AuthorizeCodenvyOnVCSHostStep(@Nonnull @Named("restContext") final String baseUrl,
                                         @Nonnull final InitializeWorkflowContextStep initializeWorkflowContextStep,
                                         @Nonnull final NotificationHelper notificationHelper,
                                         @Nonnull final VcsHostingService vcsHostingService,
                                         @Nonnull final AppContext appContext,
                                         @Nonnull final ContributeMessages messages) {
        this.baseUrl = baseUrl;
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
                    authenticateOnVCSHost(new AsyncCallback<HostUser>() {
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

    /**
     * Authenticates the user on the VCS Host.
     */
    private void authenticateOnVCSHost(final AsyncCallback<HostUser> callback) {
        final CurrentUser currentUser = appContext.getCurrentUser();
        final String authUrl = baseUrl
                               + "/oauth/authenticate?oauth_provider=github&userId=" + currentUser.getProfile().getId()
                               + "&scope=user,repo,write:public_key&redirect_after_login="
                               + Window.Location.getProtocol() + "//"
                               + Window.Location.getHost() + "/ws/"
                               + Config.getWorkspaceName();

        new JsOAuthWindow(authUrl, "error.url", 500, 980, new OAuthCallback() {
            @Override
            public void onAuthenticated(final OAuthStatus authStatus) {
                // maybe it's possible to avoid this request if authStatus contains the vcs host user.
                vcsHostingService.getUserInfo(callback);
            }
        }).loginWithOAuth();
    }
}
