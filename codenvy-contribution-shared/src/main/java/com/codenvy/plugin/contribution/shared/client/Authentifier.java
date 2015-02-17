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
package com.codenvy.plugin.contribution.shared.client;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentUser;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Authentication logic.
 */
public class Authentifier {

    /** String user to identify failures caused by no credential or bad credentials. */
    private static final String BAD_CREDENTIALS_EXCEPTION_MESSAGE = "Bad credentials";

    private final NotificationHelper notificationHelper;
    private final VcsHostingService vcsHostingService;
    private final AppContext appContext;
    private final AuthentifierMessages messages;

    @Inject
    public Authentifier(@Nonnull final NotificationHelper notificationHelper,
                        @Nonnull final VcsHostingService vcsHostingService,
                        @Nonnull final AppContext appContext,
                        @Nonnull final AuthentifierMessages messages) {
        this.notificationHelper = notificationHelper;
        this.vcsHostingService = vcsHostingService;
        this.appContext = appContext;
        this.messages = messages;
    }

    /**
     * User authentication.
     * 
     * @param callback
     */
    public void authenticate(final AsyncCallback<HostUser> callback) {
        checkIfAlreadyAuthenticated(callback);
    }

    /**
     * Checks if the user is already authenticated.
     * 
     * @param callback
     */
    private void checkIfAlreadyAuthenticated(final AsyncCallback<HostUser> callback) {
        vcsHostingService.getUserInfo(new AsyncCallback<HostUser>() {
            @Override
            public void onFailure(final Throwable exception) {
                final String exceptionMessage = exception.getMessage();

                if (exceptionMessage != null && exceptionMessage.contains(BAD_CREDENTIALS_EXCEPTION_MESSAGE)) {
                    // not authenticated, do the real authentication attempt
                    doAuthentication(callback);
                } else {
                    notificationHelper.showError(Authentifier.class, exception);
                    callback.onFailure(exception);
                }
            }

            @Override
            public void onSuccess(final HostUser user) {
                callback.onSuccess(user);
            }
        });
    }

    /**
     * Really authenticate the user.
     * 
     * @param callback
     */
    private void doAuthentication(final AsyncCallback<HostUser> callback) {
        final CurrentUser currentUser = appContext.getCurrentUser();
        vcsHostingService.authenticate(currentUser, new AsyncCallback<HostUser>() {
            @Override
            public void onFailure(final Throwable exception) {
                final String exceptionMessage = exception.getMessage();
                if (exceptionMessage != null && exceptionMessage.contains(BAD_CREDENTIALS_EXCEPTION_MESSAGE)) {
                    notificationHelper
                                      .showError(Authentifier.class,
                                                 messages.errorCannotAccessVCSHost());

                } else {
                    notificationHelper.showError(Authentifier.class, exception);
                }
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final HostUser user) {
                callback.onSuccess(user);
            }
        });
    }
}
