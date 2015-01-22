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
     * Returns the current content of the branch name.
     *
     * @return the branch name
     */
    String getBranchName();

    /**
     * Returns the current content of the contribution comment.
     *
     * @return the comment.
     */
    String getContributionComment();

    /**
     * Returns the contribution title.
     *
     * @return the title.
     */
    String getContributionTitle();

    /**
     * Sets the focused/unfocused state of the branch name field.
     */
    void setBranchNameFocus(boolean focused);

    /**
     * Sets the enabled/disabled state of the branch name field.
     */
    void setBranchNameEnabled(boolean enabled);

    /**
     * Sets the enabled/disabled state of the contribution comment field.
     */
    void setContributionCommentEnabled(boolean enabled);

    /**
     * Sets the enabled/disabled state of the contribution title field.
     */
    void setContributionTitleEnabled(boolean enabled);

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
     * Sets the branch name input error state.
     *
     * @param showError
     *         {@code true} if the branch name is in error, {@code false} otherwise.
     */
    void showBranchNameError(boolean showError);

    /**
     * Sets the contribution title input error state.
     *
     * @param showError
     *         {@code true} if the contribution title is in error, {@code false} otherwise.
     */
    void showContributionTitleError(boolean showError);

    /**
     * Shows the status section.
     */
    void showStatusSection();

    /**
     * Hides the status section.
     */
    void hideStatusSection();

    /**
     * Resets the status section.
     */
    void resetStatusSection();

    /**
     * Show the status footer.
     */
    void showStatusSectionFooter();

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

        /**
         * Suggests a branch name for the current work.
         *
         * @return the suggestion.
         */
        String suggestBranchName();
    }
}
