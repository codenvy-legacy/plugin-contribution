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
package com.codenvy.plugin.contribution.client.dialogs.commit;

import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import javax.annotation.Nonnull;

import static com.codenvy.ide.ext.git.client.GitRepositoryInitializer.isGitRepository;
import static com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CONTINUE;
import static com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.OK;

/**
 * This presenter provides base functionality to commit project changes or not before cloning or generating a factory url.
 *
 * @author Kevin Pollet
 */
public class CommitPresenter implements CommitView.ActionDelegate {

    private final CommitView          view;
    private final AppContext          appContext;
    private final VcsService          vcsService;
    private final NotificationHelper  notificationHelper;
    private       CommitActionHandler handler;

    @Inject
    public CommitPresenter(@Nonnull final CommitView view,
                           @Nonnull final AppContext appContext,
                           @Nonnull final VcsService vcsService,
                           @Nonnull final NotificationHelper notificationHelper) {
        this.view = view;
        this.appContext = appContext;
        this.vcsService = vcsService;
        this.notificationHelper = notificationHelper;

        this.view.setDelegate(this);
    }

    /**
     * Opens the {@link com.codenvy.plugin.contribution.client.dialogs.commit.CommitView}.
     *
     * @param commitDescription
     *         the default commit description.
     */
    public void showView(@Nonnull String commitDescription) {
        view.show(commitDescription);
    }

    /**
     * Sets the {@link com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter.CommitActionHandler} called after the ok or
     * continue action is
     * executed.
     *
     * @param handler
     *         the handler to set.
     */
    public void setCommitActionHandler(final CommitActionHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns if the current project has uncommitted changes.
     */
    public void hasUncommittedChanges(final AsyncCallback<Boolean> callback) {
        final CurrentProject project = appContext.getCurrentProject();
        if (project == null) {
            callback.onFailure(new IllegalStateException("No project opened"));

        } else if (!isGitRepository(project.getRootProject())) {
            callback.onFailure(new IllegalStateException("Opened project is not has no Git repository"));

        } else {
            vcsService.hasUncommittedChanges(project.getRootProject(), callback);
        }
    }

    @Override
    public void onOk() {
        final CurrentProject project = appContext.getCurrentProject();
        if (project != null) {
            vcsService.commit(project.getRootProject(), view.isIncludeUntracked(), view.getCommitDescription(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.showError(CommitPresenter.class, exception);
                }

                @Override
                public void onSuccess(final Void result) {
                    view.close();

                    if (handler != null) {
                        handler.onCommitAction(OK);
                    }
                }
            });
        }
    }

    @Override
    public void onContinue() {
        view.close();

        if (handler != null) {
            handler.onCommitAction(CONTINUE);
        }
    }

    @Override
    public void onCancel() {
        view.close();
    }

    @Override
    public void onCommitDescriptionChanged() {
        view.setOkButtonEnabled(!view.getCommitDescription().isEmpty());
    }

    public interface CommitActionHandler {
        /**
         * Called when a commit actions is done on the commit view.
         *
         * @param action
         *         the action.
         */
        void onCommitAction(CommitAction action);

        enum CommitAction {
            OK,
            CONTINUE
        }
    }
}
