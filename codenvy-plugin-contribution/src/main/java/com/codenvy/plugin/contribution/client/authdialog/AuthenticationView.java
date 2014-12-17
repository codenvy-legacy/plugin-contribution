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

import com.codenvy.ide.api.mvp.View;

/**
 * View used to ask the user to authenticate in Codenvy.
 *
 * @author Kevin Pollet
 */
public interface AuthenticationView extends View<AuthenticationView.ActionDelegate> {
    /**
     * Delegates for view actions.
     */
    interface ActionDelegate {
        /**
         * Called when the login button is clicked.
         */
        void onLoginClicked();

        /**
         * Called when the create account button is clicked.
         */
        void onCreateAccountClicked();
    }

    /**
     * Shows the authentication view.
     */
    void show();

    /**
     * Hides the authentication view.
     */
    void hide();
}
