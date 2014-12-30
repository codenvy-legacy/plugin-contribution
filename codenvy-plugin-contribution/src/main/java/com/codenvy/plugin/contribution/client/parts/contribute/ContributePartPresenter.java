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
package com.codenvy.plugin.contribution.client.parts.contribute;

import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.steps.RenameBranchStep;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.codenvy.ide.api.constraints.Constraints.FIRST;
import static com.codenvy.ide.api.parts.PartStackType.TOOLING;

/**
 * Part for the contribution configuration.
 */
public class ContributePartPresenter extends BasePresenter implements ContributePartView.ActionDelegate {
    /** The component view. */
    private final ContributePartView view;

    /** THe event bus. */
    private final EventBus eventBus;

    /** The workspace agent. */
    private final WorkspaceAgent workspaceAgent;

    /** The contribution configuration, which contains values chosen by the user. */
    private final Configuration configuration;

    /** The contribution context which contains project, work branch etc. */
    private final Context context;

    /** The contribute plugin messages. */
    private final ContributeMessages messages;

    /** The rename branch step. */
    private final RenameBranchStep renameBranchStep;

    /** The project action handler registration. */
    private HandlerRegistration projectActionHandler;

    @Inject
    public ContributePartPresenter(@Nonnull final ContributePartView view,
                                   @Nonnull final Context context,
                                   @Nonnull final ContributeMessages messages,
                                   @Nonnull final EventBus eventBus,
                                   @Nonnull final WorkspaceAgent workspaceAgent,
                                   @Nonnull final RenameBranchStep renameBranchStep,
                                   @Nonnull final DtoFactory dtoFactory) {
        this.view = view;
        this.eventBus = eventBus;
        this.workspaceAgent = workspaceAgent;
        this.configuration = dtoFactory.createDto(Configuration.class);
        this.context = context;
        this.messages = messages;
        this.renameBranchStep = renameBranchStep;

        this.view.setDelegate(this);
    }

    public void process() {
        projectActionHandler = eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                workspaceAgent.openPart(ContributePartPresenter.this, TOOLING, FIRST);
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
                workspaceAgent.removePart(ContributePartPresenter.this);
                projectActionHandler.removeHandler();
            }
        });
    }

    public void showContributePart() {
        view.reset();
        workspaceAgent.setActivePart(this);
    }

    @Override
    public void onContribute() {
        configuration.withBranchName(view.getBranchName())
                     .withPullRequestComment(view.getContributionComment())
                     .withContributionTitle(view.getContributionTitle());

        renameBranchStep.execute(context, configuration);
    }

    @Override
    public String suggestBranchName() {
        return context.getWorkBranchName();
    }

    @Override
    public void updateControls() {
        final String branchName = view.getBranchName();
        final String contributionTitle = view.getContributionTitle();

        boolean ready = true;
        view.showBranchNameError(false);
        view.showContributionTitleError(false);

        if (branchName == null || !branchName.matches("[0-9A-Za-z-]+")) {
            view.showBranchNameError(true);
            ready = false;
        }

        if (contributionTitle == null || contributionTitle.trim().isEmpty()) {
            view.showContributionTitleError(true);
            ready = false;
        }

        view.setContributeEnabled(ready);
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(view.asWidget());
    }

    @Nonnull
    @Override
    public String getTitle() {
        return messages.contributePartTitle();
    }

    @Nullable
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return null;
    }

    @Override
    public int getSize() {
        return 350;
    }
}
