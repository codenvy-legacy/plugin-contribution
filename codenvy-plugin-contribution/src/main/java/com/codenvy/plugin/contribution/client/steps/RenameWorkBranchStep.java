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
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.Branch;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

/**
 * Renames the current branch with the one provided by the user.
 */
public class RenameWorkBranchStep implements Step {
    private final DialogFactory      dialogFactory;
    private final Step               addForkRemoteStep;
    private final VcsService         vcsService;
    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;

    @Inject
    public RenameWorkBranchStep(@Nonnull final AddForkRemoteStep addForkRemoteStep,
                                @Nonnull final VcsService vcsService,
                                @Nonnull final DialogFactory dialogFactory,
                                @Nonnull final ContributeMessages messages,
                                @Nonnull final NotificationHelper notificationHelper,
                                @Nonnull final WaitForkOnRemoteStepFactory waitRemoteStepFactory) {
        this.dialogFactory = dialogFactory;
        this.addForkRemoteStep = waitRemoteStepFactory.create(addForkRemoteStep);
        this.vcsService = vcsService;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final String newBranchName = workflow.getConfiguration().getBranchName();

        if (newBranchName.equals(context.getWorkBranchName())) {
            workflow.setStep(addForkRemoteStep);
            workflow.executeStep();

        } else {
            checkExistAndRename(workflow, newBranchName, context);
        }
    }

    /**
     * Check if the branch exists and either do the rename or return to configuration.
     *
     * @param branchName
     *         the provided branch name
     * @param context
     *         the contribution context
     */
    private void checkExistAndRename(final ContributorWorkflow workflow, final String branchName, final Context context) {
        vcsService.listLocalBranches(context.getProject(), new AsyncCallback<List<Branch>>() {
            @Override
            public void onSuccess(final List<Branch> result) {
                for (final Branch branch : result) {
                    if (branch.getDisplayName().equals(branchName)) {
                        // the branch exists
                        dialogFactory.createMessageDialog(messages.stepRenameWorkBranchMissingConfigTitle(),
                                                          messages.stepRenameWorkBranchErrorLocalBranchExists(branchName),
                                                          new ConfirmCallback() {
                                                              @Override
                                                              public void accepted() {
                                                                  //TODO open the contribute part with branch name in error
                                                                  //TODO maybe it's better to check if the branch exist before contribute click
                                                              }
                                                          });
                    }
                }
                doRename(workflow, branchName, context);

            }

            @Override
            public void onFailure(final Throwable caught) {
                notificationHelper.showError(RenameWorkBranchStep.class, messages.stepRenameWorkBranchErrorListLocalBranches());
            }
        });
    }

    private void doRename(final ContributorWorkflow workflow, final String branchName, final Context context) {
        vcsService.renameBranch(context.getProject(), context.getWorkBranchName(), branchName, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                notificationHelper.showInfo(messages.stepRenameWorkBranchLocalBranchRenamed(branchName));
                context.setWorkBranchName(branchName);

                workflow.setStep(addForkRemoteStep);
                workflow.executeStep();
            }

            @Override
            public void onFailure(final Throwable caught) {
                notificationHelper.showError(RenameWorkBranchStep.class, messages.stepRenameWorkBranchErrorRenameLocalBranch());
            }
        });
    }
}
