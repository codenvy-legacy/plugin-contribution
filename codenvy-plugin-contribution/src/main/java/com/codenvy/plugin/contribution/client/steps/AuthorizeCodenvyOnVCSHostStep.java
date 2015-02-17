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
package com.codenvy.plugin.contribution.client.steps;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.AUTHORIZE_CODENVY_ON_VCS_HOST;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.plugin.contribution.shared.client.Authentifier;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This step authorizes Codenvy on the VCS Host.
 *
 * @author Kevin Pollet
 */
public class AuthorizeCodenvyOnVCSHostStep implements Step {

    private final Step         initializeWorkflowContextStep;

    private final Authentifier authentifier;

    @Inject
    public AuthorizeCodenvyOnVCSHostStep(@Nonnull final InitializeWorkflowContextStep initializeWorkflowContextStep,
                                         @Nonnull final Authentifier authentifier) {
        this.initializeWorkflowContextStep = initializeWorkflowContextStep;
        this.authentifier = authentifier;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        this.authentifier.authenticate(new AsyncCallback<HostUser>() {
            @Override
            public void onFailure(final Throwable caught) {
                workflow.fireStepErrorEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
            }
            @Override
            public void onSuccess(final HostUser user) {
                onVCSHostUserAuthenticated(workflow, user);
            }
        });
    }

    private void onVCSHostUserAuthenticated(final ContributorWorkflow workflow, final HostUser user) {
        workflow.getContext().setHostUserLogin(user.getLogin());
        workflow.fireStepDoneEvent(AUTHORIZE_CODENVY_ON_VCS_HOST);
        workflow.setStep(initializeWorkflowContextStep);
        workflow.executeStep();
    }
}
