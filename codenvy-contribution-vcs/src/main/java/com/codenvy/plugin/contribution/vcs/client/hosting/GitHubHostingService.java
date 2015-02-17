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
package com.codenvy.plugin.contribution.vcs.client.hosting;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import com.codenvy.ide.api.app.CurrentUser;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.ext.github.client.GitHubClientService;
import com.codenvy.ide.ext.github.shared.GitHubPullRequest;
import com.codenvy.ide.ext.github.shared.GitHubPullRequestCreationInput;
import com.codenvy.ide.ext.github.shared.GitHubPullRequestList;
import com.codenvy.ide.ext.github.shared.GitHubRepository;
import com.codenvy.ide.ext.github.shared.GitHubRepositoryList;
import com.codenvy.ide.ext.github.shared.GitHubUser;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.util.Config;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequestHead;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.Repository;
import com.codenvy.security.oauth.JsOAuthWindow;
import com.codenvy.security.oauth.OAuthCallback;
import com.codenvy.security.oauth.OAuthStatus;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * {@link VcsHostingService} implementation for GitHub.
 *
 * @author Kevin Pollet
 */
public class GitHubHostingService implements VcsHostingService {
    private static final String SSH_URL_PREFIX                            = "git@github.com:";
    private static final String HTTPS_URL_PREFIX                          = "https://github.com/";
    private static final RegExp REPOSITORY_NAME_OWNER_PATTERN             = RegExp.compile("([^/]+)/([^.]+)");
    private static final String NO_COMMITS_IN_PULL_REQUEST_ERROR_MESSAGE  = "No commits between";
    private static final String PULL_REQUEST_ALREADY_EXISTS_ERROR_MESSAGE = "A pull request already exists for ";

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final DtoFactory             dtoFactory;
    private final GitHubClientService    gitHubClientService;
    private final GitHubTemplates        gitHubTemplates;
    private final String                 baseUrl;

