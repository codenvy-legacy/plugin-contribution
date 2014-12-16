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
package com.codenvy.plugin.contribution.client.vcs;

import java.util.List;

import javax.annotation.Nonnull;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Service for VCS operations.
 */
public interface VcsService {
    /**
     * Checkout a branch of the given project.
     * 
     * @param project the project descriptor
     * @param branchName the name of the branch to checkout
     * @param createNew create a new branch if true
     * @param callback callback when the operation is done
     */
    void checkoutBranch(@Nonnull ProjectDescriptor project, String branchName, boolean createNew, AsyncCallback<String> callback);

    /**
     * Create a branch for the given project.
     * 
     * @param project the project descriptor
     * @param name the name of the branch to create
     * @param startPoint commit at which to start the new branch
     * @param callback callback when the operation is done
     */
    void createBranch(@Nonnull ProjectDescriptor project, String name, String startPoint, AsyncCallback<Branch> callback);

    /**
     * Get the current branch for the project.
     * 
     * @param project the project descriptor
     * @param callback callback when the operation is done
     */
    void getBranchName(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<String> callback);

    /**
     * Rename a branch of the project.
     * 
     * @param project the project descriptor
     * @param oldName the name of the branch to rename
     * @param newName the new name
     * @param callback callback when the operation is done
     */
    void renameBranch(@Nonnull ProjectDescriptor project, String oldName, String newName, AsyncCallback<Void> callback);

    /**
     * List the local branches.
     * 
     * @param project the project descriptor
     * @param callback what to to with the branches list
     */
    void listLocalBranches(@Nonnull ProjectDescriptor project, AsyncCallback<List<Branch>> callback);
}
