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
package com.codenvy.plugin.contribution.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitView;
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitViewImpl;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartView;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartViewImpl;
import com.codenvy.plugin.contribution.client.steps.AddForkRemoteStep;
import com.codenvy.plugin.contribution.client.steps.AddReviewFactoryLinkStep;
import com.codenvy.plugin.contribution.client.steps.AuthorizeCodenvyOnVCSHostStep;
import com.codenvy.plugin.contribution.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.steps.CreateForkStep;
import com.codenvy.plugin.contribution.client.steps.CreateWorkBranchStep;
import com.codenvy.plugin.contribution.client.steps.GenerateReviewFactoryStep;
import com.codenvy.plugin.contribution.client.steps.InitializeWorkflowContextStep;
import com.codenvy.plugin.contribution.client.steps.IssuePullRequestStep;
import com.codenvy.plugin.contribution.client.steps.PushBranchOnForkStep;
import com.codenvy.plugin.contribution.client.steps.RenameWorkBranchStep;
import com.codenvy.plugin.contribution.client.steps.WaitForkOnRemoteStepFactory;
import com.codenvy.plugin.contribution.client.vcs.GitVcsService;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcs.VcsServiceProvider;
import com.codenvy.plugin.contribution.client.vcs.hosting.GitHubHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

import javax.inject.Singleton;

/**
 * Gin module definition for the contributor extension.
 */
@ExtensionGinModule
public class ContributorGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(VcsServiceProvider.class);
        bind(VcsService.class).to(GitVcsService.class);
        bind(VcsHostingService.class).to(GitHubHostingService.class);

        // bind the commit dialog view
        bind(CommitView.class).to(CommitViewImpl.class);

        // bind the part view
        bind(ContributePartView.class).to(ContributePartViewImpl.class);
        bind(ContributePartPresenter.class).in(Singleton.class);

        // the steps
        bind(ContributorWorkflow.class).in(Singleton.class);
        bind(InitializeWorkflowContextStep.class);
        bind(CreateWorkBranchStep.class);
        bind(CommitWorkingTreeStep.class);
        bind(AuthorizeCodenvyOnVCSHostStep.class);
        bind(AddReviewFactoryLinkStep.class);
        bind(GenerateReviewFactoryStep.class);
        bind(IssuePullRequestStep.class);
        bind(PushBranchOnForkStep.class);
        bind(RenameWorkBranchStep.class);
        bind(AddForkRemoteStep.class);
        bind(CreateForkStep.class);
        install(new GinFactoryModuleBuilder().build(WaitForkOnRemoteStepFactory.class));
    }
}
