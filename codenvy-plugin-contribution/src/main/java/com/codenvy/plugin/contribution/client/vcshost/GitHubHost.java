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

import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.ext.github.client.GitHubClientService;
import com.codenvy.ide.ext.github.shared.GitHubRepository;
import com.codenvy.ide.ext.github.shared.GitHubRepositoryList;
import com.codenvy.ide.ext.github.shared.GitHubUser;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.codenvy.plugin.contribution.client.vcshost.RepositoryHost} implementation for GitHub.
 */
public class GitHubHost implements RepositoryHost {

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final DtoFactory             dtoFactory;
    private final GitHubClientService    gitHubClientService;

    /**
     * The templates for repository URLs.
     */
    private final UrlTemplates urlTemplates;

    @Inject
    public GitHubHost(final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                      final DtoFactory dtoFactory,
                      final GitHubClientService gitHubClientService,
                      final UrlTemplates urlTemplates) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dtoFactory = dtoFactory;
        this.gitHubClientService = gitHubClientService;
        this.urlTemplates = urlTemplates;
    }

    @Override
    public void getUserInfo(final AsyncCallback<HostUser> callback) {
        gitHubClientService
                .getUserInfo(new AsyncRequestCallback<GitHubUser>(dtoUnmarshallerFactory.newUnmarshaller(GitHubUser.class)) {
                    @Override
                    protected void onSuccess(final GitHubUser result) {
                        if (result == null) {
                            callback.onFailure(new Exception("No user info"));
                        } else {
                            final HostUser user = GitHubHost.this.dtoFactory.createDto(HostUser.class);
                            user.withId(result.getId()).withLogin(result.getLogin()).withName(result.getName()).withUrl(result.getUrl());
                            callback.onSuccess(user);
                        }
                    }

                    @Override
                    protected void onFailure(final Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
    }

    @Override
    public void getRepositoriesList(final AsyncCallback<List<Repository>> callback) {
        gitHubClientService.getRepositoriesList(
                new AsyncRequestCallback<GitHubRepositoryList>(dtoUnmarshallerFactory.newUnmarshaller(GitHubRepositoryList.class)) {
                    @Override
                    protected void onSuccess(final GitHubRepositoryList result) {
                        final List<Repository> repositories = new ArrayList<>();
                        for (final GitHubRepository original : result.getRepositories()) {
                            final Repository repository = GitHubHost.this.dtoFactory.createDto(Repository.class);
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

    @Override
    public void getForks(final String owner, final String repository, final AsyncCallback<List<Repository>> callback) {
        gitHubClientService.getForks(owner,
                                     repository,
                                     new AsyncRequestCallback<GitHubRepositoryList>(
                                             dtoUnmarshallerFactory.newUnmarshaller(GitHubRepositoryList.class)) {
                                         @Override
                                         protected void onSuccess(final GitHubRepositoryList result) {
                                             final List<Repository> repositories = new ArrayList<>();
                                             for (final GitHubRepository original : result.getRepositories()) {
                                                 final Repository repository = GitHubHost.this.dtoFactory.createDto(Repository.class);
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

    @Override
    public void fork(final String owner, final String repository, final AsyncCallback<Repository> callback) {
        gitHubClientService.fork(owner,
                                 repository,
                                 new AsyncRequestCallback<GitHubRepository>(
                                         dtoUnmarshallerFactory.newUnmarshaller(GitHubRepository.class)) {
                                     @Override
                                     protected void onSuccess(final GitHubRepository result) {
                                         if (result != null) {
                                             final Repository repository = GitHubHost.this.dtoFactory.createDto(Repository.class);
                                             repository.withFork(result.isFork()).withName(result.getName())
                                                       .withPrivateRepo(result.isPrivateRepo()).withUrl(result.getUrl());
                                             callback.onSuccess(repository);
                                         } else {
                                             callback.onFailure(new Exception("No repository."));
                                         }
                                     }

                                     @Override
                                     protected void onFailure(final Throwable exception) {
                                         callback.onFailure(exception);
                                     }
                                 });
    }

    @Override
    public String makeRemoteUrl(final String username, final String repository) {
        return urlTemplates.gitRemoteTemplate(username, repository);
    }

    @Override
    public void commentPullRequest(final String username, final String repository,
                                   final String pullRequestId, final String commentText,
                                   final AsyncCallback<Void> callback) {

    }

    @Override
    public void createPullRequest(final String owner,
                                  final String repository,
                                  final String title,
                                  final String headBranch,
                                  final String baseBranch,
                                  final String body,
                                  final AsyncCallback<PullRequest> callback) {

    }
}
