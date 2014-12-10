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

import java.util.List;
import java.util.Map;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.constraints.Anchor;
import com.codenvy.ide.api.constraints.Constraints;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Stephane Tournie
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributorExtension {

    private final ActionManager    actionManager;
    private final ContributeAction contributeAction;
    private final ContributorLocalizationConstant localConstant;

    private DefaultActionGroup     contributeToolbarGroup;
    private DefaultActionGroup     mainToolbarGroup;

    @Inject
    public ContributorExtension(EventBus eventBus,
                                ActionManager actionManager,
                                ContributeAction contributeAction,
                                ProjectServiceClient projectServiceClient,
                                ContributorLocalizationConstant localConstant) {

        this.actionManager = actionManager;
        this.contributeAction = contributeAction;
        this.localConstant = localConstant;

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

        Map<String, List<String>> attributes = event.getProject().getAttributes();

        if (attributes != null && attributes.containsKey("contribute")) {
            String contributeAttribute = attributes.get("contribute").get(0);
            if (Integer.parseInt(contributeAttribute) == 1) {

                // register & display contribute button
                actionManager.registerAction(localConstant.contributorButtonName(), contributeAction);
                mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_TOOLBAR);
                contributeToolbarGroup = new DefaultActionGroup(GROUP_MAIN_TOOLBAR, false, actionManager);
                actionManager.registerAction(GROUP_MAIN_TOOLBAR, contributeToolbarGroup);
                contributeToolbarGroup.add(contributeAction);
                mainToolbarGroup.add(contributeToolbarGroup, new Constraints(Anchor.AFTER, GROUP_RUN_TOOLBAR));
            }
        }
    }

    private void exitContributeMode() {
        actionManager.unregisterAction(localConstant.contributorButtonName());
        if (mainToolbarGroup != null && contributeToolbarGroup != null) {
            mainToolbarGroup.remove(contributeToolbarGroup);
        }
    }
}
