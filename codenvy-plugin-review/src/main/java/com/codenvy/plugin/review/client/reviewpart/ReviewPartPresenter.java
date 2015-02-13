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
package com.codenvy.plugin.review.client.reviewpart;

import static com.codenvy.ide.api.constraints.Constraints.LAST;
import static com.codenvy.ide.api.parts.PartStackType.TOOLING;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.review.client.ReviewMessages;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Part for the contribution configuration.
 *
 * @author Kevin Pollet
 */
public class ReviewPartPresenter extends BasePresenter implements ReviewPartView.ActionDelegate {
    private final ReviewPartView view;
    private final WorkspaceAgent workspaceAgent;
    private final ReviewMessages messages;
    private final VcsHostingService vcsHostingService;
    private final AppContext appContext;
    private final VcsServiceProvider vcsServiceProvider;
    private final DialogFactory dialogFactory;

    @Inject
    public ReviewPartPresenter(@Nonnull final ReviewPartView view,
                               @Nonnull final ReviewMessages messages,
                               @Nonnull final WorkspaceAgent workspaceAgent,
                               @Nonnull final EventBus eventBus,
                               @Nonnull final VcsHostingService vcsHostingService,
                               @Nonnull final AppContext appContext,
                               @Nonnull final VcsServiceProvider vcsServiceProvider,
                               @Nonnull final DialogFactory dialogFactory) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.vcsHostingService = vcsHostingService;
        this.messages = messages;
        this.appContext = appContext;
        this.vcsServiceProvider = vcsServiceProvider;
        this.dialogFactory = dialogFactory;

        this.view.setDelegate(this);
    }

    public void open() {
        workspaceAgent.openPart(ReviewPartPresenter.this, TOOLING, LAST);
    }

    public void remove() {
        workspaceAgent.removePart(ReviewPartPresenter.this);
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(view.asWidget());
    }

    @Override
    public String getTitle() {
        return messages.reviewPartTitle();
    }

    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    @Override
    public String getTitleToolTip() {
        return null;
    }

    @Override
    public int getSize() {
        return 350;
    }
}
