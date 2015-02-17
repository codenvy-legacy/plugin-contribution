/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.review.client;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;

/**
 * Context used to share information between the steps in the contribution workflow.
 *
 * @author Kevin Pollet
 */
public class Context {

    /** The project. */
    private ProjectDescriptor project;

    /** The name of the cloned branch. */
    private String clonedBranchName;

    /** The name of the working branch. */
    private String workBranchName;

    /** The name of the user on host VCS. */
    private String hostUserLogin;

    /** The name of the owner of the repository forked on VCS. */
    private String upstreamRepositoryOwner;

    /** The name of the repository forked on VCS. */
    private String upstreamRepositoryName;

    /** The name of the owner of the repository cloned on VCS. */
    private String originRepositoryOwner;

    /** The name of the repository cloned on VCS. */
    private String originRepositoryName;

    /** The identifier of the pull request on the hosting service. */
    private String pullRequestId;

    /** The issue number of the pull request issued for the contribution. */
    private String pullRequestIssueNumber;

    /** The name of the forked remote. */
    private String forkedRemoteName;

    /** The name of the forked repository. */
    private String forkedRepositoryName;

    public ProjectDescriptor getProject() {
        return project;
    }

    public void setProject(final ProjectDescriptor project) {
        this.project = project;
    }

    public String getClonedBranchName() {
        return clonedBranchName;
    }

    public void setClonedBranchName(final String clonedBranchName) {
        this.clonedBranchName = clonedBranchName;
    }

    public String getWorkBranchName() {
        return workBranchName;
    }

    public void setWorkBranchName(final String workBranchName) {
        this.workBranchName = workBranchName;
    }

    public String getHostUserLogin() {
        return hostUserLogin;
    }

    public void setHostUserLogin(final String hostUserLogin) {
        this.hostUserLogin = hostUserLogin;
    }

    public String getUpstreamRepositoryOwner() {
        return upstreamRepositoryOwner;
    }

    public void setUpstreamRepositoryOwner(String upstreamRepositoryOwner) {
        this.upstreamRepositoryOwner = upstreamRepositoryOwner;
    }

    public String getUpstreamRepositoryName() {
        return upstreamRepositoryName;
    }

    public void setUpstreamRepositoryName(String upstreamRepositoryName) {
        this.upstreamRepositoryName = upstreamRepositoryName;
    }

    public String getOriginRepositoryOwner() {
        return originRepositoryOwner;
    }

    public void setOriginRepositoryOwner(final String originRepositoryOwner) {
        this.originRepositoryOwner = originRepositoryOwner;
    }

    public String getOriginRepositoryName() {
        return originRepositoryName;
    }

    public void setOriginRepositoryName(final String originRepositoryName) {
        this.originRepositoryName = originRepositoryName;
    }

    /**
     * Return the pull request id for this contribution.
     *
     * @return the pull request id
     */
    public String getPullRequestId() {
        return pullRequestId;
    }

    /**
     * Sets the pull request id for this contribution.
     *
     * @param pullRequestId
     *         the new value
     */
    public void setPullRequestId(final String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    /**
     * Return the issue number of the pull request issued for this contribution.
     *
     * @return the pull request issue id
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
