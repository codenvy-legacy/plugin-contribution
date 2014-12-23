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


import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.ContributeMessages;
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
     * The notification manager.
     */
    private final NotificationManager notificationManager;

    /**
     * I18n-able messages.
     */
    private final ContributeMessages messages;

    /**
     * The remote repository host.
     */
    private final RepositoryHost repositoryHost;

    @Inject
    public AddRemoteStep(final @Nonnull VcsService vcsService,
                         final @Nonnull RepositoryHost repositoryHost,
                         final @Nonnull PushBranchOnForkStep pushStep,
                         final @Nonnull WaitForForOnRemoteStepFactory waitRemoteStepFactory,
                         final @Nonnull NotificationManager notificationManager,
                         final @Nonnull ContributeMessages messages) {
        this.vcsService = vcsService;
        this.repositoryHost = repositoryHost;
        this.pushStep = pushStep;
        this.waitRemoteStepFactory = waitRemoteStepFactory;
        this.notificationManager = notificationManager;
        this.messages = messages;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        final String remoteUrl = this.repositoryHost.makeRemoteUrl(context.getHostUserLogin(), context.getOriginRepositoryName());

        checkRemotePresent(context, config, remoteUrl);
    }

    private void checkRemotePresent(final Context context, final Configuration config, final String remoteUrl) {
        this.vcsService.listRemotes(context.getProject(), new AsyncCallback<List<Remote>>() {
            @Override
            public void onSuccess(final List<Remote> result) {
                for (final Remote remote : result) {
                    if (FORK_REMOTE_NAME.equals(remote.getName())) {
                        Log.info(AddRemoteStep.class, messages.forekRemoteAlreadyPresent(FORK_REMOTE_NAME));
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
            public void onFailure(final Throwable caught) {
                notificationManager.showWarning(messages.warnCheckRemote());
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
        this.vcsService.addRemote(context.getProject(), FORK_REMOTE_NAME, remoteUrl, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void notUsed) {
                context.setForkedRemoteName(FORK_REMOTE_NAME);
                proceed(context, config);
            }

            @Override
            public void onFailure(final Throwable caught) {
                notificationManager.showError(messages.errorAddRemoteFailed());
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
        this.vcsService.deleteRemote(context.getProject(), remoteUrl, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                addRemote(context, config, remoteUrl);
            }

            @Override
            public void onFailure(final Throwable caught) {
                notificationManager.showError(messages.errorRemoveRemoteFailed());
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
        final Step waitStep = this.waitRemoteStepFactory.create(this.pushStep);
        waitStep.execute(context, config);
    }
}
