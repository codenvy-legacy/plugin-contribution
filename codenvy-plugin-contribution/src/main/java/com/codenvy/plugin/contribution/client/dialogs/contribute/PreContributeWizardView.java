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
package com.codenvy.plugin.contribution.client.dialogs.contribute;

import com.codenvy.ide.api.mvp.View;
import com.codenvy.ide.api.wizard.Wizard;

/**
 * Interface for the contribution configuration shown when the user decides to send their contribution.
 */
public interface PreContributeWizardView extends View<PreContributeWizardView.ActionDelegate> {

    /**
     * Resets the dialog fields to their initial value.
     */
    void reset();

    /**
     * Shows the dialog.
     */
    void show();

    /**
     * Hide the dialog.
     */
    void hide();

    /**
     * Returns the current content of the branch name.
     *
     * @return the branch name
     */
    String getBranchName();

    /**
     * Returns the current content of the pull request comment.
     *
     * @return the comment
     */
    String getPullRequestComment();

    /**
     * Returns the contribution title/
     *
     * @return the title
     */
    String getContribTitle();

    /**
     * Sets the enabled/disabled state of the "Contribute" button.
     *
     * @param enabled
     *         true to enable, false to disable
     */
    void setContributeEnabled(boolean enabled);


    /**
     * Action delegate interface for the contribution configuration dialog.
     */
    interface ActionDelegate extends Wizard.UpdateDelegate {

        /** Performs any actions appropriate in response to the user having pressed the Contribute button */
        void onContributeClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button */
        void onCancelClicked();

        /**
         * Suggests a branch name for the current work.
         *
         * @return the suggestion
         */
        String suggestBranchName();
    }
}
