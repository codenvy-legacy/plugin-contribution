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
package com.codenvy.plugin.review.client;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.google.web.bindery.event.shared.EventBus;

@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ReviewExtension implements ProjectActionHandler {
    private final ReviewMessages messages;


    @Inject
    public ReviewExtension(@Nonnull final EventBus eventBus,
                           @Nonnull final ReviewMessages messages,
                           @Nonnull final ReviewResources resources) {
        this.messages = messages;

    }

    @Override
    public void onProjectOpened(final ProjectActionEvent event) {
        initializeReviewExtension(event.getProject());
    }

    @Override
    public void onProjectClosed(final ProjectActionEvent event) {
    }

    private void initializeReviewExtension(final ProjectDescriptor project) {
    }
}
