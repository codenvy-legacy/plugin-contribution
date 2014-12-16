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
package com.codenvy.plugin.contribution.client;

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
import com.codenvy.ide.util.loging.Log;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.api.action.IdeActions.GROUP_MAIN_TOOLBAR;
import static com.codenvy.ide.api.action.IdeActions.GROUP_RUN_TOOLBAR;

/**
 * @author Stephane Tournie
 * @author Kevin Pollet
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributorExtension {
    private static final String ATTRIBUTE_CONTRIBUTE_KEY   = "contribute";
    private static final String WORKING_BRANCH_NAME_PREFIX = "contrib-";

    private final ActionManager       actionManager;
    private final Context             context;
    private final ContributeAction    contributeAction;
    private final ContributeMessages  messages;
    private final NotificationManager notificationManager;
    private final VcsService          vcsService;

    private DefaultActionGroup contributeToolbarGroup;
    private DefaultActionGroup mainToolbarGroup;

    @Inject
    public ContributorExtension(final Context context,
                                final EventBus eventBus,
                                final ActionManager actionManager,
                                final ContributeAction contributeAction,
                                final ContributeMessages messages,
                                final NotificationManager notificationManager,
                                final VcsService gitAgent) {

        this.actionManager = actionManager;
        this.context = context;
        this.contributeAction = contributeAction;
        this.messages = messages;
        this.notificationManager = notificationManager;
        this.vcsService = gitAgent;

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(final ProjectActionEvent event) {
                initContributeMode(event);
            }

            @Override
            public void onProjectClosed(final ProjectActionEvent projectActionEvent) {
                exitContributeMode();
            }
        });
    }

    /**
     * Initialize contribution button & operations.
     *
     * @param event
     *         the load event.
     */
    private void initContributeMode(final ProjectActionEvent event) {
        final ProjectDescriptor project = event.getProject();
        context.setProject(project);

        final Map<String, List<String>> attributes = event.getProject().getAttributes();
        if (attributes != null && attributes.containsKey(ATTRIBUTE_CONTRIBUTE_KEY)) {

            final String contributeAttribute = attributes.get(ATTRIBUTE_CONTRIBUTE_KEY).get(0);
            if ("true".equalsIgnoreCase(contributeAttribute)) {

                // branch specified in factory.json has been already checkout at this point
                // register & display contribute button
                actionManager.registerAction(messages.contributorButtonName(), contributeAction);
                mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_TOOLBAR);
                contributeToolbarGroup = new DefaultActionGroup(GROUP_MAIN_TOOLBAR, false, actionManager);
                contributeToolbarGroup.add(contributeAction);
                mainToolbarGroup.add(contributeToolbarGroup, new Constraints(Anchor.AFTER, GROUP_RUN_TOOLBAR));

                final String workingBranchName = generateWorkingBranchName();
                context.setWorkBranchName(workingBranchName);
                final Notification notification = new Notification("Creating a new working branch " + workingBranchName + "...", Type.INFO,
                                                                   Status.PROGRESS);
                notificationManager.showNotification(notification);
                // shorthand for create + checkout new temporary working branch -> checkout -b branchName
                vcsService.checkoutBranch(project, workingBranchName, true, new AsyncCallback<String>() {

                    @Override
                    public void onSuccess(final String result) {
                        notification.setMessage("Branch " + workingBranchName + " successfully created and checked out.");
                        notification.setStatus(Status.FINISHED);
                    }

                    @Override
                    public void onFailure(final Throwable exception) {
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
        actionManager.unregisterAction(messages.contributorButtonName());
        if (mainToolbarGroup != null && contributeToolbarGroup != null) {
            mainToolbarGroup.remove(contributeToolbarGroup);
        }
    }

    /**
     * Generates the working branch name used for the contribution.
     *
     * @return the working branch name, never {@code null}.
     */
    private String generateWorkingBranchName() {
        final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMddyyyy");
        return WORKING_BRANCH_NAME_PREFIX + dateTimeFormat.format(new Date());
    }
}
