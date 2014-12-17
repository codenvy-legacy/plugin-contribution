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

    @DefaultMessage("You need to provide the contribution branch name.")
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
}
