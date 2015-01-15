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

import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.steps.events.StepDoneEvent;
import com.codenvy.plugin.contribution.client.steps.events.StepDoneHandler;
import com.codenvy.plugin.contribution.client.steps.events.UpdateModeEvent;
import com.codenvy.plugin.contribution.client.steps.events.UpdateModeHandler;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import static com.codenvy.ide.api.constraints.Constraints.FIRST;
import static com.codenvy.ide.api.parts.PartStackType.TOOLING;

/**
 * Part for the contribution configuration.
 *
 * @author Kevin Pollet
 */
public class ContributePartPresenter extends BasePresenter
        implements ContributePartView.ActionDelegate, StepDoneHandler, UpdateModeHandler {
    /** The component view. */
    private final ContributePartView view;

    /** The workspace agent. */
    private final WorkspaceAgent workspaceAgent;

    /** The contribute plugin messages. */
    private final ContributeMessages messages;

    /** The contributor workflow controller. */
    private final Provider<ContributorWorkflow> workflowProvider;

    /** The VCS hosting service. */
    private final VcsHostingService vcsHostingService;

    /** The step to authorize Codenvy on GitHub. */
    private final Provider<CommitWorkingTreeStep> commitWorkingTreeStep;

    @Inject
    public ContributePartPresenter(@Nonnull final ContributePartView view,
                                   @Nonnull final ContributeMessages messages,
                                   @Nonnull final WorkspaceAgent workspaceAgent,
                                   @Nonnull final EventBus eventBus,
                                   @Nonnull final Provider<ContributorWorkflow> workflowProvider,
                                   @Nonnull final VcsHostingService vcsHostingService,
                                   @Nonnull final Provider<CommitWorkingTreeStep> commitWorkingTreeStep) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.workflowProvider = workflowProvider;
        this.vcsHostingService = vcsHostingService;
        this.messages = messages;
        this.commitWorkingTreeStep = commitWorkingTreeStep;

        this.view.setDelegate(this);
        eventBus.addHandler(StepDoneEvent.TYPE, this);
        eventBus.addHandler(UpdateModeEvent.TYPE, this);
    }

    public void open() {
        workspaceAgent.openPart(ContributePartPresenter.this, TOOLING, FIRST);
    }

    public void remove() {
        workspaceAgent.removePart(ContributePartPresenter.this);
    }

    public void showContributePart() {
        view.reset();
        workspaceAgent.setActivePart(this);
    }

    public void showStatusSection() {
        view.showStatusSection();
    }

    public void setRepositoryUrl(String url) {
        view.setRepositoryUrl(url);
    }

    public void setClonedBranch(String branch) {
        view.setClonedBranch(branch);
    }

    @Override
    public void onContribute() {
        view.hideStatusSection();
        view.resetStatusSection();

        // resume the contribution workflowProvider and execute the current step
        final ContributorWorkflow workflow = workflowProvider.get();
        workflow.getConfiguration().setBranchName(view.getBranchName());
        workflow.getConfiguration().setContributionComment(view.getContributionComment());
        workflow.getConfiguration().setContributionTitle(view.getContributionTitle());
        workflow.setStep(commitWorkingTreeStep.get());
        workflow.executeStep();
    }


    @Override
    public void onOpenOnRepositoryHost() {
        final Context context = workflowProvider.get().getContext();

        Window.open(vcsHostingService.makePullRequestUrl(context.getOriginRepositoryOwner(), context.getOriginRepositoryName(),
                                                         context.getPullRequestIssueNumber()), "", "");
    }

    @Override
    public String suggestBranchName() {
        return workflowProvider.get().getContext().getWorkBranchName();
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

    @Override
    public void onStepDone(@Nonnull final StepDoneEvent event) {
        switch (event.getStep()) {
            case CREATE_FORK: {
                view.setCreateForkStatus(event.isSuccess());
            }
            break;

            case PUSH_BRANCH: {
                view.setPushBranchStatus(event.isSuccess());
            }
            break;

            case ISSUE_PULL_REQUEST: {
                view.setIssuePullRequestStatus(event.isSuccess());
                if (event.isSuccess()) {
                    view.showStatusSectionFooter();
                }
            }
            break;
        }
    }

    @Override
    public void onUpdateModeChange(@Nonnull final UpdateModeEvent event) {
        switch (event.getState()) {
            case START_UPDATE_MODE: {
                view.setBranchNameEnabled(false);
                view.setContributionTitleEnabled(false);
                view.setContributionCommentEnabled(false);
                view.setContributeButtonMessage(messages.contributePartConfigureContributionSectionButtonContributeUpdateText());
            }
            break;

            case STOP_UPDATE_MODE: {
                view.setBranchNameEnabled(true);
                view.setBranchNameFocus(true);
                view.setContributionTitleEnabled(true);
                view.setContributionCommentEnabled(true);
                view.setContributeButtonMessage(messages.contributePartConfigureContributionSectionButtonContributeText());
            }
            break;
        }
    }
}
