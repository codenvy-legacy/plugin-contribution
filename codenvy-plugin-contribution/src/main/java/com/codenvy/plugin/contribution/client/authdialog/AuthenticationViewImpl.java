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

import com.codenvy.ide.ui.window.Window;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;

import javax.inject.Inject;

/**
 * {@link com.codenvy.plugin.contribution.client.authdialog.AuthenticationView} implementation.
 *
 * @author Kevin Pollet
 */
public class AuthenticationViewImpl extends Window implements AuthenticationView {
    /** The uUI binder for this component. */
    private static final AuthenticationViewUiBinder UI_BINDER = GWT.create(AuthenticationViewUiBinder.class);

    @UiField(provided = true)
    ContributeMessages messages;

    private ActionDelegate delegate;

    @Inject
    public AuthenticationViewImpl(final ContributeMessages messages) {
        this.messages = messages;

        setWidget(UI_BINDER.createAndBindUi(this));
        setTitle(messages.authenticationDialogTitle());

        final Button createAccountButton =
                createButton(messages.authenticationDialogCreateAccountButtonText(), "authentication-dialog-create-account-button",
                             new ClickHandler() {
                                 @Override
                                 public void onClick(ClickEvent event) {
                                     delegate.onCreateAccountClicked();
                                 }
                             });
        createAccountButton.addStyleName(Window.resources.centerPanelCss().blueButton());

        final Button loginButton =
                createButton(messages.authenticationDialogLoginButtonText(), "authentication-dialog-login-button", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        delegate.onLoginClicked();
                    }
                });

        getFooter().add(createAccountButton);
        getFooter().add(loginButton);
    }

    /**
     * Sets the {@link com.codenvy.plugin.contribution.client.authdialog.AuthenticationView.ActionDelegate}.
     *
     * @param delegate
     *         the {@link com.codenvy.plugin.contribution.client.authdialog.AuthenticationView.ActionDelegate}, must not be {@code null}.
     */
    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    protected void onClose() {
        // nothing to do
    }
}
