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
     * Resets the part fields to their initial value.
     */
    void reset();

    /**
     * Set factory's repository URL.
     *
     */
    void setRepositoryUrl(String url);

    /**
     * Set factory's cloned branch name.
     *
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
     * Sets the enabled/disabled state of the "Contribute" button.
     *
     * @param enabled
     *         true to enable, false to disable
     */
    void setContributeEnabled(boolean enabled);

    /**
     * Hides the contribute button.
     */
    void hideContribute();

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
     * Checks the create fork status checkbox.
     */
    void checkCreateForkCheckBox();

    /**
     * Show the status footer.
     */
    void showStatusSectionFooter();

    /**
     * Checks the push branch status checkbox.
     */
    void checkPushBranchCheckBox();

    /**
     * Checks the issue pull request status checkbox.
     */
    void checkIssuePullRequestCheckBox();

    /**
     * Action delegate interface for the contribution configuration dialog.
     */
    interface ActionDelegate extends BaseActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Contribute button */
        void onContribute();

        /** Performs any action appropriate in response to the user having pressed the open on repository host button */
         void onOpenOnRepositoryHost();

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
