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
package com.codenvy.ide.contributor.client;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.ext.git.client.GitServiceClient;
import com.codenvy.ide.ext.git.shared.Branch;
import com.codenvy.ide.ext.github.client.GitHubClientService;
import com.codenvy.ide.ext.github.shared.GitHubRepositoryList;
import com.codenvy.ide.ext.github.shared.GitHubUser;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GitAgent {

    private final GitServiceClient    gitServiceClient;
    private final GitHubClientService gitHubClientService;

    @Inject
    public GitAgent(GitServiceClient gitServiceClient, GitHubClientService gitHubClientService) {
        this.gitServiceClient = gitServiceClient;
        this.gitHubClientService = gitHubClientService;
    }

    public void checkoutBranch(ProjectDescriptor project, String name, boolean createNew, AsyncRequestCallback<String> callback) {
        gitServiceClient.branchCheckout(project, name, null, createNew, callback);
    }

    public void createBranch(ProjectDescriptor project, String name, String startPoint, AsyncRequestCallback<Branch> callback) {
        gitServiceClient.branchCreate(project, name, startPoint, callback);
    }

    public void getUserInfo(AsyncRequestCallback<GitHubUser> callback) {
        gitHubClientService.getUserInfo(callback);
    }

    public void getRepositoriesList(AsyncRequestCallback<GitHubRepositoryList> callback) {
        gitHubClientService.getRepositoriesList(callback);
    }

    public void getForks(String user, String repository, AsyncRequestCallback<GitHubRepositoryList> callback) {
        gitHubClientService.getForks(user, repository, callback);
    }
}
