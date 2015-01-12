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
package com.codenvy.plugin.contribution.client.dialogs.commit;

import com.codenvy.ide.api.mvp.View;

import javax.annotation.Nonnull;

/**
 * View for committing uncommitted project changes.
 *
 * @author Kevin Pollet
 */
public interface CommitView extends View<CommitView.ActionDelegate> {

    interface ActionDelegate {
        /**
         * Called when project changes must be committed.
         */
        void onOk();

        /**
         * Called when project changes must not be committed.
         */
        void onContinue();

        /**
         * Called when the operation must be aborted.
         */
        void onCancel();

        /**
         * Called when the commit description is changed.
         */
        void onCommitDescriptionChanged();
    }

    /**
     * Opens the commit view with the given commit description.
     */
    void show();

    /**
     * Close the commit view.
     */
    void close();

    /**
     * Returns the current commit description.
     *
     * @return the current commit description.
     */
    @Nonnull
    String getCommitDescription();

    /**
     * Enables or disables the button OK.
     *
     * @param enabled
     *         {@code true} to enable the OK button, {@code false} otherwise.
     */
    void setOkButtonEnabled(final boolean enabled);
}
