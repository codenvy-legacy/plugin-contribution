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

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.git.client.GitServiceClient;
import com.codenvy.ide.ext.git.shared.Revision;
import com.codenvy.ide.ext.git.shared.Status;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.ide.websocket.WebSocketException;
import com.codenvy.ide.websocket.rest.RequestCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import static com.codenvy.ide.api.notification.Notification.Type.ERROR;
import static com.codenvy.ide.ext.git.client.GitRepositoryInitializer.isGitRepository;
import static com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CONTINUE;
import static com.codenvy.plugin.contribution.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.OK;

/**
 * This presenter provides base functionality to commit project changes or not before cloning or generating a factory url.
 *
 * @author Kevin Pollet
 */
public class CommitPresenter implements CommitView.ActionDelegate {

    private final CommitView             view;
    private final AppContext             appContext;
    private final GitServiceClient       gitServiceClient;
    private final NotificationManager    notificationManager;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private       CommitActionHandler    handler;

    @Inject
    public CommitPresenter(final CommitView view,
                           final AppContext appContext,
                           final GitServiceClient gitServiceClient,
                           final NotificationManager notificationManager,
                           final DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.view = view;
        this.appContext = appContext;
        this.gitServiceClient = gitServiceClient;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

        this.view.setDelegate(this);
        this.view.setOkButtonEnabled(false);
    }

    /**
     * Opens the {@link com.codenvy.plugin.contribution.client.dialogs.commit.CommitView}.
     */
    public void showView() {
        view.show();
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
            gitServiceClient.status(project.getRootProject(),
                                    new AsyncRequestCallback<Status>(dtoUnmarshallerFactory.newUnmarshaller(Status.class)) {
                                        @Override
                                        protected void onSuccess(final Status status) {
                                            callback.onSuccess(!status.isClean());
                                        }

                                        @Override
                                        protected void onFailure(final Throwable exception) {
                                            callback.onFailure(exception);
                                        }
                                    });
        }
    }

    @Override
    public void onOk() {
        final CurrentProject project = appContext.getCurrentProject();
        if (project != null) {
            final ProjectDescriptor projectDescriptor = project.getRootProject();
            try {

                gitServiceClient.add(projectDescriptor, false, null, new RequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void aVoid) {
                        gitServiceClient.commit(projectDescriptor, view.getCommitDescription(), true, false,
                                                new AsyncRequestCallback<Revision>() {
                                                    @Override
                                                    protected void onSuccess(final Revision revision) {
                                                        view.close();

                                                        if (handler != null) {
                                                            handler.onCommitAction(OK);
                                                        }
                                                    }

                                                    @Override
                                                    protected void onFailure(final Throwable exception) {
                                                        handleError(exception);
                                                    }
                                                });
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        handleError(exception);
                    }
                });

            } catch (WebSocketException exception) {
                handleError(exception);
            }
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
    public void onCommitDescriptionChanged() {
        view.setOkButtonEnabled(!view.getCommitDescription().isEmpty());
    }

    /**
     * Handles an exception and display the error message in a notification.
     *
     * @param exception
     *         the exception to handle.
     */
    private void handleError(final Throwable exception) {
        notificationManager.showNotification(new Notification(exception.getMessage(), ERROR));
        Log.error(CommitPresenter.class, exception.getMessage());
    }

    public interface CommitActionHandler {
        enum CommitAction {
            OK,
            CONTINUE
        }

        /**
         * Called when a commit actions is done on the commit view.
         *
         * @param action
         *         the action.
         */
        void onCommitAction(CommitAction action);
    }
}
