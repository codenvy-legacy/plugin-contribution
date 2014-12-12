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
package com.codenvy.plugin.contribution.client.contribdialog;

import javax.annotation.Nonnull;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.wizard.WizardDialog;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Dialog for the contribution configuration.
 */
public class PreContributeWizardPresenter implements WizardDialog, PreContributeWizardView.ActionDelegate {

    /**
     * The app content.
     */
    private final AppContext appContext;

    /**
     * The component view.
     */
    private final PreContributeWizardView view;

    /**
     * The service for VCS operations.
     */
    private final VcsService vcsService;

    /**
     * The following operation to finish the contribution.
     */
    private FinishContributionOperation finishContribution;

    @AssistedInject
    public PreContributeWizardPresenter(final AppContext appContext,
                                        final ContributeMessages messages,
                                        final PreContributeWizardView view,
                                        final VcsService vcsService,
                                        @Assisted @Nonnull final FinishContributionOperation finishContribution) {
        this.appContext = appContext;
        this.view = view;
        this.vcsService = vcsService;
        this.finishContribution = finishContribution;
    }

    @Override
    public void show() {
        view.reset();
        view.show();
    }

    @Override
    public void onContributeClicked() {
        this.finishContribution.finishContribution(this.view.getBranchName(), this.view.getPullRequestComment());
    }

    @Override
    public void onCancelClicked() {
        this.view.hide();
    }

    @Override
    public void suggestBranchName(final AsyncCallback<String> callback) {
        final CurrentProject currentProject = this.appContext.getCurrentProject();
        if (currentProject == null) {
            callback.onSuccess("");
        }
        final ProjectDescriptor projectDescriptor = currentProject.getProjectDescription();
        this.vcsService.getBranchName(projectDescriptor, callback);
    }

    @Override
    public void updateControls() {
        final String branchName = this.view.getBranchName();
        if (branchName == null || "".equals(branchName)) {
            this.view.setContributeEnabled(false);
        } else {
            this.view.setContributeEnabled(true);
        }
    }

}
