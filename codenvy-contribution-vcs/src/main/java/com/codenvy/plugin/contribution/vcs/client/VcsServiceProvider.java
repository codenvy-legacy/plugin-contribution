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
package com.codenvy.plugin.contribution.vcs.client;

import static com.codenvy.ide.ext.git.client.GitRepositoryInitializer.isGitRepository;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;

/**
 * Provider for the {@link com.codenvy.plugin.contribution.vcs.client.VcsService}.
 *
 * @author Kevin Pollet
 */
public class VcsServiceProvider {
    private final AppContext appContext;
    private final GitVcsService gitVcsService;

    @Inject
    public VcsServiceProvider(@Nonnull final AppContext appContext, @Nonnull final GitVcsService gitVcsService) {
        this.appContext = appContext;
        this.gitVcsService = gitVcsService;
    }

    /**
     * Returns the {@link com.codenvy.plugin.contribution.vcs.client.VcsService} implementation corresponding to the current project VCS.
     *
     * @return the {@link com.codenvy.plugin.contribution.vcs.client.VcsService} implementation or {@code null} if not supported or not
     *         initialized.
     */
    public VcsService getVcsService() {
        final CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            return getVcsService(currentProject.getProjectDescription());
        }
        return null;
    }

    /**
     * Returns the {@link com.codenvy.plugin.contribution.vcs.client.VcsService} implementation corresponding to the given project VCS.
     *
     * @return the {@link com.codenvy.plugin.contribution.vcs.client.VcsService} implementation or {@code null} if not supported or not
     *         initialized.
     */
    public VcsService getVcsService(@Nonnull final ProjectDescriptor project) {
        if (isGitRepository(project)) {
            return gitVcsService;
        } else {
            return null;
        }
    }
}
