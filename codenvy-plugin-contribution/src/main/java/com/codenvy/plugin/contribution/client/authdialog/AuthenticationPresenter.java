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
package com.codenvy.plugin.contribution.client.authdialog;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import static com.google.gwt.http.client.URL.encodeQueryString;

/**
 * {@link com.codenvy.plugin.contribution.client.authdialog.AuthenticationView} presenter.
 *
 * @author Kevin Pollet
 */
public class AuthenticationPresenter implements AuthenticationView.ActionDelegate {
    private static final String LOGIN_PATH                   = "/site/login";
    private static final String CREATE_ACCOUNT_PATH          = "/site/create-account";
    private static final String REDIRECT_URL_QUERY_PARAM_KEY = "redirect_url";

    private final AuthenticationView view;

    @Inject
    public AuthenticationPresenter(final AuthenticationView view) {
        this.view = view;
        this.view.setDelegate(this);
    }

    public void showDialog() {
        view.show();
    }

    @Override
    public void onLoginClicked() {
        Window.Location.replace(LOGIN_PATH + getQueryParamsForAuthentication());
    }

    @Override
    public void onCreateAccountClicked() {
        Window.Location.replace(CREATE_ACCOUNT_PATH + getQueryParamsForAuthentication());
    }

    private String getQueryParamsForAuthentication() {
        return "?" + REDIRECT_URL_QUERY_PARAM_KEY + "=" + encodeQueryString(Window.Location.getHref());
    }
}
