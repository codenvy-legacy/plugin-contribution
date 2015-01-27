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

import com.codenvy.ide.api.mvp.View;
import com.codenvy.ide.api.parts.base.BaseActionDelegate;

import java.util.List;

/**
 * Interface for the contribution configuration shown when the user decides to send their contribution.
 */
public interface ContributePartView extends View<ContributePartView.ActionDelegate> {
    /**
     * Resets the part to its initial state.
     */
    void reset();

    /**
     * Set factory's repository URL.
     */
    void setRepositoryUrl(String url);

    /**
     * Set factory's cloned branch name.
     */
    void setClonedBranch(String branch);

    /**
     * Returns the contribution branch name.
     *
     * @return the contribution branch name
     */
    String getContributionBranchName();

    /**
     * Sets the contribution branch name.
     *
     * @param branchName
     *         the contribution branch name.
     */
    void setContributionBranchName(String branchName);

    /**
     * Set the contribution branch name suggestions.
     *
     * @param branchNames
     *         the branch name suggestion list.
     */
    void setContributionBranchNameSuggestionList(List<String> branchNames);

    /**
     * Sets the enabled/disabled state of the contribution branch name field.
     */
    void setContributionBranchNameEnabled(boolean enabled);

    /**
     * Sets the contribution branch name input error state.
     *
     * @param showError
     *         {@code true} if the contribution branch name is in error, {@code false} otherwise.
     */
    void showContributionBranchNameError(boolean showError);

    /**
     * Returns the current content of the contribution comment.
     *
     * @return the comment.
     */
    String getContributionComment();

    /**
     * Sets the enabled/disabled state of the contribution comment field.
     */
    void setContributionCommentEnabled(boolean enabled);

    /**
     * Returns the contribution title.
     *
     * @return the title.
     */
    String getContributionTitle();

    /**
     * Sets the enabled/disabled state of the contribution title field.
     */
    void setContributionTitleEnabled(boolean enabled);

    /**
     * Sets the contribution title input error state.
     *
     * @param showError
     *         {@code true} if the contribution title is in error, {@code false} otherwise.
     */
    void showContributionTitleError(boolean showError);

    /**
     * Sets the enabled/disabled state of the "Contribute" button.
     *
     * @param enabled
     *         true to enable, false to disable
     */
    void setContributeEnabled(boolean enabled);

    /**
     * Sets the text displayed into the "Contribute" button.
     *
     * @param text
     *         the text to display
     */
    void setContributeButtonText(String text);

    /**
     * Shows the status section.
     */
    void showStatusSection();

    /**
     * Show the status footer.
     */
    void showStatusSectionFooter();

    /**
     * Clears the status section.
     */
    void clearStatusSection();

    /**
     * Hides the status section.
     */
    void hideStatusSection();

    /**
     * Show the new contribution section.
     */
    void showNewContributionSection();

    /**
     * Hide the new contribution section.
     */
    void hideNewContributionSection();

    /**
     * Sets the create fork step status.
     *
     * @param success
     *         {@code true} if success, {@code false} if error.
     */
    void setCreateForkStatus(boolean success);

    /**
     * Sets the push branch step status.
     *
     * @param success
     *         {@code true} if success, {@code false} if error.
     */
    void setPushBranchStatus(boolean success);

    /**
     * Sets the issue pull request step status.
     *
     * @param success
     *         {@code true} if success, {@code false} if error.
     */
    void setIssuePullRequestStatus(boolean success);

    /**
     * Defines if the contribution is in progress.
     *
     * @param progress
     *         {@code true} if the contribution is in progress, {@code false} otherwise.
     */
    void setContributionProgressState(boolean progress);

    /**
     * Action delegate interface for the contribution configuration dialog.
     */
    interface ActionDelegate extends BaseActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Contribute button. */
        void onContribute();

        /** Performs any action appropriate in response to the user having pressed the open on repository host button. */
        void onOpenOnRepositoryHost();

        /** Performs any action appropriate in response to the user having pressed the start new contribution button. */
        void onNewContribution();

        /** Performs any action when view state is modified. */
        void updateControls();
    }
}
