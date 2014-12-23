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

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Service for VCS operations.
 */
public interface VcsService {
    /**
     * Add a remote to the project VCS metadata.
     *
     * @param project
     *         the project descriptor.
     * @param remote
     *         the remote name.
     * @param remoteUrl
     *         the remote URL.
     * @param callback
     *         callback when the operation is done.
     */
    void addRemote(@Nonnull ProjectDescriptor project, @Nonnull String remote, @Nonnull String remoteUrl, @Nonnull AsyncCallback<Void> callback);

    /**
     * Checkout a branch of the given project.
     *
     * @param project
     *         the project descriptor.
     * @param branchName
     *         the name of the branch to checkout.
     * @param createNew
     *         create a new branch if {@code true}.
     * @param callback
     *         callback when the operation is done.
     */
    void checkoutBranch(@Nonnull ProjectDescriptor project, @Nonnull String branchName, boolean createNew, @Nonnull AsyncCallback<String> callback);

    /**
     * Create a branch for the given project.
     *
     * @param project
     *         the project descriptor.
     * @param name
     *         the name of the branch to create.
     * @param startPoint
     *         commit at which to start the new branch.
     * @param callback
     *         callback when the operation is done.
     */
    void createBranch(@Nonnull ProjectDescriptor project, @Nonnull String name, @Nonnull String startPoint, @Nonnull AsyncCallback<Branch> callback);

    /**
     * Removes a remote to the project VCS metadata.
     *
     * @param project
     *         the project descriptor.
     * @param remote
     *         the remote name.
     * @param callback
     *         callback when the operation is done.
     */
    void deleteRemote(@Nonnull ProjectDescriptor project, @Nonnull String remote, @Nonnull AsyncCallback<Void> callback);

    /**
     * Get the current branch for the project.
     *
     * @param project
     *         the project descriptor.
     * @param callback
     *         callback when the operation is done.
     */
    void getBranchName(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<String> callback);

    /**
     * List the local branches.
     *
     * @param project
     *         the project descriptor.
     * @param callback
     *         what to to with the branches list.
     */
    void listLocalBranches(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<List<Branch>> callback);

    /**
     * List remotes.
     *
     * @param project
     *         the project descriptor.
     * @param callback
     *         what to to with the remotes list
     */
    void listRemotes(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<List<Remote>> callback);

    /**
     * Push a local branch to the given remote.
     *
     * @param project
     *         the project descriptor.
     * @param remote
     *         the remote name
     * @param localBranchNameToPush
     *         the local branch name
     * @param callback
     *         callback when the operation is done.
     */
    void pushBranch(@Nonnull ProjectDescriptor project, @Nonnull String remote, @Nonnull String localBranchNameToPush, @Nonnull AsyncCallback<Void> callback);

    /**
     * Rename a branch of the project.
     *
     * @param project
     *         the project descriptor.
     * @param oldName
     *         the name of the branch to rename.
     * @param newName
     *         the new name.
     * @param callback
     *         callback when the operation is done.
     */
    void renameBranch(@Nonnull ProjectDescriptor project, @Nonnull String oldName, @Nonnull String newName, @Nonnull AsyncCallback<Void> callback);
}
