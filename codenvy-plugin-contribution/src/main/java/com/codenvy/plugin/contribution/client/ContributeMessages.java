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

    @DefaultMessage("Configure Contribution")
    String preContributeWizardTitle();

    @DefaultMessage("Choose a branch name for your contribution...")
    String branchNameInputPlaceHolder();

    @DefaultMessage("Type a comment text for the pull request...")
    String pullRequestCommentPlaceHolder();

    @DefaultMessage("Contribute")
    String contributeButton();

    @DefaultMessage("Cancel")
    String cancelButton();

    @Key("contributor.button.name")
    String contributorButtonName();

    @Key("contributor.button.description")
    String contributorButtonDescription();

    @DefaultMessage("Codenvy needs authorization on github to continue")
    String repositoryHostAuthorizeMessage();

    @DefaultMessage("You need to provide the contribution branch name")
    String warnBranchEmpty();

    @DefaultMessage("Incorrect or missing information")
    String warnMissingConfigTitle();

    @DefaultMessage("Could not list local branches. Contribution interrupted")
    String errorListBranches();

    @DefaultMessage("A branch with this name already exists: {0}")
    String errorBranchExists(String name);

    @DefaultMessage("Branch rename failed. Contribution interrupted")
    String errorRenameFailed();

    @DefaultMessage("Adding fork remote failed. Contribution interrupted")
    String errorAddRemoteFailed();

    @DefaultMessage("Contribution branch renamed to {0}")
    String infoRenamedBranch(String branchName);

    /*
     * Commit dialog
     */
    @Key("commit.dialog.title")
    String commitDialogTitle();

    @Key("commit.dialog.message")
    String commitDialogMessage();

    @Key("commit.dialog.description.title")
    String commitDialogDescriptionTitle();

    @Key("commit.dialog.button.ok.text")
    String commitDialogButtonOkText();

    @Key("commit.dialog.button.continue.text")
    String commitDialogButtonContinueText();

    /*
     * messages for check/create remote user fork step
     */
    public final static String CONTRIB_ISSUE_PR_PREFIX = "Issue PR: ";

    @DefaultMessage(CONTRIB_ISSUE_PR_PREFIX + "Using existing user''s fork")
    String useExistingUserFork();

    @DefaultMessage(CONTRIB_ISSUE_PR_PREFIX + "Creating a fork the repository`{0}/{1}`")
    String creatingFork(String username, String repository);

    @DefaultMessage(CONTRIB_ISSUE_PR_PREFIX + "Requested creation of a fork of the repository `{0}/{1}`")
    String requestedForkCreation(String repositoryOwner, String repositoryName);

    @DefaultMessage(CONTRIB_ISSUE_PR_PREFIX + "Failed creating the fork of the repository`{0}/{1}`. {2}")
    String failedCreatingUserFork(String repositoryOwner, String repositoryName, String message);

    @DefaultMessage(CONTRIB_ISSUE_PR_PREFIX + "Successfully pushed contribution branch to fork")
    String successPushingBranchToFork();

    @DefaultMessage(CONTRIB_ISSUE_PR_PREFIX + "Pushing contribution branch to fork")
    String pushingWorkingBranchToFork();

    @DefaultMessage(CONTRIB_ISSUE_PR_PREFIX + "Failed pushing contribution branch to fork: {0}")
    String failedPushingBranchToFork(String cause);

    @DefaultMessage("You can review this pull request by following this link: {0}")
    String pullRequestlinkComment(String factoryUrl);

    @DefaultMessage("Could not post review factory link in pull request comments: {0}")
    String warnPostFactoryLinkFailed(String factoryUrl);

    @DefaultMessage("Could not create review factory link")
    String warnCreateFactoryFailed();

    @DefaultMessage("Remote with name {0} already exists.")
        // period because that's a log, not a notification
    String forekRemoteAlreadyPresent(String forkRemoteName);

    @DefaultMessage("Failed to set the forked repository remote. Contribution interrupted")
    String errorRemoveRemoteFailed();

    @DefaultMessage("Could not check the remotes.")
    String warnCheckRemote();

    @DefaultMessage("Creation of the pull request failed. Contribution is interrupted")
    String errorPullRequestFailed();
}
