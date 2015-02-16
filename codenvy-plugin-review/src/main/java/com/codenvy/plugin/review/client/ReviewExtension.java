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
package com.codenvy.plugin.review.client;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.plugin.contribution.shared.client.SharedConstants;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.google.web.bindery.event.shared.EventBus;

@Singleton
@Extension(title = "Review", version = "1.0.0")
public class ReviewExtension implements ProjectActionHandler {
    private final ReviewMessages      messages;
    private final VcsHostingService   vcsHostingService;
    private final VcsServiceProvider  vcsServiceProvider;

    @Inject
    public ReviewExtension(@Nonnull final EventBus eventBus,
                           @Nonnull final ReviewMessages messages,
                           @Nonnull final ReviewResources resources,
                           @Nonnull final VcsHostingService vcsHostingService,
                           @Nonnull final VcsServiceProvider vcsServiceProvider) {
        this.messages = messages;
        this.vcsHostingService = vcsHostingService;
        this.vcsServiceProvider = vcsServiceProvider;

        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    @Override
    public void onProjectOpened(final ProjectActionEvent event) {
        initializeReviewExtension(event.getProject());
    }

    @Override
    public void onProjectClosed(final ProjectActionEvent event) {
    }

    private void initializeReviewExtension(final ProjectDescriptor project) {
        final VcsService vcsService = this.vcsServiceProvider.getVcsService(project);
        final List<String> projectPermissions = project.getPermissions();
        final List<String> reviewAttr = project.getAttributes().get(SharedConstants.ATTRIBUTE_REVIEW_KEY);
        final List<String> pullRequestIdAttr = project.getAttributes().get(SharedConstants.ATTRIBUTE_REVIEW_PULLREQUEST_ID);

        if (vcsService != null
                && projectPermissions != null && projectPermissions.contains("write")
                && !(reviewAttr.isEmpty())
                && !(pullRequestIdAttr.isEmpty())) {
            final String reviewValue = reviewAttr.get(0);
            final String pullRequestId = pullRequestIdAttr.get(0);
        }
    }
}
