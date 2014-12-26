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

import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.Branch;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Provider;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

/**
 * Renames the current branch with the one provided by the user.
 */
public class RenameBranchStep implements Step {
    /**
     * Factory for dialogs.
     */
    private final DialogFactory dialogFactory;

    /**
     * The following step.
     */
    private final Step nextStep;

    /**
     * A provider for configure steps. Needed in case the branch name must be asked again.<br>
     * Also, inject a provider so there is no dependency cycle.
     */
    private final Provider<ConfigureStep> configureStepProvider;

    /**
     * The service for VCS operations.
     */
    private final VcsService vcsService;

    /**
     * I18n-able messages.
     */
    private final ContributeMessages messages;

    /**
     * Helper to work with notification.
     */
    private final NotificationHelper notificationHelper;

    @Inject
    public RenameBranchStep(@Nonnull final AddRemoteStep addRemoteStep,
                            @Nonnull final Provider<ConfigureStep> configureStepProvider,
                            @Nonnull final VcsService vcsService,
                            @Nonnull final DialogFactory dialogFactory,
                            @Nonnull final ContributeMessages messages,
                            @Nonnull final NotificationHelper notificationHelper) {
        this.dialogFactory = dialogFactory;
        this.nextStep = addRemoteStep;
        this.configureStepProvider = configureStepProvider;
        this.vcsService = vcsService;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        if (config == null || config.getBranchName() == null || "".equals(config.getBranchName())) {
            final ConfirmCallback callback = new ConfirmCallback() {
                @Override
                public void accepted() {
                    configureStepProvider.get().execute(context, config);
                }
            };
            dialogFactory.createMessageDialog(messages.warnMissingConfigTitle(),
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

    /**
     * Continue with the folloing step.
     *
     * @param context
     *         the contribution context
     * @param config
     *         the configuration
     */
    private void proceed(final Context context, final Configuration config) {
        nextStep.execute(context, config);
    }

    /**
     * Check if the branch exists and either do the rename or return to configuration.
     *
     * @param branchName
     *         the provided branch name
     * @param context
     *         the contribution context
     * @param config
     *         the contribution configuration
     */
    private void checkExistAndRename(final String branchName, final Context context, final Configuration config) {
        vcsService.listLocalBranches(context.getProject(), new AsyncCallback<List<Branch>>() {
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
                notificationHelper.showError(RenameBranchStep.class, messages.errorListBranches());
            }
        });
    }

    /**
     * Rename the branch.
     *
     * @param branchName
     *         the provided name
     * @param context
     *         the contribution context
     * @param config
     *         the contribution configuration
     */
    private void doRename(final String branchName, final Context context, final Configuration config) {
        vcsService.renameBranch(context.getProject(), context.getWorkBranchName(), branchName, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                notificationHelper.showInfo(messages.infoRenamedBranch(branchName));
                context.setWorkBranchName(branchName);
                proceed(context, config);
            }

            @Override
            public void onFailure(final Throwable caught) {
                notificationHelper.showError(RenameBranchStep.class, messages.errorRenameFailed());
            }
        });

    }
}
