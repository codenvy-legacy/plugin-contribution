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
package com.codenvy.plugin.contribution.vcs.client.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.GitHubHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.google.gwt.inject.client.AbstractGinModule;

/**
 * Gin module definition for the contribution VCS.
 */
@ExtensionGinModule
public class ContributorVcsModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(VcsServiceProvider.class);
        bind(VcsHostingService.class).to(GitHubHostingService.class);
    }
}
