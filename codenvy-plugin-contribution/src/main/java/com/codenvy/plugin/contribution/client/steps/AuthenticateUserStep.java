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
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.ide.api.app.AppContext;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import static com.google.gwt.http.client.URL.encodeQueryString;
import static com.google.gwt.user.client.Window.Location;

/**
 * This step authenticates the user. Currently the user is authenticated with the GitHub VCS Host. Once the user is authenticated a
 * corresponding Codenvy account is created.
 *
 * @author Kevin Pollet
 */
public class AuthenticateUserStep implements Step {
    private final String     baseUrl;
    private final AppContext appContext;
    private final Step       initializeWorkflowStep;

    @Inject
    public AuthenticateUserStep(@Nonnull @Named("restContext") final String baseUrl,
                                @Nonnull final AppContext appContext,
                                @Nonnull final InitializeWorkflowContextStep initializeWorkflowContextStep) {
        this.baseUrl = baseUrl;
        this.appContext = appContext;
        this.initializeWorkflowStep = initializeWorkflowContextStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        if (appContext.getCurrentUser().isUserPermanent()) {
            workflow.setStep(initializeWorkflowStep);
            workflow.executeStep();

        } else {
            final String authUrl = baseUrl
                                   + "/oauth/authenticate?oauth_provider=github&mode=federated_login"
                                   + "&scope=user,repo,write:public_key&redirect_after_login="
                                   + encodeQueryString(baseUrl + "/oauth?redirect_url=" + Location.getHref() + "&oauth_provider=github");

            Location.assign(authUrl);
        }
    }
}
