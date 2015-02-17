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
import com.codenvy.plugin.contribution.shared.client.SharedConstants;
import com.codenvy.plugin.contribution.vcs.client.Remote;
import com.codenvy.plugin.contribution.vcs.client.VcsService;
import com.codenvy.plugin.contribution.vcs.client.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.review.client.promises.Function;
import com.codenvy.plugin.review.client.promises.FunctionException;
import com.codenvy.plugin.review.client.promises.Operation;
import com.codenvy.plugin.review.client.promises.OperationException;
import com.codenvy.plugin.review.client.promises.Promise;
import com.codenvy.plugin.review.client.promises.PromiseError;
import com.codenvy.plugin.review.client.promises.internal.AsyncPromiseHelper;
import com.codenvy.plugin.review.client.promises.internal.AsyncPromiseHelper.RequestCall;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

@Singleton
@Extension(title = "Review", version = "1.0.0")
public class ReviewExtension implements ProjectActionHandler {
    private final ReviewMessages      messages;
    private final VcsHostingService   vcsHostingService;
    private final VcsServiceProvider  vcsServiceProvider;
    private final Provider<Context> contextProvider;

    @Inject
    public ReviewExtension(@Nonnull final EventBus eventBus,
                           @Nonnull final ReviewMessages messages,
                           @Nonnull final ReviewResources resources,
                           @Nonnull final Provider<Context> contextProvider,
                           @Nonnull final VcsHostingService vcsHostingService,
                           @Nonnull final VcsServiceProvider vcsServiceProvider) {
        this.messages = messages;
        this.vcsHostingService = vcsHostingService;
        this.vcsServiceProvider = vcsServiceProvider;
        this.contextProvider = contextProvider;

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

        if (vcsService != null
                && projectPermissions != null && projectPermissions.contains("write")
                && !(reviewAttr.isEmpty())
                && !(pullRequestIdAttr.isEmpty())) {
            Promise<Remote> remotePromise = retrieveUpstreamRemote(project);

            // if error handling needed, it's here - for the moment jsut bail out, nothing has started
            remotePromise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(final PromiseError error) {
                    Log.info(ReviewExtension.class, "Review flag set, pull request id present but no upstream remote found");
                    Log.debug(ReviewExtension.class, error.toString());
                    onError(AbortReviewCause.NO_UPSTREAM_REMOTE);
                }
            });

            Promise<Context> repositoryPromise = remotePromise.then(new Function<Remote, Context>() {
                @Override
                public Context apply(final Remote remote) throws FunctionException {
                    final Context context = setupUserAndRepository(remote);
                    if (context == null) {
                        throw new FunctionException("The remote URL doesn't match the VCS host");
                    } else {
                        return context;
                    }
                }
            });
            repositoryPromise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(final PromiseError arg) throws OperationException {
                    onError(AbortReviewCause.REMOTE_URL_HOSTING_MISMATCH);
                }
            });
        }
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

    private Context setupUserAndRepository(final Remote remote) {
        String url = remote.getUrl();
        if (this.vcsHostingService.isVcsHostRemoteUrl(url)) {
            final String repositoryName = this.vcsHostingService.getRepositoryNameFromUrl(url);
            final String owner = this.vcsHostingService.getRepositoryOwnerFromUrl(url);
            final Context result = this.contextProvider.get();
            result.setOriginRepositoryName(repositoryName);
            result.setOriginRepositoryOwner(owner);
            return result;
        } else {
            return null;
        }
    }

    private void onError(final AbortReviewCause cause) {

    }
}