    @Inject
    public GitHubHostingService(@Nonnull @Named("restContext") final String baseUrl,
                                @Nonnull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                @Nonnull final DtoFactory dtoFactory,
                                @Nonnull final GitHubClientService gitHubClientService,
                                @Nonnull final GitHubTemplates gitHubTemplates) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dtoFactory = dtoFactory;
        this.gitHubClientService = gitHubClientService;
        this.gitHubTemplates = gitHubTemplates;
        this.baseUrl = baseUrl;
    }

    @Override
    public void getUserInfo(@Nonnull final AsyncCallback<HostUser> callback) {
        gitHubClientService.getUserInfo(new AsyncRequestCallback<GitHubUser>(dtoUnmarshallerFactory.newUnmarshaller(GitHubUser.class)) {
            @Override
            protected void onSuccess(final GitHubUser gitHubUser) {
                if (gitHubUser == null) {
                    callback.onFailure(new Exception("No user info"));

                } else {
                    final HostUser user = dtoFactory.createDto(HostUser.class)
                                                    .withId(gitHubUser.getId())
                                                    .withLogin(gitHubUser.getLogin())
                                                    .withName(gitHubUser.getName())
                                                    .withUrl(gitHubUser.getUrl());
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
    public void getRepository(@Nonnull String owner, @Nonnull String repository, @Nonnull final AsyncCallback<Repository> callback) {
        gitHubClientService.getRepository(owner, repository, new AsyncRequestCallback<GitHubRepository>(
                dtoUnmarshallerFactory.newUnmarshaller(GitHubRepository.class)) {
            @Override
            protected void onSuccess(final GitHubRepository gitHubRepository) {
                callback.onSuccess(valueOf(gitHubRepository));
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void getRepositories(@Nonnull final AsyncCallback<List<Repository>> callback) {
        gitHubClientService.getRepositoriesList(
                new AsyncRequestCallback<GitHubRepositoryList>(dtoUnmarshallerFactory.newUnmarshaller(GitHubRepositoryList.class)) {
                    @Override
                    protected void onSuccess(final GitHubRepositoryList gitHubRepositoryList) {
                        final List<Repository> repositories = new ArrayList<>();
                        for (final GitHubRepository oneGitHubRepository : gitHubRepositoryList.getRepositories()) {
                            repositories.add(valueOf(oneGitHubRepository));
                        }
                        callback.onSuccess(repositories);
                    }

                    @Override
                    protected void onFailure(final Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
    }

    @Nonnull
    @Override
    public String getRepositoryNameFromUrl(@Nonnull final String url) {
        final String urlWithoutGitHubPrefix =
                url.substring(url.startsWith(SSH_URL_PREFIX) ? SSH_URL_PREFIX.length() : HTTPS_URL_PREFIX.length());

        return REPOSITORY_NAME_OWNER_PATTERN.exec(urlWithoutGitHubPrefix)
                                            .getGroup(2);
    }

    @Nonnull
    @Override
    public String getRepositoryOwnerFromUrl(@Nonnull final String url) {
        final String urlWithoutGitHubPrefix =
                url.substring(url.startsWith(SSH_URL_PREFIX) ? SSH_URL_PREFIX.length() : HTTPS_URL_PREFIX.length());

        return REPOSITORY_NAME_OWNER_PATTERN.exec(urlWithoutGitHubPrefix)
                                            .getGroup(1);
    }

    @Override
    public void fork(@Nonnull final String owner, @Nonnull final String repository, @Nonnull final AsyncCallback<Repository> callback) {
        gitHubClientService.fork(owner, repository, new AsyncRequestCallback<GitHubRepository>(
                dtoUnmarshallerFactory.newUnmarshaller(GitHubRepository.class)) {
            @Override
            protected void onSuccess(final GitHubRepository gitHubRepository) {
                if (gitHubRepository != null) {
                    callback.onSuccess(valueOf(gitHubRepository));

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

    @Nonnull
    @Override
    public String makeSSHRemoteUrl(@Nonnull final String username, @Nonnull final String repository) {
        return gitHubTemplates.sshUrlTemplate(username, repository);
    }

    @Nonnull
    @Override
    public String makeHttpRemoteUrl(@Nonnull final String username, @Nonnull final String repository) {
        return gitHubTemplates.httpUrlTemplate(username, repository);
    }

    @Nonnull
    @Override
    public String makePullRequestUrl(@Nonnull final String username, @Nonnull final String repository,
                                     @Nonnull final String pullRequestNumber) {
        return gitHubTemplates.pullRequestUrlTemplate(username, repository, pullRequestNumber);
    }

    @Nonnull
    @Override
    public String formatReviewFactoryUrl(@Nonnull final String reviewFactoryUrl) {
        final String protocol = Window.Location.getProtocol();
        final String host = Window.Location.getHost();

        return gitHubTemplates.formattedReviewFactoryUrlTemplate(protocol, host, reviewFactoryUrl);
    }

    @Override
    public boolean isVcsHostRemoteUrl(@Nonnull final String remoteUrl) {
        return remoteUrl.startsWith(SSH_URL_PREFIX) || remoteUrl.startsWith(HTTPS_URL_PREFIX);
    }

    @Override
    public void getPullRequest(@Nonnull final String owner, @Nonnull final String repository, @Nonnull final String headBranch,
                               @Nonnull final AsyncCallback<PullRequest> callback) {
        getPullRequests(owner, repository, new AsyncCallback<List<PullRequest>>() {
            @Override
            public void onSuccess(final List<PullRequest> pullRequests) {
                final PullRequest pullRequest = getPullRequestByBranch(headBranch, pullRequests);
                if (pullRequest != null) {
                    callback.onSuccess(pullRequest);

                } else {
                    callback.onFailure(new NoPullRequestException(headBranch));
                }
            }

            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    /**
     * Get all pull requests for given owner:repository
     *
     * @param owner
     *         the username of the owner.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    private void getPullRequests(@Nonnull final String owner, @Nonnull final String repository,
                                 @Nonnull final AsyncCallback<List<PullRequest>> callback) {
        gitHubClientService.getPullRequests(owner, repository, new AsyncRequestCallback<GitHubPullRequestList>(
                dtoUnmarshallerFactory.newUnmarshaller(GitHubPullRequestList.class)) {
            @Override
            protected void onSuccess(final GitHubPullRequestList result) {
                final List<PullRequest> pullRequests = new ArrayList<>();
                for (final GitHubPullRequest oneGitHubPullRequest : result.getPullRequests()) {
                    pullRequests.add(valueOf(oneGitHubPullRequest));
                }
                callback.onSuccess(pullRequests);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    protected PullRequest getPullRequestByBranch(final String headBranch, final List<PullRequest> pullRequests) {
        for (final PullRequest onePullRequest : pullRequests) {
            if (headBranch.equals(onePullRequest.getHead().getLabel())) {
                return onePullRequest;
            }
        }
        return null;
    }

    @Override
    public void getPullRequestById(final String owner, final String repository, final String pullRequestId,
                                   final AsyncCallback<PullRequest> callback) {
        final Unmarshallable<GitHubPullRequest> unmarshall = this.dtoUnmarshallerFactory.newUnmarshaller(GitHubPullRequest.class);
        gitHubClientService.getPullRequest(owner, repository, pullRequestId, new AsyncRequestCallback<GitHubPullRequest>(unmarshall) {
            @Override
            protected void onSuccess(final GitHubPullRequest result) {
                callback.onSuccess(valueOf(result));
            }
            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void createPullRequest(@Nonnull final String owner,
                                  @Nonnull final String repository,
                                  @Nonnull final String title,
                                  @Nonnull final String headBranch,
                                  @Nonnull final String baseBranch,
                                  @Nonnull final String body,
                                  @Nonnull final AsyncCallback<PullRequest> callback) {

        final GitHubPullRequestCreationInput input = dtoFactory.createDto(GitHubPullRequestCreationInput.class)
                                                               .withTitle(title)
                                                               .withHead(headBranch)
                                                               .withBase(baseBranch)
                                                               .withBody(body);

        final Unmarshallable<GitHubPullRequest> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(GitHubPullRequest.class);
        gitHubClientService.createPullRequest(owner, repository, input, new AsyncRequestCallback<GitHubPullRequest>(unmarshaller) {
            @Override
            protected void onSuccess(final GitHubPullRequest gitHubPullRequest) {
                callback.onSuccess(valueOf(gitHubPullRequest));
            }

            @Override
            protected void onFailure(final Throwable exception) {
                final String exceptionMessage = exception.getMessage();
                if (exceptionMessage != null && exceptionMessage.contains(NO_COMMITS_IN_PULL_REQUEST_ERROR_MESSAGE)) {
                    callback.onFailure(new NoCommitsInPullRequestException(headBranch, baseBranch));

                } else if (exceptionMessage != null && exceptionMessage.contains(PULL_REQUEST_ALREADY_EXISTS_ERROR_MESSAGE)) {
                    callback.onFailure(new PullRequestAlreadyExistsException(headBranch));

                } else {
                    callback.onFailure(exception);
                }
            }
        });
    }

    @Override
    public void getUserFork(@Nonnull final String user, @Nonnull String owner, @Nonnull final String repository,
                            @Nonnull final AsyncCallback<Repository> callback) {
        getForks(owner, repository, new AsyncCallback<List<Repository>>() {

            @Override
            public void onSuccess(final List<Repository> repositories) {
                final Repository userFork = getUserFork(user, repositories);
                if (userFork != null) {
                    callback.onSuccess(userFork);

                } else {
                    callback.onFailure(new NoUserForkException(user));
                }
            }

            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    /**
     * Returns the forks of the given repository for the given owner.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    private void getForks(@Nonnull final String owner, @Nonnull final String repository,
                          @Nonnull final AsyncCallback<List<Repository>> callback) {

        gitHubClientService.getForks(owner, repository, new AsyncRequestCallback<GitHubRepositoryList>(
                dtoUnmarshallerFactory.newUnmarshaller(GitHubRepositoryList.class)) {
            @Override
            protected void onSuccess(final GitHubRepositoryList gitHubRepositoryList) {
                final List<Repository> repositories = new ArrayList<>();
                for (final GitHubRepository oneGitHubRepository : gitHubRepositoryList.getRepositories()) {
                    repositories.add(valueOf(oneGitHubRepository));
                }
                callback.onSuccess(repositories);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private Repository getUserFork(final String login, final List<Repository> forks) {
        for (final Repository oneRepository : forks) {
            final String repositoryUrl = oneRepository.getCloneUrl();
            if (repositoryUrl.toLowerCase().contains("/" + login.toLowerCase() + "/")) {
                return oneRepository;
            }
        }
        return null;
    }

    /**
     * Converts an instance of {@link com.codenvy.ide.ext.github.shared.GitHubRepository} into a {@link
     * com.codenvy.plugin.contribution.vcs.client.hosting.dto.vcs.hosting.dto.Repository}.
     *
     * @param gitHubRepository
     *         the GitHub repository to convert.
     * @return the corresponding {@link com.codenvy.plugin.contribution.vcs.client.hosting.dto.vcs.hosting.dto.Repository} instance.
     */
    private Repository valueOf(final GitHubRepository gitHubRepository) {
        final GitHubRepository gitHubRepositoryParent = gitHubRepository.getParent();
        final Repository parent = gitHubRepositoryParent == null ? null :
                                  dtoFactory.createDto(Repository.class)
                                            .withFork(gitHubRepositoryParent.isFork())
                                            .withName(gitHubRepositoryParent.getName())
                                            .withParent(null)
                                            .withPrivateRepo(gitHubRepositoryParent.isPrivateRepo())
                                            .withCloneUrl(gitHubRepositoryParent.getCloneUrl());

        return dtoFactory.createDto(Repository.class)
                         .withFork(gitHubRepository.isFork())
                         .withName(gitHubRepository.getName())
                         .withParent(parent)
                         .withPrivateRepo(gitHubRepository.isPrivateRepo())
                         .withCloneUrl(gitHubRepository.getCloneUrl());
    }

    /**
     * Converts an instance of {@link com.codenvy.ide.ext.github.shared.GitHubPullRequest} into a {@link
     * com.codenvy.plugin.contribution.vcs.client.hosting.dto.vcs.hosting.dto.PullRequest}.
     *
     * @param gitHubPullRequest
     *         the GitHub pull request to convert.
     * @return the corresponding {@link com.codenvy.plugin.contribution.vcs.client.hosting.dto.vcs.hosting.dto.PullRequest} instance.
     */
    private PullRequest valueOf(final GitHubPullRequest gitHubPullRequest) {
        final PullRequestHead pullRequestHead = dtoFactory.createDto(PullRequestHead.class)
                                                          .withLabel(gitHubPullRequest.getHead().getLabel())
                                                          .withRef(gitHubPullRequest.getHead().getRef())
                                                          .withSha(gitHubPullRequest.getHead().getSha());

        return dtoFactory.createDto(PullRequest.class)
                         .withId(gitHubPullRequest.getId())
                         .withUrl(gitHubPullRequest.getUrl())
                         .withHtmlUrl(gitHubPullRequest.getHtmlUrl())
                         .withNumber(gitHubPullRequest.getNumber())
                         .withState(gitHubPullRequest.getState())
                         .withHead(pullRequestHead);
    }

    @Override
    public void authenticate(final CurrentUser currentUser, final AsyncCallback<HostUser> callback) {
        final String authUrl = baseUrl
                               + "/oauth/authenticate?oauth_provider=github&userId=" + currentUser.getProfile().getId()
                               + "&scope=user,repo,write:public_key&redirect_after_login="
                               + Window.Location.getProtocol() + "//"
                               + Window.Location.getHost() + "/ws/"
                               + Config.getWorkspaceName();

        new JsOAuthWindow(authUrl, "error.url", 500, 980, new OAuthCallback() {
            @Override
            public void onAuthenticated(final OAuthStatus authStatus) {
                // maybe it's possible to avoid this request if authStatus contains the vcs host user.
                getUserInfo(callback);
            }
        }).loginWithOAuth();
    }
}
