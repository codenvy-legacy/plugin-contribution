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

import com.codenvy.api.factory.dto.Factory;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.client.vcs.Branch;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcs.VcsServiceProvider;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.INFO;
import static com.codenvy.plugin.contribution.client.ContributeConstants.ATTRIBUTE_CONTRIBUTE_KEY;

/**
 * This step defines the working branch for the user contribution.
 * <ul>
 * <li>If the user comes from a contribution factory the contribution branch has to be created automatically.
 * <li>If the project is cloned from GitHub the contribution branch is the current one.
 * </ul>
 * <p/>
 * The next step is executed when the user click on the contribute/update
 * button. See {@link com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter#onContribute()}
 *
 * @author Kevin Pollet
 */
public class DefineWorkBranchStep implements Step {
    private static final String GENERATED_WORKING_BRANCH_NAME_PREFIX = "contrib-";

    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;
    private final VcsServiceProvider vcsServiceProvider;
    private final AppContext         appContext;

    @Inject
    public DefineWorkBranchStep(@Nonnull final ContributeMessages messages,
                                @Nonnull final NotificationHelper notificationHelper,
                                @Nonnull final VcsServiceProvider vcsServiceProvider,
                                @Nonnull final AppContext appContext) {
        this.messages = messages;
        this.notificationHelper = notificationHelper;
        this.vcsServiceProvider = vcsServiceProvider;
        this.appContext = appContext;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        final Context context = workflow.getContext();
        final Factory factory = appContext.getFactory();
        final VcsService vcsService = vcsServiceProvider.getVcsService();

        // if we come from a factory we have to create the working branch
        if (factory != null && factory.getProject().getAttributes().containsKey(ATTRIBUTE_CONTRIBUTE_KEY)) {
            final String workingBranchName = generateWorkBranchName();
            final Notification createWorkingBranchNotification =
                    new Notification(messages.stepDefineWorkBranchCreatingWorkBranch(workingBranchName), INFO, PROGRESS);
            notificationHelper.showNotification(createWorkingBranchNotification);

            // the working branch is only created if it doesn't exist
            vcsService.listLocalBranches(context.getProject(), new AsyncCallback<List<Branch>>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.finishNotificationWithError(DefineWorkBranchStep.class, exception, createWorkingBranchNotification);
                }

                @Override
                public void onSuccess(final List<Branch> branches) {
                    boolean workingBranchExists = false;

                    for (final Branch oneBranch : branches) {
                        if (workingBranchName.equals(oneBranch.getDisplayName())) {
                            workingBranchExists = true;
                            break;
                        }
                    }

                    // shorthand for create + checkout new temporary working branch -> checkout -b branchName
                    vcsService.checkoutBranch(context.getProject(), workingBranchName, !workingBranchExists, new AsyncCallback<String>() {
                        @Override
                        public void onSuccess(final String result) {
                            context.setWorkBranchName(workingBranchName);
                            notificationHelper.finishNotification(messages.stepDefineWorkBranchWorkBranchCreated(workingBranchName),
                                                                  createWorkingBranchNotification);
                        }

                        @Override
                        public void onFailure(final Throwable exception) {
                            notificationHelper.finishNotificationWithError(DefineWorkBranchStep.class, exception,
                                                                           createWorkingBranchNotification);
                        }
                    });
                }
            });

            // if it's a github project the working branch is the current one
        } else {
            vcsService.getBranchName(context.getProject(), new AsyncCallback<String>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.showError(DefineWorkBranchStep.class, exception);
                }

                @Override
                public void onSuccess(final String branchName) {
                    context.setWorkBranchName(branchName);
                }
            });
        }
    }

    /**
     * Generates the work branch name used for the contribution.
     *
     * @return the work branch name, never {@code null}.
     */
    private String generateWorkBranchName() {
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMddyyyy");
        return GENERATED_WORKING_BRANCH_NAME_PREFIX + dateTimeFormat.format(new Date());
    }
}
