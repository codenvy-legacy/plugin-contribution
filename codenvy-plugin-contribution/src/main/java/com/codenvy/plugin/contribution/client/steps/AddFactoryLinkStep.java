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

import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;

/**
 * Adds a factory link to the contribution in a comment of the pull request.
 */
public class AddFactoryLinkStep implements Step {

    /**
     * The remote VCS repository.
     */
    private final RepositoryHost repository;

    @Inject
    public AddFactoryLinkStep(final RepositoryHost repository) {
        this.repository = repository;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        // TODO Auto-generated method stub

    }

}
