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
package com.codenvy.ide.contributor.client;

import com.codenvy.api.user.gwt.client.UserServiceClient;
import com.codenvy.api.user.shared.dto.UserDescriptor;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.Notification.Status;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.github.shared.GitHubUser;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.security.oauth.JsOAuthWindow;
import com.codenvy.ide.security.oauth.OAuthCallback;
import com.codenvy.ide.security.oauth.OAuthStatus;
import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.ide.util.Config;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ContributeAction extends ProjectAction {

    private final UserServiceClient      userServiceClient;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final NotificationManager    notificationManager;
    private final DialogFactory          dialogFactory;
    private final String                 baseUrl;
    private final GitAgent               gitAgent;

    private UserDescriptor               userDescriptor;

    @Inject
    public ContributeAction(ContributeResources contributeResources,
                            ContributorLocalizationConstant localConstant,
                            UserServiceClient userServiceClient,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            NotificationManager notificationManager,
                            DialogFactory dialogFactory,
                            @Named("restContext") String baseUrl,
                            GitAgent gitAgent) {
        super(localConstant.contributorButtonName(), localConstant.contributorButtonDescription(), contributeResources.contributeButton());

        this.userServiceClient = userServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.baseUrl = baseUrl;
        this.gitAgent = gitAgent;
    }

    @Override
    protected void updateProjectAction(ActionEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getCurrentUserInfo();
    }

    private void getCurrentUserInfo() {
        // get current user's Codenvy account
        userServiceClient.getCurrentUser(
                         new AsyncRequestCallback<UserDescriptor>(dtoUnmarshallerFactory.newUnmarshaller(UserDescriptor.class)) {
                             @Override
                             protected void onSuccess(UserDescriptor user) {
                                 userDescriptor = user;
                                 // TODO if user is anonymous create a Codenvy account
                                 // get current user's associated github account
                                 getVCSUserInfo();
                             }

                             @Override
                             protected void onFailure(Throwable exception) {
                                 notificationManager.showNotification(new Notification(exception.getMessage(), Notification.Type.ERROR));
                             }
                         }
                         );
    }

    private void getVCSUserInfo() {
        gitAgent.getUserInfo(new AsyncRequestCallback<GitHubUser>() {
            @Override
            protected void onSuccess(GitHubUser result) {
                onVCSUserAuthenticated();
            }

            @Override
            protected void onFailure(Throwable exception) {
                if (exception.getMessage().contains("Bad credentials")) {
                    // get user's Github account authenticated as it is not already
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
                         + "&scope=user,repo,write:public_key&userId=" + userDescriptor.getId()
                         + "&redirect_after_login="
                         + Window.Location.getProtocol() + "//"
                         + Window.Location.getHost() + "/ws/"
                         + Config.getWorkspaceName();
        JsOAuthWindow authWindow = new JsOAuthWindow(authUrl, "error.url", 500, 980, new OAuthCallback() {

            @Override
            public void onAuthenticated(OAuthStatus authStatus) {
                onVCSUserAuthenticated();
            }

        });
        authWindow.loginWithOAuth();
    }

    private void onVCSUserAuthenticated() {
        notificationManager.showNotification(new Notification("User successfully authenticated.", Notification.Type.INFO, Status.FINISHED));

        // TODO check if user has a fork already existing for origin repo. if not we create the fork
        // TODO open wizard to configure PR (branch name, descr, review)
        // TODO rename local branch with name given in PR config
        // TODO push local branch to forked repo on GitHub
    }
}
