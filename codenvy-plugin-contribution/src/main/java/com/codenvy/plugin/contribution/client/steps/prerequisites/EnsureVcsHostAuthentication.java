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
public class EnsureVcsHostAuthentication implements Prerequisite {

    private final AppContext appContext;

    @Inject
    public EnsureVcsHostAuthentication(final AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void isFullfilled(final Callback<Boolean, Throwable> callback) {
        final CurrentUser user = appContext.getCurrentUser();
        callback.onSuccess(user.isUserPermanent());
    }

    @Override
    public void fullfill(final Callback<Void, Throwable> callback) {
        isFullfilled(new Callback<Boolean, Throwable>() {
            @Override
            public void onFailure(final Throwable reason) {
                // TODO
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
        // TODO Auto-generated method stub

    }
}
