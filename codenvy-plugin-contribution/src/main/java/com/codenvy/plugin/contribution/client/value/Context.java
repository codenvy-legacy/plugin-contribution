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
package com.codenvy.plugin.contribution.client.value;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;

/**
 * Contribution context, with information on current project, branch etc.
 */
public class Context {
    /** The project. */
    private ProjectDescriptor project;

    /**
     * The name of the working branch.
     */
    private String workBranchName;

    /**
     * The name of the user on VCS.
     */
    private String hostUserLogin;

    /**
     * The name of the repository forked on VCS.
     */
    private String repositoryName;

    /**
     * The id of the pull request issued for the contribution.
     */
    private String pullRequestId;

    /**
     * Flag that tells is the fork has been seen.
     */
    private boolean forkReady = false;

    public ProjectDescriptor getProject() {
        return this.project;
    }

    public void setProject(ProjectDescriptor desc) {
        this.project = desc;
    }

    public Context withProject(ProjectDescriptor desc) {
        this.project = desc;
        return this;
    }

    public String getWorkBranchName() {
        return this.workBranchName;
    }

    public void setWorkBranchName(String name) {
        this.workBranchName = name;
    }

    public Context withWorkBranchName(String name) {
        this.workBranchName = name;
        return this;
    }

    /**
     * Tells if the fork is ready.
     *
     * @return true iff the fork is ready
     */
    public boolean getForkReady() {
        return this.forkReady;
    }

    /**
     * Sets the fork ready flag.
     *
     * @param newValue
     *         the new value
     */
    public void setForkReady(final boolean newValue) {
        this.forkReady = newValue;
    }

    public String getHostUserLogin() {
        return hostUserLogin;
    }

    public void setHostUserLogin(String hostUserLogin) {
        this.hostUserLogin = hostUserLogin;
    }

    public Context withHostUserLogin(String hostUserLogin) {
        this.hostUserLogin = hostUserLogin;
        return this;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public Context withRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    /**
     * Return the id of the pull request issued for this contribution.
     * 
     * @return the pull request id
     */
    public String getPullRequestId() {
        return pullRequestId;
    }

    /**
     * Sets the id of the pull request issued for this contribution.
     * 
     * @param pullRequestId the new value
     */
    public void setPullRequestId(final String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    public Context withPullRequestId(final String pullRequestId) {
        this.pullRequestId = pullRequestId;
        return this;
    }
}
