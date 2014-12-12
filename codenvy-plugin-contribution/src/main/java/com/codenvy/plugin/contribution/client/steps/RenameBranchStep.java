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

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RenameBranchStep implements Step {

    private final NotificationManager notificationManager;
    private final PushBranchOnForkStep pushStep;
    private final VcsService vcsService;

    @Inject
    public RenameBranchStep(final @Nonnull PushBranchOnForkStep pushStep,
                            final @Nonnull VcsService vcsService,
                            final @Nonnull NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
        this.pushStep = pushStep;
        this.vcsService = vcsService;
    }

    public void execute(final Context context, final Configuration config) {
        this.vcsService.renameBranch(context.getProject(), context.getWorkBranchName(), config.getBranchName(),
                                     new AsyncCallback<Void>() {

                                         @Override
                                         public void onSuccess(final Void result) {
                                             pushStep.execute(context, config);
                                         }

                                         @Override
                                         public void onFailure(final Throwable caught) {
                                             notificationManager.showError("Could not rename branch");
                                         }
                                     });
    }
}
