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


import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.Remote;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

/**
 * Adds the forked remote repository to the remotes of the project.
 */
public class AddRemoteStep implements Step {

    /**
     * The local name of the forked repository remote.
     */
    private final static String FORK_REMOTE_NAME = "fork";

    /**
     * The service for VCS operations.
     */
    private final VcsService vcsService;

    /**
     * The factory used to create "wait" steps.
     */
    private final WaitForForOnRemoteStepFactory waitRemoteStepFactory;

    /**
     * The following step.
     */
    private final Step pushStep;

    /**
     * I18n-able messages.
     */
    private final ContributeMessages messages;

    /**
     * The remote repository host.
     */
    private final RepositoryHost repositoryHost;

    /**
     * Notification helper.
     */
    private final NotificationHelper notificationHelper;

    @Inject
    public AddRemoteStep(@Nonnull final VcsService vcsService,
                         @Nonnull final RepositoryHost repositoryHost,
                         @Nonnull final PushBranchOnForkStep pushStep,
                         @Nonnull final WaitForForOnRemoteStepFactory waitRemoteStepFactory,
                         @Nonnull final ContributeMessages messages,
                         @Nonnull final NotificationHelper notificationHelper) {
        this.vcsService = vcsService;
        this.repositoryHost = repositoryHost;
        this.pushStep = pushStep;
        this.waitRemoteStepFactory = waitRemoteStepFactory;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        final String remoteUrl = repositoryHost.makeRemoteUrl(context.getHostUserLogin(), context.getOriginRepositoryName());

        checkRemotePresent(context, config, remoteUrl);
    }

    private void checkRemotePresent(final Context context, final Configuration config, final String remoteUrl) {
        vcsService.listRemotes(context.getProject(), new AsyncCallback<List<Remote>>() {
            @Override
            public void onSuccess(final List<Remote> result) {
                for (final Remote remote : result) {
                    if (FORK_REMOTE_NAME.equals(remote.getName())) {
                        context.setForkedRemoteName(FORK_REMOTE_NAME);
                        if (remoteUrl.equals(remote.getUrl())) {
                            // all is correct, continue
                            proceed(context, config);
                        } else {
                            replaceRemote(context, config, remoteUrl);
                        }
                        // leave the method, do not go to addRemote(...)
                        return;
                    }
                }
                addRemote(context, config, remoteUrl);
            }

            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showWarning(messages.warnCheckRemote());
            }
        });
    }

    /**
     * Add the remote to the project.
     *
     * @param context
     *         the contribution context
     * @param config
     *         the contribution configuration
     * @param remoteUrl
     *         the url of the remote
     */
    private void addRemote(final Context context, final Configuration config, final String remoteUrl) {
        vcsService.addRemote(context.getProject(), FORK_REMOTE_NAME, remoteUrl, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void notUsed) {
                context.setForkedRemoteName(FORK_REMOTE_NAME);
                proceed(context, config);
            }

            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showError(AddRemoteStep.class, messages.errorAddRemoteFailed());
            }
        });
    }

    /**
     * Removes the fork remote from the project before adding it with the correct URL.
     *
     * @param context
     *         the contribution context
     * @param config
     *         the contribution configuration
     * @param remoteUrl
     *         the url of the remote
     */
    private void replaceRemote(final Context context, final Configuration config, final String remoteUrl) {
        vcsService.deleteRemote(context.getProject(), remoteUrl, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                addRemote(context, config, remoteUrl);
            }

            @Override
            public void onFailure(final Throwable caught) {
                notificationHelper.showError(AddRemoteStep.class, messages.errorRemoveRemoteFailed());
            }
        });
    }

    /**
     * Continue to the following step.
     *
     * @param context
     *         the contribution context
     * @param config
     *         the contribution configuration
     */
    private void proceed(final Context context, final Configuration config) {
        final Step waitStep = waitRemoteStepFactory.create(pushStep);
        waitStep.execute(context, config);
    }
}
