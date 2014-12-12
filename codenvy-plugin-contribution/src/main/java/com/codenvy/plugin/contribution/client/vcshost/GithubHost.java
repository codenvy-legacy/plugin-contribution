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
package com.codenvy.plugin.contribution.client.vcshost;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.ext.github.client.GitHubClientService;
import com.codenvy.ide.ext.github.shared.GitHubRepository;
import com.codenvy.ide.ext.github.shared.GitHubRepositoryList;
import com.codenvy.ide.ext.github.shared.GitHubUser;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GithubHost implements RepositoryHost {

    private final GitHubClientService gitHubClientService;
    private final DtoFactory dtoFactory;

    @Inject
    public GithubHost(final DtoFactory dtoFactory,
                      final GitHubClientService gitHubClientService) {
        this.dtoFactory = dtoFactory;
        this.gitHubClientService = gitHubClientService;
    }

    @Override
    public void getUserInfo(final AsyncCallback<HostUser> callback) {
        this.gitHubClientService.getUserInfo(new AsyncRequestCallback<GitHubUser>() {
            @Override
            protected void onSuccess(final GitHubUser result) {
                final HostUser user = GithubHost.this.dtoFactory.createDto(HostUser.class);
                user.withId(result.getId()).withLogin(user.getLogin()).withName(user.getName()).withUrl(user.getUrl());
                callback.onSuccess(user);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void getRepositoriesList(final AsyncCallback<List<Repository>> callback) {
        this.gitHubClientService.getRepositoriesList(new AsyncRequestCallback<GitHubRepositoryList>() {
            @Override
            protected void onSuccess(final GitHubRepositoryList result) {
                final List<Repository> repositories = new ArrayList<>();
                for (final GitHubRepository original : result.getRepositories()) {
                    final Repository repository = GithubHost.this.dtoFactory.createDto(Repository.class);
                    repository.withFork(original.isFork()).withName(original.getName())
                              .withPrivateRepo(original.isPrivateRepo()).withUrl(original.getUrl());
                    repositories.add(repository);
                }
                callback.onSuccess(repositories);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

}
