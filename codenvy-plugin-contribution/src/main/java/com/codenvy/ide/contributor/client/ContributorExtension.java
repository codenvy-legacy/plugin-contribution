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
package com.codenvy.ide.contributor.client;

import static com.codenvy.ide.api.action.IdeActions.GROUP_MAIN_TOOLBAR;
import static com.codenvy.ide.api.action.IdeActions.GROUP_RUN_TOOLBAR;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.constraints.Anchor;
import com.codenvy.ide.api.constraints.Constraints;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.Notification.Status;
import com.codenvy.ide.api.notification.Notification.Type;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Stephane Tournie
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributorExtension {

    private final ActionManager                   actionManager;
    private final ContributeAction                contributeAction;
    private final ContributorLocalizationConstant localConstant;
    private final NotificationManager             notificationManager;
    private final GitAgent                        gitAgent;

    private DefaultActionGroup                    contributeToolbarGroup;
    private DefaultActionGroup                    mainToolbarGroup;

    @Inject
    public ContributorExtension(EventBus eventBus,
                                ActionManager actionManager,
                                ContributeAction contributeAction,
                                ProjectServiceClient projectServiceClient,
                                ContributorLocalizationConstant localConstant,
                                NotificationManager notificationManager,
                                GitAgent gitAgent) {

        this.actionManager = actionManager;
        this.contributeAction = contributeAction;
        this.localConstant = localConstant;
        this.notificationManager = notificationManager;
        this.gitAgent = gitAgent;

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                initContributeMode(event);
            }

            @Override
            public void onProjectClosed(ProjectActionEvent projectActionEvent) {
                exitContributeMode();
            }
        });
    }

    /**
     * Initialize contribution button & operations
     *
     * @param event the load event
     */
    private void initContributeMode(ProjectActionEvent event) {

        ProjectDescriptor project = event.getProject();
        Map<String, List<String>> attributes = event.getProject().getAttributes();

        if (attributes != null && attributes.containsKey("contribute")) {
            String contributeAttribute = attributes.get("contribute").get(0);
            if (contributeAttribute.equals("true")) {

                // branch specified in factory.json has been already checkout at this point
                // register & display contribute button
                actionManager.registerAction(localConstant.contributorButtonName(), contributeAction);
                mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_TOOLBAR);
                contributeToolbarGroup = new DefaultActionGroup(GROUP_MAIN_TOOLBAR, false, actionManager);
                actionManager.registerAction(GROUP_MAIN_TOOLBAR, contributeToolbarGroup);
                contributeToolbarGroup.add(contributeAction);
                mainToolbarGroup.add(contributeToolbarGroup, new Constraints(Anchor.AFTER, GROUP_RUN_TOOLBAR));

                // TODO use hash of cloned branch instead of a random number
                Date today = new Date();
                DateTimeFormat timeFormat = DateTimeFormat.getFormat("MMddyyyy");
                final String workingBranchName = "contrib-" + timeFormat.format(today) + "-" + String.valueOf(Math.random()).substring(8);
                final Notification notification = new Notification("Creating a new working branch " + workingBranchName + "...", Type.INFO,
                                                                   Status.PROGRESS);
                notificationManager.showNotification(notification);
                // shorthand for create + checkout new temporary working branch -> checkout -b branchName
                gitAgent.checkoutBranch(project, workingBranchName, true, new AsyncRequestCallback<String>() {

                    @Override
                    protected void onSuccess(String result) {
                        notification.setMessage("Branch " + workingBranchName + " successfully created and checkout.");
                        notification.setStatus(Status.FINISHED);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notification.setMessage("Failed to create branch " + workingBranchName + ".");
                        notification.setType(Type.ERROR);
                        notification.setStatus(Status.FINISHED);
                        Log.error(ContributorExtension.class, exception.getMessage());
                    }
                });
            }
        }
    }

    private void exitContributeMode() {
        // remove contribute button
        actionManager.unregisterAction(localConstant.contributorButtonName());
        if (mainToolbarGroup != null && contributeToolbarGroup != null) {
            mainToolbarGroup.remove(contributeToolbarGroup);
        }
    }
}
