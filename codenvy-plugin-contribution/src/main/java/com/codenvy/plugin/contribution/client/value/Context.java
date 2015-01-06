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

    /** The name of the cloned branch. */
    private String clonedBranchName;

    /** The name of the working branch. */
    private String workBranchName;

    /** The name of the user on VCS. */
    private String hostUserLogin;

    /** The name of the owner of the origin repository on VCS. */
    private String originRepositoryOwner;

    /** The name of the repository forked on VCS. */
    private String originRepositoryName;

    /** The issue number of the pull request issued for the contribution. */
    private String pullRequestIssueNumber;

    /** Flag that tells is the fork has been seen. */
    private boolean forkReady = false;

    /** The generated review factory URL. */
    private String reviewFactoryUrl;

    /** The name of the forked remote. */
    private String forkedRemoteName;

    /** The name of the forked repository. */
    private String forkedRepositoryName;

    public ProjectDescriptor getProject() {
        return project;
    }

    public void setProject(ProjectDescriptor desc) {
        project = desc;
    }

    public Context withProject(ProjectDescriptor desc) {
        project = desc;
        return this;
    }

    public String getClonedBranchName() {
        return clonedBranchName;
    }

    public void setClonedBranchName(String name) {
        clonedBranchName = name;
    }

    public Context withClonedBranchName(String name) {
        clonedBranchName = name;
        return this;
    }

    public String getWorkBranchName() {
        return workBranchName;
    }

    public void setWorkBranchName(String name) {
        workBranchName = name;
    }

    public Context withWorkBranchName(String name) {
        workBranchName = name;
        return this;
    }

    /**
     * Tells if the fork is ready.
     *
     * @return true iff the fork is ready
     */
    public boolean getForkReady() {
        return forkReady;
    }

    /**
     * Sets the fork ready flag.
     *
     * @param newValue
     *         the new value
     */
    public void setForkReady(final boolean newValue) {
        forkReady = newValue;
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

    public String getOriginRepositoryOwner() {
        return originRepositoryOwner;
    }

    public void setOriginRepositoryOwner(String originRepositoryOwner) {
        this.originRepositoryOwner = originRepositoryOwner;
    }

    public Context withOriginRepositoryOwner(String originRepositoryOwner) {
        this.originRepositoryOwner = originRepositoryOwner;
        return this;
    }

    public String getOriginRepositoryName() {
        return originRepositoryName;
    }

    public void setOriginRepositoryName(String originRepositoryName) {
        this.originRepositoryName = originRepositoryName;
    }

    public Context withOriginRepositoryName(String originRepositoryName) {
        this.originRepositoryName = originRepositoryName;
        return this;
    }

    /**
     * Return the issue number of the pull request issued for this contribution.
     *
     * @return the pull request id
     */
    public String getPullRequestIssueNumber() {
        return pullRequestIssueNumber;
    }

    /**
     * Sets the issue number of the pull request issued for this contribution.
     *
     * @param pullRequestIssueNumber
     *         the new value
     */
    public void setPullRequestIssueNumber(final String pullRequestIssueNumber) {
        this.pullRequestIssueNumber = pullRequestIssueNumber;
    }

    public Context withPullRequestIssueNumber(final String pullRequestIssueNumber) {
        this.pullRequestIssueNumber = pullRequestIssueNumber;
        return this;
    }

    /**
     * Returns the generated review factory URL (if available).
     *
     * @return factory URL
     */
    public String getReviewFactoryUrl() {
        return this.reviewFactoryUrl;
    }

    /**
     * Sets the generated review factory URL (if available).
     *
     * @param factoryUrl
     *         new value
     */
    public void setReviewFactoryUrl(final String factoryUrl) {
        this.reviewFactoryUrl = factoryUrl;
    }

    /**
     * Sets the generated review factory URL (if available).
     *
     * @param factoryUrl
     *         new value
     * @return this object
     */
    public Context withReviewFactoryUrl(final String factoryUrl) {
        this.reviewFactoryUrl = factoryUrl;
        return this;
    }

    public String getForkedRemoteName() {
        return forkedRemoteName;
    }

    public void setForkedRemoteName(String forkedRemoteName) {
        this.forkedRemoteName = forkedRemoteName;
    }

    public String getForkedRepositoryName() {
        return forkedRepositoryName;
    }

    public void setForkedRepositoryName(String forkedRepositoryName) {
        this.forkedRepositoryName = forkedRepositoryName;
    }
}
