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
package com.codenvy.plugin.contribution.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Internationalizable messages for the contributor plugin.
 */
public interface ContributeMessages extends Messages {
    /*
     * Contribute part
     */
    @Key("contribute.part.title")
    String contributePartTitle();

    @Key("contribute.part.error.cannot.access.vcs.host")
    String contributePartErrorCannotAccessVCSHost();

    @Key("contribute.part.project.information.section.title")
    String contributePartProjectInformationSectionTitle();

    @Key("contribute.part.project.information.section.repository.url.label")
    String contributePartProjectInformationSectionRepositoryUrlLabel();

    @Key("contribute.part.project.information.section.repository.cloned.branch.label")
    String contributePartProjectInformationSectionClonedBranchLabel();

    @Key("contribute.part.configure.contribution.section.title")
    String contributePartConfigureContributionSectionTitle();

    @Key("contribute.part.configure.contribution.section.branch.name.label")
    String contributePartConfigureContributionSectionBranchNameLabel();

    @Key("contribute.part.configure.contribution.section.branch.name.placeholder")
    String contributePartConfigureContributionSectionBranchNamePlaceholder();

    @Key("contribute.part.configure.contribution.section.contribution.title.label")
    String contributePartConfigureContributionSectionContributionTitleLabel();

    @Key("contribute.part.configure.contribution.section.contribution.title.placeholder")
    String contributePartConfigureContributionSectionContributionTitlePlaceholder();

    @Key("contribute.part.configure.contribution.section.contribution.comment.label")
    String contributePartConfigureContributionSectionContributionCommentLabel();

    @Key("contribute.part.configure.contribution.section.contribution.comment.placeholder")
    String contributePartConfigureContributionSectionContributionCommentPlaceholder();

    @Key("contribute.part.configure.contribution.section.button.contribute.text")
    String contributePartConfigureContributionSectionButtonContributeText();

    @Key("contribute.part.status.section.title")
    String contributePartStatusSectionTitle();

    @Key("contribute.part.status.section.fork.created.label")
    String contributePartStatusSectionForkCreatedLabel();

    @Key("contribute.part.status.section.branch.pushed.label")
    String contributePartStatusSectionBranchPushedLabel();

    @Key("contribute.part.status.section.pull.request.issued.label")
    String contributePartStatusSectionPullRequestIssuedLabel();

    @Key("contribute.part.status.section.contribution.issued.label")
    String contributePartStatusSectionContributionIssuedLabel();

    @Key("contribute.part.status.section.button.open.on.github.text")
    String contributePartStatusSectionButtonOpenOnGithubText();

    /*
     * Commit dialog
     */
    @Key("commit.dialog.title")
    String commitDialogTitle();

    @Key("commit.dialog.message")
    String commitDialogMessage();

    @Key("commit.dialog.checkbox.include.untracked.text")
    String commitDialogCheckBoxIncludeUntracked();

    @Key("commit.dialog.description.title")
    String commitDialogDescriptionTitle();

    @Key("commit.dialog.button.ok.text")
    String commitDialogButtonOkText();

    @Key("commit.dialog.button.continue.text")
    String commitDialogButtonContinueText();

    @Key("commit.dialog.button.cancel.text")
    String commitDialogButtonCancelText();

    /*
     * Notification message prefix.
     */
    @Key("notification.message.prefix")
    String notificationMessagePrefix(String notificationMessage);

    /*
     * Rename work branch step
     */
    @Key("step.rename.work.branch.local.branch.renamed")
    String stepRenameWorkBranchLocalBranchRenamed(String branchName);

    @Key("step.rename.work.branch.missing.config.title")
    String stepRenameWorkBranchMissingConfigTitle();

    @Key("step.rename.work.branch.error.list.local.branches")
    String stepRenameWorkBranchErrorListLocalBranches();

    @Key("step.rename.work.branch.error.local.branch.exists")
    String stepRenameWorkBranchErrorLocalBranchExists(String branchName);

    @Key("step.rename.work.branch.error.rename.local.branch")
    String stepRenameWorkBranchErrorRenameLocalBranch();

    /*
     * Add fork remote step
     */
    @Key("step.add.fork.remote.error.add.fork")
    String stepAddForkRemoteErrorAddFork();

    @Key("step.add.fork.remote.error.set.forked.repository.remote")
    String stepAddForkRemoteErrorSetForkedRepositoryRemote();

    @Key("step.add.fork.remote.error.check.remotes")
    String stepAddForkRemoteErrorCheckRemote();

    /*
     * Fork creation step
     */
    @Key("step.fork.creation.use.existing.fork")
    String stepForkCreationUseExistingFork();

    @Key("step.fork.creation.create.fork")
    String stepForkCreationCreateFork(String username, String repository);

    @Key("step.fork.creation.request.fork.creation")
    String stepForkCreationRequestForkCreation(String owner, String repository);

    @Key("step.fork.creation.error.creating.fork")
    String stepForkCreationErrorCreatingFork(String owner, String repository, String message);

    /*
     * Push branch fork step
     */
    @Key("step.push.branch.pushing.branch")
    String stepPushBranchPushingBranch();

    @Key("step.push.branch.error.pushing.branch")
    String stepPushBranchErrorPushingBranch(String cause);

    @Key("step.push.branch.branch.pushed")
    String stepPushBranchBranchPushed();

    /*
     * Add review factory link step
     */
    @Key("step.add.review.factory.link.pull.request.comment")
    String stepAddReviewFactoryLinkPullRequestComment(String factoryUrl);

    @Key("step.add.review.factory.link.error.posting.factory.link")
    String stepAddReviewFactoryLinkErrorPostingFactoryLink(String factoryUrl);

    /*
     * Generate review factory step
     */
    @Key("step.generate.review.factory.error.create.factory")
    String stepGenerateReviewFactoryErrorCreateFactory();

    /*
     * Issue pull request step
     */
    @Key("step.issue.pull.request.pull.request.created")
    String stepIssuePullRequestPullRequestCreated(String url);

    @Key("step.issue.pull.request.issuing.pull.request")
    String stepIssuePullRequestIssuingPullRequest();

    @Key("step.issue.pull.request.existing.pull.request.updated")
    String stepIssuePullRequestExistingPullRequestUpdated(String headBranch);

    @Key("step.issue.pull.request.error.create.pull.request")
    String stepIssuePullRequestErrorCreatePullRequest();

    /*
     * Contributor extension
     */
    @Key("contributor.extension.creating.work.branch")
    String contributorExtensionCreatingWorkBranch(String branchName);

    @Key("contributor.extension.work.branch.created")
    String contributorExtensionWorkBranchCreated(String branchName);

    @Key("contributor.extension.error.updating.contribution.attributes")
    String contributorExtensionErrorUpdatingContributionAttributes(String exceptionMessage);
}
