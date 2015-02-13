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
package com.codenvy.plugin.contribution.client.steps;


import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.Remote;
import com.codenvy.plugin.contribution.vcs.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.hosting.VcsHostingService;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import java.util.List;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.ADD_FORK_REMOTE;

/**
 * Adds the forked remote repository to the remotes of the project.
 */
public class AddForkRemoteStep implements Step {
    private final static String ORIGIN_REMOTE_NAME = "origin";
    private final static String FORK_REMOTE_NAME   = "fork";

    private final VcsServiceProvider vcsServiceProvider;
    private final Step               pushBranchOnForkStep;
    private final ContributeMessages messages;
    private final VcsHostingService  vcsHostingService;
    private final NotificationHelper notificationHelper;

    @Inject
    public AddForkRemoteStep(@Nonnull final VcsServiceProvider vcsServiceProvider,
                             @Nonnull final VcsHostingService vcsHostingService,
                             @Nonnull final PushBranchOnForkStep pushBranchOnForkStep,
                             @Nonnull final ContributeMessages messages,
                             @Nonnull final NotificationHelper notificationHelper) {
        this.vcsServiceProvider = vcsServiceProvider;
        this.vcsHostingService = vcsHostingService;
        this.pushBranchOnForkStep = pushBranchOnForkStep;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final String originRepositoryOwner = context.getOriginRepositoryOwner();
        final String originRepositoryName = context.getOriginRepositoryName();
        final String upstreamRepositoryOwner = context.getUpstreamRepositoryOwner();
        final String upstreamRepositoryName = context.getUpstreamRepositoryName();

        // the fork remote has to be added only if we cloned the upstream else it's origin
        if (originRepositoryOwner.equalsIgnoreCase(upstreamRepositoryOwner) &&
            originRepositoryName.equalsIgnoreCase(upstreamRepositoryName)) {
            final String remoteUrl = vcsHostingService.makeSSHRemoteUrl(context.getHostUserLogin(), context.getForkedRepositoryName());
            checkRemotePresent(workflow, remoteUrl);

        } else {
            context.setForkedRemoteName(ORIGIN_REMOTE_NAME);
            proceed(workflow);
        }
    }

    private void checkRemotePresent(final ContributorWorkflow workflow, final String remoteUrl) {
        final Context context = workflow.getContext();

        vcsServiceProvider.getVcsService()
                          .listRemotes(workflow.getContext().getProject(), new AsyncCallback<List<Remote>>() {
                              @Override
                              public void onSuccess(final List<Remote> result) {
                                  for (final Remote remote : result) {
                                      if (FORK_REMOTE_NAME.equals(remote.getName())) {
                                          context.setForkedRemoteName(FORK_REMOTE_NAME);
                                          if (remoteUrl.equals(remote.getUrl())) {
                                              // all is correct, continue
                                              proceed(workflow);

                                          } else {
                                              replaceRemote(workflow, remoteUrl);
                                          }
                                          // leave the method, do not go to addRemote(...)
                                          return;
                                      }
                                  }
                                  addRemote(workflow, remoteUrl);
                              }

                              @Override
                              public void onFailure(final Throwable exception) {
                                  workflow.fireStepErrorEvent(ADD_FORK_REMOTE);
                                  notificationHelper.showWarning(messages.stepAddForkRemoteErrorCheckRemote());
                              }
                          });
    }

    /**
     * Add the remote to the project.
     *
     * @param workflow
     *         the {@link com.codenvy.plugin.contribution.client.steps.ContributorWorkflow}.
     * @param remoteUrl
     *         the url of the remote
     */
    private void addRemote(final ContributorWorkflow workflow, final String remoteUrl) {
        final Context context = workflow.getContext();

        vcsServiceProvider.getVcsService()
                          .addRemote(context.getProject(), FORK_REMOTE_NAME, remoteUrl, new AsyncCallback<Void>() {
                              @Override
                              public void onSuccess(final Void notUsed) {
                                  context.setForkedRemoteName(FORK_REMOTE_NAME);

                                  proceed(workflow);
                              }

                              @Override
                              public void onFailure(final Throwable exception) {
                                  workflow.fireStepErrorEvent(ADD_FORK_REMOTE);
                                  notificationHelper.showError(AddForkRemoteStep.class, messages.stepAddForkRemoteErrorAddFork());
                              }
                          });
    }

    /**
     * Removes the fork remote from the project before adding it with the correct URL.
     *
     * @param workflow
     *         the {@link com.codenvy.plugin.contribution.client.steps.ContributorWorkflow}.
     * @param remoteUrl
     *         the url of the remote
     */
    private void replaceRemote(final ContributorWorkflow workflow, final String remoteUrl) {
        final Context context = workflow.getContext();

        vcsServiceProvider.getVcsService()
                          .deleteRemote(context.getProject(), FORK_REMOTE_NAME, new AsyncCallback<Void>() {
                              @Override
                              public void onSuccess(final Void result) {
                                  addRemote(workflow, remoteUrl);
                              }

                              @Override
                              public void onFailure(final Throwable caught) {
                                  workflow.fireStepErrorEvent(ADD_FORK_REMOTE);
                                  notificationHelper
                                          .showError(AddForkRemoteStep.class, messages.stepAddForkRemoteErrorSetForkedRepositoryRemote());
                              }
                          });
    }

    private void proceed(final ContributorWorkflow workflow) {
        workflow.fireStepDoneEvent(ADD_FORK_REMOTE);
        workflow.setStep(pushBranchOnForkStep);
        workflow.executeStep();
    }
}
