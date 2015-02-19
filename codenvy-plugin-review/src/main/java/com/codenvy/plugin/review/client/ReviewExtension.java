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


import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.shared.client.Authentifier;
import com.codenvy.plugin.contribution.shared.client.SharedConstants;
import com.codenvy.plugin.contribution.vcs.client.Remote;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;
import com.codenvy.plugin.review.client.promises.Operation;
import com.codenvy.plugin.review.client.promises.OperationException;
import com.codenvy.plugin.review.client.promises.Promise;
import com.codenvy.plugin.review.client.promises.PromiseError;
import com.codenvy.plugin.review.client.promises.internal.AsyncPromiseHelper;
import com.codenvy.plugin.review.client.promises.internal.AsyncPromiseHelper.RequestCall;
import com.codenvy.plugin.review.client.promises.js.Promises;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

@Singleton
@Extension(title = "Review", version = "1.0.0")
public class ReviewExtension implements ProjectActionHandler {
    private final Authentifier        authentifier;
    private final ReviewMessages      messages;
    private final VcsHostingService   vcsHostingService;
    private final VcsServiceProvider  vcsServiceProvider;
    private final ReviewState reviewState;

    @Inject
    public ReviewExtension(@Nonnull final Authentifier authentifier,
                           @Nonnull final EventBus eventBus,
                           @Nonnull final ReviewMessages messages,
                           @Nonnull final ReviewResources resources,
                           @Nonnull final ReviewState reviewState,
                           @Nonnull final VcsHostingService vcsHostingService,
                           @Nonnull final VcsServiceProvider vcsServiceProvider) {
        this.authentifier = authentifier;
        this.messages = messages;
        this.vcsHostingService = vcsHostingService;
        this.vcsServiceProvider = vcsServiceProvider;
        this.reviewState = reviewState;

        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    @Override
    public void onProjectOpened(final ProjectActionEvent event) {
        initializeReviewExtension(event.getProject());
    }

    @Override
    public void onProjectClosed(final ProjectActionEvent event) {
    }

    private void initializeReviewExtension(final ProjectDescriptor project) {
        final VcsService vcsService = this.vcsServiceProvider.getVcsService(project);
        final List<String> projectPermissions = project.getPermissions();
        final List<String> reviewAttr = project.getAttributes().get(SharedConstants.ATTRIBUTE_REVIEW_KEY);
        final List<String> pullRequestIdAttr = project.getAttributes().get(SharedConstants.ATTRIBUTE_REVIEW_PULLREQUEST_ID);
        final List<String> upstreamOwnerAttr = project.getAttributes().get(SharedConstants.ATTRIBUTE_REVIEW_UPSTREAM_OWNER);
        final List<String> upstreamRepositoryAttr = project.getAttributes().get(SharedConstants.ATTRIBUTE_REVIEW_UPSTREAM_REPOSITORY);

        if (vcsService != null
                && projectPermissions != null && projectPermissions.contains("write")
            && (reviewAttr != null) && !(reviewAttr.isEmpty())
            && (pullRequestIdAttr != null) && !(pullRequestIdAttr.isEmpty())
            && (upstreamOwnerAttr != null) && !(upstreamOwnerAttr.isEmpty())
            && (upstreamRepositoryAttr != null) && !(upstreamRepositoryAttr.isEmpty())) {

            this.reviewState.init();
            this.reviewState.getContext().setUpstreamRepositoryName(upstreamRepositoryAttr.get(0));
            this.reviewState.getContext().setUpstreamRepositoryOwner(upstreamOwnerAttr.get(0));

            final Promise<Remote> remotePromise = retrieveUpstreamRemote(project);

            // if error handling needed, it's here - for the moment jsut bail out, nothing has started
            remotePromise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(final PromiseError error) throws OperationException {
                    Log.info(ReviewExtension.class, "Review flag set, pull request id present but no upstream remote found");
                    Log.debug(ReviewExtension.class, error.toString());
                    onError(AbortReviewCause.NO_UPSTREAM_REMOTE);
                    throw new OperationException();
                }
            });

            // find owner and repository of the fork
            final Promise<Remote> repositoryPromise = remotePromise.then(new Operation<Remote>() {
                @Override
                public void apply(final Remote remote) throws OperationException {
                    setupUserAndRepository(remote);
                }
            });
            repositoryPromise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(final PromiseError arg) throws OperationException {
                    onError(AbortReviewCause.REMOTE_URL_HOSTING_MISMATCH);
                    throw new OperationException();
                }
            });

            // get pull request info
            final String pullRequestId = pullRequestIdAttr.get(0);
            final Promise<PullRequest> prPromise = repositoryPromise.then(getPullRequest(pullRequestId));


            // auth
            final Promise<HostUser> authPromise = authenticate();

            // join auth and PR
            final Promise<JsArrayMixed> authAndPR = Promises.all(prPromise, authPromise);
            authAndPR.then(new Operation<JsArrayMixed>() {
                @Override
                public void apply(final JsArrayMixed arg) throws OperationException {
                    Window.alert("here!");
                }
            });
        }
    }

    private Promise<PullRequest> getPullRequest(final String pullRequestId) {
        return AsyncPromiseHelper.createFromAsyncRequest(new RequestCall<PullRequest>() {
            @Override
            public void makeCall(final AsyncCallback<PullRequest> callback) {
                final Context context = reviewState.getContext();
                vcsHostingService.getPullRequestById(context.getOriginRepositoryOwner(),
                                                     context.getOriginRepositoryName(),
                                                     pullRequestId,
                                                     callback);
            }
        });
    }

    private Promise<Remote> retrieveUpstreamRemote(final ProjectDescriptor project) {
        final VcsService vcsService = this.vcsServiceProvider.getVcsService(project);
        final RequestCall<Remote> getUpstreamRemoteCall = new RequestCall<Remote>() {
            @Override
            public void makeCall(final AsyncCallback<Remote> callback) {
                vcsService.getUpstreamRemote(project, callback);
            }
        };
        return AsyncPromiseHelper.createFromAsyncRequest(getUpstreamRemoteCall);
    }

    private void setupUserAndRepository(final Remote remote) throws OperationException {
        String url = remote.getUrl();
        if (this.vcsHostingService.isVcsHostRemoteUrl(url)) {
            final String repositoryName = this.vcsHostingService.getRepositoryNameFromUrl(url);
            final String owner = this.vcsHostingService.getRepositoryOwnerFromUrl(url);
            final Context context = this.reviewState.getContext();
            context.setOriginRepositoryName(repositoryName);
            context.setOriginRepositoryOwner(owner);
        } else {
            throw new OperationException("The remote URL doesn't match the VCS host");
        }
    }

    private Promise<HostUser> authenticate() {
        final RequestCall<HostUser> authenticateCall = new RequestCall<HostUser>() {
            @Override
            public void makeCall(final AsyncCallback<HostUser> callback) {
                authentifier.authenticate(callback);
            }
        };
        final Promise<HostUser> result = AsyncPromiseHelper.createFromAsyncRequest(authenticateCall);
        result.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(final PromiseError e) throws OperationException {
                onError(AbortReviewCause.NO_AUTH);
                throw new OperationException();
            }
        });

        return result;
    }

    private void onError(final AbortReviewCause cause) {
        Log.error(ReviewExtension.class, "Aborted review mode: " + cause);
    }
}
