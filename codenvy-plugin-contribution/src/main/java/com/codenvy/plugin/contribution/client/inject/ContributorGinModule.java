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
import com.codenvy.plugin.contribution.client.contribdialog.PreContributeWizardPresenterFactory;
import com.codenvy.plugin.contribution.client.contribdialog.PreContributeWizardView;
import com.codenvy.plugin.contribution.client.contribdialog.PreContributeWizardViewImpl;
import com.codenvy.plugin.contribution.client.vcs.GitVcsService;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcshost.GithubHost;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

/**
 * Gin module definition for the contributor extension.
 */
@ExtensionGinModule
public class ContributorGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(VcsService.class).to(GitVcsService.class);
        bind(RepositoryHost.class).to(GithubHost.class);

        // bind the configure contribution dialog
        install(new GinFactoryModuleBuilder().build(PreContributeWizardPresenterFactory.class));
        // bind the configure dialog view
        bind(PreContributeWizardView.class).to(PreContributeWizardViewImpl.class);
    }
}
