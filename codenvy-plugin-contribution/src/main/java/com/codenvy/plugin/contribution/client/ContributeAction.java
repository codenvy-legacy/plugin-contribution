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
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.security.oauth.JsOAuthWindow;
import com.codenvy.ide.security.oauth.OAuthCallback;
import com.codenvy.ide.security.oauth.OAuthStatus;
import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.ide.util.Config;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.vcshost.HostUser;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ContributeAction extends ProjectAction {

    private final UserServiceClient      userServiceClient;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final NotificationManager    notificationManager;
    private final DialogFactory          dialogFactory;
    private final String                 baseUrl;
    private final RepositoryHost         repositoryHost;

    private UserDescriptor               userDescriptor;
    private HostUser                     hostUser;

    @Inject
    public ContributeAction(final ContributeResources contributeResources,
                            final ContributeMessages messages,
                            final UserServiceClient userServiceClient,
                            final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            final NotificationManager notificationManager,
                            final DialogFactory dialogFactory,
                            final @Named("restContext") String baseUrl,
                            final RepositoryHost repositoryHost) {
        super(messages.contributorButtonName(), messages.contributorButtonDescription(), contributeResources.contributeButton());

        this.userServiceClient = userServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.baseUrl = baseUrl;
        this.repositoryHost = repositoryHost;
    }

    @Override
    protected void updateProjectAction(final ActionEvent e) {

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (appContext.getCurrentUser().isUserPermanent()) {
            getCurrentUserInfo();
        } else {
            // TODO as user is temporary create a Codenvy account and then getCurrentUserInfo
            notificationManager.showNotification(new Notification("Current user isn't permanent.", Notification.Type.ERROR, Status.FINISHED));
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
                                                      "Codenvy requests authorization through OAuth2 protocol",
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
            public void onAuthenticated(OAuthStatus authStatus) {
                // TODO get GitHub user
                onVCSUserAuthenticated();
            }

        });
        authWindow.loginWithOAuth();
    }

    private void onVCSUserAuthenticated() {
        notificationManager.showNotification(new Notification("User successfully authenticated.", Notification.Type.INFO, Status.FINISHED));

        /* parallel with the other items */
        // TODO check if user has a fork already existing for origin repo & fork if not
        
        /* sequential */
        // TODO open wizard to configure PR (branch name, descr, review)
        // TODO rename local branch with name given in PR config
        // TODO push local branch to forked repo on GitHub
    }
}
