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

import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentUser;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.util.Config;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter;
import com.codenvy.plugin.contribution.client.steps.ForkCreationStep;
import com.codenvy.plugin.contribution.client.steps.RenameWorkBranchStep;
import com.codenvy.plugin.contribution.client.steps.event.StepDoneEvent;
import com.codenvy.plugin.contribution.client.steps.event.StepDoneHandler;
import com.codenvy.plugin.contribution.client.steps.event.UpdateModeEvent;
import com.codenvy.plugin.contribution.client.steps.event.UpdateModeHandler;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.HostUser;
import com.codenvy.security.oauth.JsOAuthWindow;
import com.codenvy.security.oauth.OAuthCallback;
import com.codenvy.security.oauth.OAuthStatus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.codenvy.ide.api.constraints.Constraints.FIRST;
import static com.codenvy.ide.api.parts.PartStackType.TOOLING;

/**
 * Part for the contribution configuration.
 */
public class ContributePartPresenter extends BasePresenter
        implements ContributePartView.ActionDelegate, CommitPresenter.CommitActionHandler, StepDoneHandler, UpdateModeHandler {
    /** The component view. */
    private final ContributePartView view;

    /** The workspace agent. */
    private final WorkspaceAgent workspaceAgent;

    /** The contribution configuration, which contains values chosen by the user. */
    private final Configuration configuration;

    /** The contribution context which contains project, work branch etc. */
    private final Context context;

    /** The contribute plugin messages. */
    private final ContributeMessages messages;

    /** The rename branch step. */
    private final RenameWorkBranchStep renameWorkBranchStep;

    /** The notification helper. */
    private final NotificationHelper notificationHelper;

    /** The application context. */
    private final AppContext appContext;

    /** The commit dialog presenter. */
    private final CommitPresenter commitPresenter;

    /** The repository host. */
    private final VcsHostingService vcsHostingService;

    /** The fork creation step. */
    private final ForkCreationStep forkCreationStep;

    /** The rest context base url. */
    private final String baseUrl;

    @Inject
    public ContributePartPresenter(@Nonnull final ContributePartView view,
                                   @Nonnull final Context context,
                                   @Nonnull final ContributeMessages messages,
                                   @Nonnull final WorkspaceAgent workspaceAgent,
                                   @Nonnull final RenameWorkBranchStep renameWorkBranchStep,
                                   @Nonnull final DtoFactory dtoFactory,
                                   @Nonnull final NotificationHelper notificationHelper,
                                   @Nonnull final AppContext appContext,
                                   @Nonnull final CommitPresenter commitPresenter,
                                   @Nonnull final VcsHostingService vcsHostingService,
                                   @Nonnull final ForkCreationStep forkCreationStep,
                                   @Nonnull @Named("restContext") final String baseUrl,
                                   @Nonnull final EventBus eventBus) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.notificationHelper = notificationHelper;
        this.appContext = appContext;
        this.commitPresenter = commitPresenter;
        this.vcsHostingService = vcsHostingService;
        this.forkCreationStep = forkCreationStep;
        this.baseUrl = baseUrl;
        this.configuration = dtoFactory.createDto(Configuration.class);
        this.context = context;
        this.messages = messages;
        this.renameWorkBranchStep = renameWorkBranchStep;

        this.view.setDelegate(this);
        this.commitPresenter.setCommitActionHandler(this);
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

        if (!appContext.getCurrentUser().isUserPermanent()) {
            notificationHelper.showError(ContributePartPresenter.class, new IllegalStateException("Codenvy account is not permanent"));

        } else {
            commitPresenter.hasUncommittedChanges(new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.showError(ContributePartPresenter.class, exception);
                }

                @Override
                public void onSuccess(final Boolean hasUncommittedChanges) {
                    if (hasUncommittedChanges) {
                        commitPresenter.showView();

                    } else {
                        getVCSHostUserInfoWithAuthentication();
                    }
                }
            });
        }
    }

    @Override
    public void onOpenOnRepositoryHost() {
        Window.open(vcsHostingService.makePullRequestUrl(context.getOriginRepositoryOwner(), context.getOriginRepositoryName(),
                                                         context.getPullRequestIssueNumber()), "", "");
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

    @Override
    public void onCommitAction(final CommitPresenter.CommitActionHandler.CommitAction action) {
        getVCSHostUserInfoWithAuthentication();
    }

    /**
     * Authenticates the user on the VCS Host.
     */
    private void authenticateOnVCSHost() {
        final CurrentUser currentUser = appContext.getCurrentUser();
        final String authUrl = baseUrl
                               + "/oauth/authenticate?oauth_provider=github&userId=" + currentUser.getProfile().getId()
                               + "&scope=user,repo,write:public_key&redirect_after_login="
                               + Window.Location.getProtocol() + "//"
                               + Window.Location.getHost() + "/ws/"
                               + Config.getWorkspaceName();

        new JsOAuthWindow(authUrl, "error.url", 500, 980, new OAuthCallback() {
            @Override
            public void onAuthenticated(final OAuthStatus authStatus) {
                // maybe it's possible to avoid this request if authStatus contains the vcs host user.
                vcsHostingService.getUserInfo(new AsyncCallback<HostUser>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        final String exceptionMessage = exception.getMessage();
                        if (exceptionMessage != null && exceptionMessage.contains("Bad credentials")) {
                            notificationHelper.showError(ContributePartPresenter.class, messages.contributePartErrorCannotAccessVCSHost());

                        } else {
                            notificationHelper.showError(ContributePartPresenter.class, exception);
                        }
                    }

                    @Override
                    public void onSuccess(final HostUser user) {
                        onVCSHostUserAuthenticated(user);
                    }
                });
            }

        }).loginWithOAuth();
    }

    /**
     * Retrieves the VCS host user info. If the user is not authenticated on the VCS host an authentication is performed.
     */
    private void getVCSHostUserInfoWithAuthentication() {
        vcsHostingService.getUserInfo(new AsyncCallback<HostUser>() {
            @Override
            public void onFailure(final Throwable exception) {
                final String exceptionMessage = exception.getMessage();
                if (exceptionMessage != null && exceptionMessage.contains("Bad credentials")) {
                    authenticateOnVCSHost();

                } else {
                    notificationHelper.showError(ContributePartPresenter.class, exception);
                }
            }

            @Override
            public void onSuccess(final HostUser user) {
                onVCSHostUserAuthenticated(user);
            }
        });
    }

    /**
     * Checks that the user authenticated on the VCS Host is authenticated on Codenvy.
     *
     * @param user
     *         the user authenticated on the VCS Host.
     */
    private void onVCSHostUserAuthenticated(final HostUser user) {
        view.showStatusSection();

        context.setHostUserLogin(user.getLogin());

        configuration.withBranchName(view.getBranchName())
                     .withPullRequestComment(view.getContributionComment())
                     .withContributionTitle(view.getContributionTitle());

        forkCreationStep.execute(context, configuration); // parallel with the other steps
        renameWorkBranchStep.execute(context, configuration);
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
    public void onUpdateModeChange(UpdateModeEvent event) {
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
