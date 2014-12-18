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
package com.codenvy.plugin.contribution.client;

import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.api.app.CurrentUser;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.util.Config;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.authdialog.AuthenticationPresenter;
import com.codenvy.plugin.contribution.client.steps.ConfigureStep;
import com.codenvy.plugin.contribution.client.steps.RemoteForkStep;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.HostUser;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.codenvy.security.oauth.JsOAuthWindow;
import com.codenvy.security.oauth.OAuthCallback;
import com.codenvy.security.oauth.OAuthStatus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ContributeAction extends ProjectAction {
    /**
     * Step where the user configures the contribution.
     */
    private final ConfigureStep configureStep;

    /**
     * The notification manager.
     */
    private final NotificationManager notificationManager;

    /**
     * The REST base URL.
     */
    private final String baseUrl;

    /**
     * A step to create a fork on the remote repository.
     */
    private RemoteForkStep remoteForkStep;

    /**
     * The remote VCS host interface.
     */
    private final RepositoryHost repositoryHost;

    /**
     * Contributor plugin context.
     */
    private final Context context;

    /**
     * Contributor plugin configuration.
     */
    private final Configuration config;

    /**
     * Presenter used to authenticate user in Codenvy.
     */
    private final AuthenticationPresenter authenticationPresenter;

    @Inject
    public ContributeAction(final ConfigureStep configureStep,
                            final Context context,
                            final ContributeResources contributeResources,
                            final ContributeMessages messages,
                            final DtoFactory dtoFactory,
                            final NotificationManager notificationManager,
                            final @Named("restContext") String baseUrl,
                            final RemoteForkStep remoteForkStep,
                            final RepositoryHost repositoryHost,
                            final AuthenticationPresenter authenticationPresenter) {
        super(messages.contributorButtonName(), messages.contributorButtonDescription(), contributeResources.contributeButton());

        this.configureStep = configureStep;
        this.notificationManager = notificationManager;
        this.baseUrl = baseUrl;
        this.remoteForkStep = remoteForkStep;
        this.repositoryHost = repositoryHost;
        this.context = context;
        this.authenticationPresenter = authenticationPresenter;
        this.config = dtoFactory.createDto(Configuration.class);
    }

    @Override
    protected void updateProjectAction(final ActionEvent e) {

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        // first we check if the user is authenticated on the vcs host
        repositoryHost.getUserInfo(new AsyncCallback<HostUser>() {
            @Override
            public void onFailure(final Throwable exception) {
                final String exceptionMessage = exception.getMessage();
                if (exceptionMessage != null && exceptionMessage.contains("Bad credentials")) {
                    authenticateOnVCSHost();

                } else {
                    handleError(exception);
                }
            }

            @Override
            public void onSuccess(final HostUser user) {
                onVCSHostUserAuthenticated(user);
            }
        });
    }

    /**
     * Authenticates the user on the VCS Host.
     */
    private void authenticateOnVCSHost() {
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
                repositoryHost.getUserInfo(new AsyncCallback<HostUser>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        handleError(exception);
                    }

                    @Override
                    public void onSuccess(final HostUser user) {
                        onVCSHostUserAuthenticated(user);
                    }
                });
            }

        }).loginWithOAuth();
    }

    /**
     * Checks that the user authenticated on the VCS Host is authenticated on Codenvy.
     *
     * @param user
     *         the user authenticated on the VCS Host.
     */
    private void onVCSHostUserAuthenticated(final HostUser user) {
        context.setHostUserLogin(user.getLogin());

        if (!appContext.getCurrentUser().isUserPermanent()) {
            authenticationPresenter.showDialog();

        } else {
            remoteForkStep.execute(context, config); // parallel with the other steps
            configureStep.execute(context, config);
        }
    }

    /**
     * Handles an exception and display the error message in a notification.
     *
     * @param exception
     *         the exception to handle.
     */
    private void handleError(final Throwable exception) {
        notificationManager.showNotification(new Notification(exception.getMessage(), Notification.Type.ERROR));
        Log.error(ContributeAction.class, exception.getMessage());
    }
}
