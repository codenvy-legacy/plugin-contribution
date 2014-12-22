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
package com.codenvy.plugin.contribution.client.dialogs.contribute;

import com.codenvy.ide.api.wizard.WizardDialog;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import javax.annotation.Nonnull;

/**
 * Dialog for the contribution configuration.
 */
public class PreContributeWizardPresenter implements WizardDialog, PreContributeWizardView.ActionDelegate {

    /**
     * The component view.
     */
    private final PreContributeWizardView view;

    /**
     * The following operation to finish the contribution.
     */
    private final FinishContributionOperation finishContribution;

    /**
     * The contribution configuration, which contains values choosen by the user.
     */
    private final Configuration configuration;

    /**
     * The contribution context which contains project, work branch etc.
     */
    private final Context context;


    @AssistedInject
    public PreContributeWizardPresenter(final PreContributeWizardView view,
                                        @Assisted @Nonnull final FinishContributionOperation finishContribution,
                                        @Assisted @Nonnull final Context context,
                                        @Assisted @Nonnull final Configuration config) {
        this.view = view;
        this.finishContribution = finishContribution;
        this.configuration = config;
        this.context = context;

        this.view.setDelegate(this);
    }

    @Override
    public void show() {
        view.reset();
        view.show();
    }

    @Override
    public void onContributeClicked() {
        this.configuration.withBranchName(this.view.getBranchName())
                          .withPullRequestComment(this.view.getPullRequestComment())
                          .withContributionTitle(this.view.getContribTitle());
        this.view.hide();
        this.finishContribution.finishContribution(this.context, this.configuration);
    }

    @Override
    public void onCancelClicked() {
        this.view.hide();
    }

    @Override
    public String suggestBranchName() {
        return this.context.getWorkBranchName();
    }

    @Override
    public void updateControls() {
        final String branchName = this.view.getBranchName();
        final String title = this.view.getContribTitle();
        boolean ready = true;
        if (branchName == null || "".equals(branchName)) {
            ready = false;
        }
        if (title == null || "".equals(title)) {
            ready = false;
        }
        this.view.setContributeEnabled(ready);
    }

}
