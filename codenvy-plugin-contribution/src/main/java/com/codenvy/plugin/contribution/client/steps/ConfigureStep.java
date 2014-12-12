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

import javax.inject.Inject;

import com.codenvy.plugin.contribution.client.contribdialog.FinishContributionOperation;
import com.codenvy.plugin.contribution.client.contribdialog.PreContributeWizardPresenter;
import com.codenvy.plugin.contribution.client.contribdialog.PreContributeWizardPresenterFactory;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;

public class ConfigureStep implements Step {

    private final PreContributeWizardPresenterFactory configureWizardFactory;
    private final RenameBranchStep renameBranchStep;

    @Inject
    public ConfigureStep(final PreContributeWizardPresenterFactory configureWizardFactory,
                         final RenameBranchStep renameBranchStep) {
        this.configureWizardFactory = configureWizardFactory;
        this.renameBranchStep = renameBranchStep;
    }

    public void execute(final Context context, final Configuration configuration) {
        final PreContributeWizardPresenter dialog = this.configureWizardFactory.create(new FinishContributionOperation() {

            @Override
            public void finishContribution(final Context context, final Configuration config) {
                renameBranchStep.execute(context, config);
            }
        }, context, configuration);
        dialog.show();
    }
}
