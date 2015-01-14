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

import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * This step allow the user to commit the current working tree if the git repository status is not clean.
 *
 * @author Kevin Pollet
 */
public class CommitWorkingTreeStep implements Step {
    private final CommitPresenter               commitPresenter;
    private final NotificationHelper            notificationHelper;
    private final AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep;

    @Inject
    public CommitWorkingTreeStep(@Nonnull final CommitPresenter commitPresenter,
                                 @Nonnull final NotificationHelper notificationHelper,
                                 @Nonnull final AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep) {
        this.commitPresenter = commitPresenter;
        this.notificationHelper = notificationHelper;
        this.authorizeCodenvyOnVCSHostStep = authorizeCodenvyOnVCSHostStep;
    }

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        workflow.setStep(authorizeCodenvyOnVCSHostStep);

        commitPresenter.setCommitActionHandler(new CommitPresenter.CommitActionHandler() {
            @Override
            public void onCommitAction(CommitAction action) {
                workflow.executeStep();
            }
        });

        commitPresenter.hasUncommittedChanges(new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showError(CommitWorkingTreeStep.class, exception);
            }

            @Override
            public void onSuccess(final Boolean hasUncommittedChanges) {
                if (hasUncommittedChanges) {
                    commitPresenter.showView();

                } else {
                    workflow.executeStep();
                }
            }
        });
    }
}
