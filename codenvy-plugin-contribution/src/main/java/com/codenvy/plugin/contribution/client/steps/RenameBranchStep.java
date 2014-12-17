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

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.Branch;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provider;

public class RenameBranchStep implements Step {

    private final NotificationManager notificationManager;
    private final DialogFactory dialogFactory;
    private final PushBranchOnForkStep pushStep;
    private final Provider<ConfigureStep> configureStepProvider;
    private final VcsService vcsService;
    private final ContributeMessages messages;
    private final WaitForForOnRemoteStepFactory waitRemoteStepFactory;

    @Inject
    public RenameBranchStep(final @Nonnull PushBranchOnForkStep pushStep,
                            final @Nonnull Provider<ConfigureStep> configureStepProvider,
                            final @Nonnull VcsService vcsService,
                            final @Nonnull DialogFactory dialogFactory,
                            final @Nonnull NotificationManager notificationManager,
                            final @Nonnull ContributeMessages messages,
                            final @Nonnull WaitForForOnRemoteStepFactory waitRemoteStepFactory) {
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.pushStep = pushStep;
        this.configureStepProvider = configureStepProvider;
        this.vcsService = vcsService;
        this.messages = messages;
        this.waitRemoteStepFactory = waitRemoteStepFactory;
    }

    public void execute(final Context context, final Configuration config) {
        if (config == null || config.getBranchName() == null || "".equals(config.getBranchName())) {
            final ConfirmCallback callback = new ConfirmCallback() {
                @Override
                public void accepted() {
                    configureStepProvider.get().execute(context, config);
                }
            };
            this.dialogFactory.createMessageDialog(messages.warnMissingConfigTitle(),
                                                   messages.warnBranchEmpty(),
                                                   callback);
            return;
        }
        final String newBranchName = config.getBranchName();
        if (newBranchName.equals(context.getWorkBranchName())) {
            // already done, proceed
            proceed(context, config);
        } else {
            checkExistAndRename(newBranchName, context, config);
        }
    }

    private void proceed(final Context context, final Configuration config) {
        final Step waitStep = this.waitRemoteStepFactory.create(this.pushStep);
        waitStep.execute(context, config);
    }

    private void checkExistAndRename(final String branchName, final Context context, final Configuration config) {
        this.vcsService.listLocalBranches(context.getProject(), new AsyncCallback<List<Branch>>() {
            @Override
            public void onSuccess(final List<Branch> result) {
                for (final Branch branch : result) {
                    if (branch.getDisplayName().equals(branchName)) {
                        // the branch exists
                        final ConfirmCallback callback = new ConfirmCallback() {
                            @Override
                            public void accepted() {
                                configureStepProvider.get().execute(context, config);
                            }
                        };
                        dialogFactory.createMessageDialog(messages.warnMissingConfigTitle(),
                                                          messages.errorBranchExists(branchName),
                                                          callback);
                    }
                }
                doRename(branchName, context, config);

            }
            @Override
            public void onFailure(final Throwable caught) {
                notificationManager.showError(messages.errorListBranches());
            }
        });
    }

    private void doRename(final String branchName, final Context context, final Configuration config) {
        this.vcsService.renameBranch(context.getProject(), context.getWorkBranchName(), branchName, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                notificationManager.showInfo(messages.infoRenamedBranch(branchName));
                context.setWorkBranchName(branchName);
                proceed(context, config);
            }
            @Override
            public void onFailure(final Throwable caught) {
                notificationManager.showError(messages.errorRenameFailed());
            }
        });

    }
}
