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
package com.codenvy.plugin.contribution.client.steps;

import javax.inject.Inject;

import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Adds a factory link to the contribution in a comment of the pull request.
 */
public class AddFactoryLinkStep implements Step {

    /**
     * The following step.
     */
    private final Step nextStep;

    /**
     * The remote VCS repository.
     */
    private final RepositoryHost repository;

    /**
     * The i18n-able messages.
     */
    private final ContributeMessages messages;

    /**
     * The notification manager.
     */
    private final NotificationManager notificationManager;

    /**
     * Unmarshaller for DTOs.
     */
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public AddFactoryLinkStep(final ProposePersistStep nextStep,
                              final RepositoryHost repositoryHost,
                              final ContributeMessages messages,
                              final NotificationManager notificationManager,
                              final DtoFactory dtoFactory,
                              final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                              final AppContext appContext) {
        this.messages = messages;
        this.notificationManager = notificationManager;
        this.nextStep = nextStep;
        this.repository = repositoryHost;

        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        if (context.getReviewFactoryUrl() == null) {
            Log.debug(AddFactoryLinkStep.class, "Not factory Url; continue to next step.");
            proceed(context, config);
        } else {
            // post the comment
            sendComment(context, config, context.getReviewFactoryUrl());
        }
    }

    /**
     * Post the comment in the pull request.
     * 
     * @param context the context of the contribution
     * @param config the configuration of the contribution
     * @param factoryUrl the factory URL to include in the comment
     */
    private void sendComment(final Context context, final Configuration config, final String factoryUrl) {
        final String commentText = messages.pullRequestlinkComment(factoryUrl);
        this.repository.commentPullRequest(context.getOriginRepositoryOwner(), context.getOriginRepositoryName(),
                                           context.getPullRequestId(), commentText, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void notUsed) {
                proceed(context, config);
            }
            @Override
            public void onFailure(final Throwable caught) {
                notificationManager.showWarning(messages.warnPostFactoryLinkFailed(factoryUrl));
                // continue anyway, this is not a hard failure
                proceed(context, config);
            }
        });
    }

    /**
     * Continue to the following step.
     * 
     * @param context the context of the contribution
     * @param config the configuration of the contribution
     */
    private void proceed(final Context context, final Configuration config) {
        this.nextStep.execute(context, config);
    }

}
