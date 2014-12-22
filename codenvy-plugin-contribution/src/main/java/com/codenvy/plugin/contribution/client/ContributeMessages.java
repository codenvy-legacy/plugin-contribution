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
     * Authentication dialog
     */
    @Key("authentication.dialog.title")
    String authenticationDialogTitle();

    @Key("authentication.dialog.message")
    String authenticationDialogMessage();

    @Key("authentication.dialog.login.button.text")
    String authenticationDialogLoginButtonText();

    @Key("authentication.dialog.create.account.button.text")
    String authenticationDialogCreateAccountButtonText();

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
}
