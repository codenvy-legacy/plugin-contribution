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

import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.steps.CommitWorkingTreeStep;
import com.codenvy.plugin.contribution.client.steps.Context;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.steps.Step;
import com.codenvy.plugin.contribution.client.steps.events.ContextPropertyChangeEvent;
import com.codenvy.plugin.contribution.client.steps.events.ContextPropertyChangeHandler;
import com.codenvy.plugin.contribution.client.steps.events.StepEvent;
import com.codenvy.plugin.contribution.client.steps.events.StepHandler;
import com.codenvy.plugin.contribution.client.utils.FactoryHelper;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
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
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.ide.api.constraints.Constraints.LAST;
import static com.codenvy.ide.api.parts.PartStackType.TOOLING;

/**
 * Part for the contribution configuration.
 *
 * @author Kevin Pollet
 */
public class ContributePartPresenter extends BasePresenter
        implements ContributePartView.ActionDelegate, StepHandler, ContextPropertyChangeHandler {
    /** The component view. */
    private final ContributePartView view;

    /** The workspace agent. */
    private final WorkspaceAgent workspaceAgent;

    /** The contribute plugin messages. */
    private final ContributeMessages messages;

    /** The contributor workflow controller. */
    private final ContributorWorkflow workflow;

    /** The VCS hosting service. */
    private final VcsHostingService vcsHostingService;

    /** The step to authorize Codenvy on GitHub. */
    private final Step commitWorkingTreeStep;

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
                                   @Nonnull final ContributorWorkflow workflow,
                                   @Nonnull final VcsHostingService vcsHostingService,
                                   @Nonnull final CommitWorkingTreeStep commitWorkingTreeStep,
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
        eventBus.addHandler(ContextPropertyChangeEvent.TYPE, this);
    }

    public void open() {
        view.reset();
        workspaceAgent.openPart(ContributePartPresenter.this, TOOLING, LAST);
    }

    public void remove() {
        workspaceAgent.removePart(ContributePartPresenter.this);
    }

    @Override
    public void onContribute() {
        view.hideStatusSection();
        view.clearStatusSection();
        view.setContributeEnabled(false);
        view.setContributionProgressState(true);

        // resume the contribution workflow and execute the commit tree step
        workflow.getConfiguration()
                .withContributionBranchName(view.getContributionBranchName())
                .withContributionComment(view.getContributionComment())
                .withContributionTitle(view.getContributionTitle());

        workflow.setStep(commitWorkingTreeStep);
        workflow.executeStep();
    }


    @Override
    public void onOpenOnRepositoryHost() {
        final Context context = workflow.getContext();

        Window.open(vcsHostingService.makePullRequestUrl(context.getUpstreamRepositoryOwner(), context.getUpstreamRepositoryName(),
                                                         context.getPullRequestIssueNumber()), "", "");
    }

    @Override
    public void onNewContribution() {
        view.hideStatusSection();
        view.clearStatusSection();
        view.hideNewContributionSection();

        final Factory factory = appContext.getFactory();
        if (factory != null) {
            final String createProjectUrl = FactoryHelper.getCreateProjectRelUrl(factory);

            if (createProjectUrl != null) {
                Window.open(createProjectUrl, "", "");
            }
        }
    }

    @Override
    public void onRefreshContributionBranchNameList() {
        final Context context = workflow.getContext();
        getLocalBranchNamesList(context.getProject(), new AsyncCallback<List<String>>() {
            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showError(ContributePartPresenter.class, exception);
            }

            @Override
            public void onSuccess(final List<String> branchNames) {
                view.setContributionBranchNameSuggestionList(branchNames);
            }
        });
    }

    @Override
    public void updateControls() {
        final String branchName = view.getContributionBranchName();
        final String contributionTitle = view.getContributionTitle();

        boolean isValid = true;
        view.showContributionBranchNameError(false);
        view.showContributionTitleError(false);

        if (branchName == null || !branchName.matches("[0-9A-Za-z-]+")) {
            view.showContributionBranchNameError(true);
            isValid = false;
        }

        if (contributionTitle == null || contributionTitle.trim().isEmpty()) {
            view.showContributionTitleError(true);
            isValid = false;
        }

        view.setContributeEnabled(isValid);
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
            case COMMIT_WORKING_TREE: {
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
                view.setContributionBranchNameEnabled(false);
                view.setContributionTitleEnabled(false);
                view.setContributionCommentEnabled(false);
                view.setContributeButtonText(messages.contributePartConfigureContributionSectionButtonContributeUpdateText());
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
    public void onContextPropertyChange(final ContextPropertyChangeEvent event) {
        final Context context = event.getContext();

        switch (event.getContextProperty()) {
            case CLONED_BRANCH_NAME: {
                view.setClonedBranch(context.getClonedBranchName());
            }
            break;

            case WORK_BRANCH_NAME: {
                view.setContributionBranchName(context.getWorkBranchName());
            }
            break;

            case ORIGIN_REPOSITORY_NAME:
            case ORIGIN_REPOSITORY_OWNER: {
                final String originRepositoryName = context.getOriginRepositoryName();
                final String originRepositoryOwner = context.getOriginRepositoryOwner();

                if (originRepositoryName != null && originRepositoryOwner != null) {
                    view.setRepositoryUrl(vcsHostingService.makeHttpRemoteUrl(originRepositoryOwner, originRepositoryName));
                }
            }
            break;

            case PROJECT: {
                getLocalBranchNamesList(context.getProject(), new AsyncCallback<List<String>>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        notificationHelper.showError(ContributePartPresenter.class, exception);
                    }

                    @Override
                    public void onSuccess(final List<String> branchNames) {
                        view.setContributionBranchNameSuggestionList(branchNames);
                    }
                });
            }
            break;
        }

        updateControls();
    }

    private void getLocalBranchNamesList(final ProjectDescriptor project, final AsyncCallback<List<String>> callback) {
        vcsService.listLocalBranches(project, new AsyncCallback<List<Branch>>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final List<Branch> branches) {
                final List<String> branchNames = new ArrayList<>();
                for (final Branch oneBranch : branches) {
                    branchNames.add(oneBranch.getDisplayName());
                }

                callback.onSuccess(branchNames);
            }
        });
    }
}
