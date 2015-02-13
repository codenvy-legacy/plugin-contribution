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
package com.codenvy.plugin.contribution.client.steps;


import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.vcs.hosting.NoCommitsInPullRequestException;
import com.codenvy.plugin.contribution.client.vcs.hosting.PullRequestAlreadyExistsException;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.client.vcs.hosting.dto.PullRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.ISSUE_PULL_REQUEST;

/**
 * Create the pull request on the remote VCS repository.
 */
public class IssuePullRequestStep implements Step {
    private static final String DEFAULT_BASE_BRANCH = "master";

    private final VcsHostingService  vcsHostingService;
    private final ContributeMessages messages;

    @Inject
    public IssuePullRequestStep(@Nonnull final VcsHostingService vcsHostingService,
                                @Nonnull final ContributeMessages messages) {
        this.vcsHostingService = vcsHostingService;
        this.messages = messages;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final Configuration configuration = workflow.getConfiguration();
        final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
        final String upstreamRepositoryName = context.getUpstreamRepositoryName();
        final String contributionTitle = configuration.getContributionTitle();
        final String baseBranch = context.getClonedBranchName() != null ? context.getClonedBranchName() : DEFAULT_BASE_BRANCH;
        final String headBranch = context.getHostUserLogin() + ":" + context.getWorkBranchName();
        final String contributionComment = configuration.getContributionComment();

        vcsHostingService.createPullRequest(upstreamRepositoryOwner, upstreamRepositoryName, contributionTitle, headBranch, baseBranch,
                                            contributionComment, new AsyncCallback<PullRequest>() {
                    @Override
                    public void onSuccess(final PullRequest pullRequest) {
                        context.setPullRequestIssueNumber(pullRequest.getNumber());
                        workflow.fireStepDoneEvent(ISSUE_PULL_REQUEST);
                    }

                    @Override
                    public void onFailure(final Throwable exception) {
                        if (exception instanceof PullRequestAlreadyExistsException) {
                            vcsHostingService.getPullRequest(upstreamRepositoryOwner, upstreamRepositoryName, headBranch,
                                                             new AsyncCallback<PullRequest>() {
                                                                 @Override
                                                                 public void onSuccess(final PullRequest pullRequest) {
                                                                     context.setPullRequestIssueNumber(pullRequest.getNumber());
                                                                     workflow.fireStepDoneEvent(ISSUE_PULL_REQUEST);
                                                                 }

                                                                 @Override
                                                                 public void onFailure(final Throwable exception) {
                                                                     workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST,
                                                                                                 exception.getMessage());
                                                                 }
                                                             });

                        } else if (exception instanceof NoCommitsInPullRequestException) {
                            workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST,
                                                        messages.stepIssuePullRequestErrorCreatePullRequestWithoutCommits());

                        } else {
                            workflow.fireStepErrorEvent(ISSUE_PULL_REQUEST, messages.stepIssuePullRequestErrorCreatePullRequest());
                        }
                    }
                });
    }
}
