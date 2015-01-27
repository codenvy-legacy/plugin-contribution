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

import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.steps.events.StepEvent;
import com.codenvy.plugin.contribution.client.steps.events.StepHandler;
import com.codenvy.plugin.contribution.client.steps.events.WorkflowModeEvent;
import com.codenvy.plugin.contribution.client.steps.events.WorkflowModeHandler;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.Branch;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.ide.api.constraints.Constraints.LAST;
import static com.codenvy.ide.api.parts.PartStackType.TOOLING;
import static com.codenvy.plugin.contribution.client.steps.events.WorkflowModeEvent.Mode.UPDATE;

/**
 * Part for the contribution configuration.
 *
 * @author Kevin Pollet
 */
public class ContributePartPresenter extends BasePresenter
        implements ContributePartView.ActionDelegate, StepHandler, WorkflowModeHandler {
    /** The component view. */
    private final ContributePartView view;

    /** The workspace agent. */
    private final WorkspaceAgent workspaceAgent;

    /** The contribute plugin messages. */
    private final ContributeMessages messages;

    /** The contributor workflow controller. */
    private final Provider<ContributorWorkflow> workflow;

    /** The VCS hosting service. */
    private final VcsHostingService vcsHostingService;

    /** The step to authorize Codenvy on GitHub. */
    private final Provider<CommitWorkingTreeStep> commitWorkingTreeStep;

    /** The application context. */
    private final AppContext appContext;

    /** The vcs service. */
    private final VcsService vcsService;

    /** The notification helper. */
    private final NotificationHelper notificationHelper;

    @Inject
    public ContributePartPresenter(@Nonnull final ContributePartView view,
                                   @Nonnull final ContributeMessages messages,
                                   @Nonnull final WorkspaceAgent workspaceAgent,
                                   @Nonnull final EventBus eventBus,
                                   @Nonnull final Provider<ContributorWorkflow> workflow,
                                   @Nonnull final VcsHostingService vcsHostingService,
                                   @Nonnull final Provider<CommitWorkingTreeStep> commitWorkingTreeStep,
                                   @Nonnull final AppContext appContext,
                                   @Nonnull final VcsService vcsService,
                                   @Nonnull final NotificationHelper notificationHelper) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.workflow = workflow;
        this.vcsHostingService = vcsHostingService;
        this.messages = messages;
        this.commitWorkingTreeStep = commitWorkingTreeStep;
        this.appContext = appContext;
        this.vcsService = vcsService;
        this.notificationHelper = notificationHelper;

        this.view.setDelegate(this);
        eventBus.addHandler(StepEvent.TYPE, this);
        eventBus.addHandler(WorkflowModeEvent.TYPE, this);
    }

    public void open() {
        final Context context = workflow.get().getContext();
        vcsService.listLocalBranches(context.getProject(), new AsyncCallback<List<Branch>>() {
            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showError(ContributePartPresenter.class, exception);
            }

            @Override
            public void onSuccess(final List<Branch> branches) {
                final List<String> branchNames = new ArrayList<>();
                for (final Branch oneBranch : branches) {
                    branchNames.add(oneBranch.getDisplayName());
                }

                view.reset();
                view.setContributionBranchName(workflow.get().getContext().getWorkBranchName());
                view.setContributionBranchNameSuggestionList(branchNames);
                workspaceAgent.openPart(ContributePartPresenter.this, TOOLING, LAST);
            }
        });
    }

    public void remove() {
        workspaceAgent.removePart(ContributePartPresenter.this);
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
        view.clearStatusSection();
        view.setContributeEnabled(false);
        view.setContributionProgressState(true);

        // resume the contribution workflow and execute the commit tree step
        final ContributorWorkflow workflow = this.workflow.get();
        workflow.getConfiguration()
                .withContributionBranchName(view.getContributionBranchName())
                .withContributionComment(view.getContributionComment())
                .withContributionTitle(view.getContributionTitle());

        workflow.setStep(commitWorkingTreeStep.get());
        workflow.executeStep();
    }


    @Override
    public void onOpenOnRepositoryHost() {
        final Context context = workflow.get().getContext();

        Window.open(vcsHostingService.makePullRequestUrl(context.getOriginRepositoryOwner(), context.getOriginRepositoryName(),
                                                         context.getPullRequestIssueNumber()), "", "");
    }

    @Override
    public void onNewContribution() {
        view.hideStatusSection();
        view.clearStatusSection();
        view.hideNewContributionSection();

        final Factory factory = appContext.getFactory();
        if (factory != null) {
            String factoryUrl = null;
            final String createProject = "create-project";
            for (final Link link : factory.getLinks()) {
                if (createProject.equals(link.getRel())) {
                    factoryUrl = link.getHref();
                    break;
                }
            }

            if (factoryUrl != null) {
                Window.open(factoryUrl, "", "");
            }
        }
    }

    @Override
    public void updateControls() {
        final String branchName = view.getContributionBranchName();
        final String contributionTitle = view.getContributionTitle();

        boolean ready = true;
        view.showContributionBranchNameError(false);
        view.showContributionTitleError(false);

        if (branchName == null || !branchName.matches("[0-9A-Za-z-]+")) {
            view.showContributionBranchNameError(true);
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
    public void onStepDone(@Nonnull final StepEvent event) {
        switch (event.getStep()) {
            case AUTHORIZE_CODENVY_ON_VCS_HOST: {
                view.showStatusSection();
            }
            break;

            case CREATE_FORK: {
                view.setCreateForkStatus(true);
            }
            break;

            case PUSH_BRANCH_ON_FORK: {
                view.setPushBranchStatus(true);
            }
            break;

            case ISSUE_PULL_REQUEST: {
                view.setIssuePullRequestStatus(true);
                view.setContributeEnabled(true);
                view.setContributionProgressState(false);
                view.showStatusSectionFooter();
                view.showNewContributionSection();
            }
            break;
        }
    }

    @Override
    public void onStepError(@Nonnull final StepEvent event) {
        switch (event.getStep()) {
            case CREATE_FORK: {
                view.setCreateForkStatus(false);
            }
            break;

            case PUSH_BRANCH_ON_FORK: {
                view.setPushBranchStatus(false);
            }
            break;

            case ISSUE_PULL_REQUEST: {
                view.setIssuePullRequestStatus(false);
            }
            break;
        }

        view.setContributeEnabled(true);
        view.setContributionProgressState(false);
    }

    @Override
    public void onWorkflowModeChange(@Nonnull final WorkflowModeEvent event) {
        view.setContributionBranchNameEnabled(event.getMode() != UPDATE);
        view.setContributionTitleEnabled(event.getMode() != UPDATE);
        view.setContributionCommentEnabled(event.getMode() != UPDATE);
        view.setContributeButtonText(
                event.getMode() == UPDATE ? messages.contributePartConfigureContributionSectionButtonContributeUpdateText()
                                          : messages.contributePartConfigureContributionSectionButtonContributeText());
    }
}
