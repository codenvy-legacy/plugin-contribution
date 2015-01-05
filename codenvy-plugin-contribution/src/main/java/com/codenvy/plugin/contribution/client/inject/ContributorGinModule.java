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
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartView;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartViewImpl;
import com.codenvy.plugin.contribution.client.steps.AddFactoryLinkStep;
import com.codenvy.plugin.contribution.client.steps.AddRemoteStep;
import com.codenvy.plugin.contribution.client.steps.GenerateReviewFactory;
import com.codenvy.plugin.contribution.client.steps.IssuePullRequestStep;
import com.codenvy.plugin.contribution.client.steps.ProposePersistStep;
import com.codenvy.plugin.contribution.client.steps.PushBranchOnForkStep;
import com.codenvy.plugin.contribution.client.steps.RemoteForkStep;
import com.codenvy.plugin.contribution.client.steps.RenameBranchStep;
import com.codenvy.plugin.contribution.client.steps.WaitForForOnRemoteStepFactory;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.GitVcsService;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcshost.GitHubHost;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
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
        bind(VcsService.class).to(GitVcsService.class);
        bind(RepositoryHost.class).to(GitHubHost.class);

        // bind the commit dialog view
        bind(CommitView.class).to(CommitViewImpl.class);

        // bind the part view
        bind(ContributePartView.class).to(ContributePartViewImpl.class);

        // the contribution context singleton
        bind(Context.class).in(Singleton.class);

        // the steps
        bind(ProposePersistStep.class);
        bind(AddFactoryLinkStep.class);
        bind(GenerateReviewFactory.class);
        bind(IssuePullRequestStep.class);
        bind(PushBranchOnForkStep.class);
        bind(RenameBranchStep.class);
        bind(AddRemoteStep.class);
        bind(RemoteForkStep.class);
        install(new GinFactoryModuleBuilder().build(WaitForForOnRemoteStepFactory.class));
    }
}
