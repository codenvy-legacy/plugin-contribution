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
}
