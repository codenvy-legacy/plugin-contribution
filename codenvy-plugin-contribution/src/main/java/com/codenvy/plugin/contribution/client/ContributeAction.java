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

import com.codenvy.api.user.gwt.client.UserServiceClient;
import com.codenvy.api.user.shared.dto.UserDescriptor;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.Notification.Status;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshaller;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.plugin.contribution.client.authdialog.AuthenticationPresenter;
import com.codenvy.security.oauth.JsOAuthWindow;
import com.codenvy.security.oauth.OAuthCallback;
import com.codenvy.security.oauth.OAuthStatus;
import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.ide.util.Config;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.steps.ConfigureStep;
import com.codenvy.plugin.contribution.client.steps.RemoteForkStep;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.HostUser;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ContributeAction extends ProjectAction {

    /**
     * I18n messages.
     */
    private final ContributeMessages messages;

    /**
     * Step where the user configures the contribution.
     */
    private final ConfigureStep configureStep;

    /**
     * Service to retrieve user informations.
     */
    private final UserServiceClient userServiceClient;

    /**
     * Factory of {@link DtoUnmarshaller} objects.
     */
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    /**
     * The notification manager.
     */
    private final NotificationManager notificationManager;

    /**
     * Factory for message dialogs.
     */
    private final DialogFactory dialogFactory;

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

    private final Context context;
    private final Configuration config;
    /**
     * The local user identity.
     */
    private UserDescriptor userDescriptor;

    /**
     * The user identity on the remote repository host.
     */
    private HostUser hostUser;

    private final AuthenticationPresenter authenticationPresenter;

    @Inject
    public ContributeAction(final ConfigureStep configureStep,
                            final Context context,
                            final ContributeResources contributeResources,
                            final ContributeMessages messages,
                            final DtoFactory dtoFactory,
                            final UserServiceClient userServiceClient,
                            final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            final NotificationManager notificationManager,
                            final DialogFactory dialogFactory,
                            final @Named("restContext") String baseUrl,
                            final RemoteForkStep remoteForkStep,
                            final RepositoryHost repositoryHost,
                            final AuthenticationPresenter authenticationPresenter) {
        super(messages.contributorButtonName(), messages.contributorButtonDescription(), contributeResources.contributeButton());

        this.configureStep = configureStep;
        this.userServiceClient = userServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.messages = messages;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
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
        if (appContext.getCurrentUser().isUserPermanent()) {
            getCurrentUserInfo();
        } else {
            authenticationPresenter.showDialog();
        }
    }

    private void getCurrentUserInfo() {
        // get current user's Codenvy account
        userServiceClient.getCurrentUser(
                         new AsyncRequestCallback<UserDescriptor>(dtoUnmarshallerFactory.newUnmarshaller(UserDescriptor.class)) {
                             @Override
                             protected void onSuccess(final UserDescriptor user) {
                                 userDescriptor = user;
                                 // get current user's associated VCS account
                                 getVCSUserInfo();
                             }

                             @Override
                             protected void onFailure(final Throwable exception) {
                                 notificationManager.showNotification(new Notification(exception.getMessage(), Notification.Type.ERROR));
                                 Log.error(ContributeAction.class, exception.getMessage());
                             }
                         }
                         );
    }

    private void getVCSUserInfo() {
        repositoryHost.getUserInfo(new AsyncCallback<HostUser>() {
            @Override
            public void onSuccess(final HostUser result) {
                hostUser = result;
                onVCSUserAuthenticated();
            }

            @Override
            public void onFailure(final Throwable exception) {
                // authenticate user's Github account
                if (exception.getMessage().contains("Bad credentials")) {
                    dialogFactory.createConfirmDialog("GitHub",
                                                      messages.repositoryHostAuthorizeMessage(),
                                                      new ConfirmCallback() {
                                                          @Override
                                                          public void accepted() {
                                                              showAuthWindow();
                                                          }
                                                      }, null).show();
                }
            }
        });
    }

    private void showAuthWindow() {
        String authUrl = baseUrl
                         + "/oauth/authenticate?oauth_provider=github"
                         + "&scope=user,repo,write:public_key&userId=" + (userDescriptor != null ? userDescriptor.getId() : "")
                         + "&redirect_after_login="
                         + Window.Location.getProtocol() + "//"
                         + Window.Location.getHost() + "/ws/"
                         + Config.getWorkspaceName();
        JsOAuthWindow authWindow = new JsOAuthWindow(authUrl, "error.url", 500, 980, new OAuthCallback() {

            @Override
            public void onAuthenticated(final OAuthStatus authStatus) {
                // TODO get GitHub user
                onVCSUserAuthenticated();
            }

        });
        authWindow.loginWithOAuth();
    }

    private void onVCSUserAuthenticated() {
        notificationManager.showNotification(new Notification("User successfully authenticated.", Notification.Type.INFO, Status.FINISHED));

        /* parallel with the other steps */
        this.remoteForkStep.execute(context, config);

        this.configureStep.execute(this.context, this.config);
    }
}
