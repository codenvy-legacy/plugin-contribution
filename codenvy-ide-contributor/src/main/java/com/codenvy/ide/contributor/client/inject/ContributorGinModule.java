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
package com.codenvy.ide.contributor.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.contributor.client.vcs.GitVcsService;
import com.codenvy.ide.contributor.client.vcs.VcsService;
import com.google.gwt.inject.client.AbstractGinModule;

/**
 * Gin module definition for the contributor extension.
 */
@ExtensionGinModule
public class ContributorGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(VcsService.class).to(GitVcsService.class);
    }
}
