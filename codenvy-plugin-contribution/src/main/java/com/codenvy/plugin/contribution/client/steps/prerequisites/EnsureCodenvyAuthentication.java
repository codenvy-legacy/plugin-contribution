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
package com.codenvy.plugin.contribution.client.steps.prerequisites;

import javax.inject.Inject;

import com.codenvy.plugin.contribution.client.steps.Context;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.Call;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

import com.codenvy.plugin.contribution.client.exception.AuthenticationFailedException;
import com.codenvy.plugin.contribution.client.exception.NoAuthenticationException;
import com.codenvy.plugin.contribution.client.steps.Prerequisite;
import com.google.gwt.core.client.Callback;

/**
 * Prerequisite for steps that need the current user to be authenticated on codenvy.
 */
public class EnsureCodenvyAuthentication implements Prerequisite {

    private final AppContext appContext;

    @Inject
    public EnsureCodenvyAuthentication(final AppContext appContext) {
        this.appContext = appContext;
    }

    private void isFulfilled(final Callback<Boolean, Throwable> callback) {
        final CurrentUser user = appContext.getCurrentUser();
        callback.onSuccess(user.isUserPermanent());
    }

    @Override
    public void fulfill(final Context context, final Callback<Void, Throwable> callback) {
        isFulfilled(new Callback<Boolean, Throwable>() {
            @Override
            public void onFailure(final Throwable reason) {
                // doesn't happen with current implementation
            }
            @Override
            public void onSuccess(final Boolean fullfilled) {
                if (fullfilled) {
                    callback.onSuccess(null);
                } else {
                    doAuthentication(callback);
                }
            }
        });
    }

    private void doAuthentication(final Callback<Void, Throwable> callback) {
        new JsOAuthWindow("/site/login", "error.url", 500, 980, new OAuthCallback() {
            @Override
            public void onAuthenticated(final OAuthStatus authStatus) {
                if (authStatus == null) {
                    callback.onFailure(new NoAuthenticationException());
                } else {
                    switch (authStatus) {
                        case FAILED:
                            callback.onFailure(new AuthenticationFailedException());
                            break;
                        case LOGGED_OUT:
                        case NOT_PERFORMED:
                            callback.onFailure(new NoAuthenticationException());
                            break;
                        case LOGGED_IN:
                            callback.onSuccess(null);
                            break;
                        default:
                            break;
                    }
                }
            }
        }).loginWithOAuth();
    }

    @Override
    public Promise<Void> fulfill(final Context context) {
        return CallbackPromiseHelper.createFromCallback(new Call<Void, Throwable>() {
            @Override
            public void makeCall(final Callback<Void, Throwable> callback) {
                fulfill(context, callback);
            }
        });
    }
}
