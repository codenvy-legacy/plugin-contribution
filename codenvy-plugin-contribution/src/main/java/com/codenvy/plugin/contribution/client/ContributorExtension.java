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

import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.api.project.shared.dto.ProjectUpdate;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.plugin.contribution.client.parts.contribute.ContributePartPresenter;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import java.util.List;
import java.util.Map;

import static com.codenvy.ide.ext.git.client.GitRepositoryInitializer.isGitRepository;
import static com.codenvy.plugin.contribution.client.ContributeConstants.ATTRIBUTE_CONTRIBUTE_BRANCH;
import static com.codenvy.plugin.contribution.client.ContributeConstants.ATTRIBUTE_CONTRIBUTE_KEY;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;

/**
 * @author Stephane Tournie
 * @author Kevin Pollet
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributorExtension {
    private final ContributeMessages      messages;
    private final AppContext              appContext;
    private final NotificationHelper      notificationHelper;
    private final ContributePartPresenter contributePartPresenter;
    private final ProjectServiceClient    projectService;
    private final DtoFactory              dtoFactory;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final ContributorWorkflow     workflow;

    @Inject
    public ContributorExtension(final EventBus eventBus,
                                final ContributeMessages messages,
                                final ContributeResources resources,
                                final AppContext appContext,
                                final NotificationHelper notificationHelper,
                                final ContributePartPresenter contributePartPresenter,
                                final ProjectServiceClient projectService,
                                final DtoFactory dtoFactory,
                                final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                final ContributorWorkflow workflow) {
        this.messages = messages;
        this.workflow = workflow;
        this.appContext = appContext;
        this.notificationHelper = notificationHelper;
        this.contributePartPresenter = contributePartPresenter;
        this.projectService = projectService;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

        resources.contributeCss().ensureInjected();

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(final ProjectActionEvent event) {
                preInitContributeMode(event);
            }

            @Override
            public void onProjectClosed(final ProjectActionEvent projectActionEvent) {
                exitContributeMode();
            }
        });
    }


    private void preInitContributeMode(final ProjectActionEvent event) {
        final ProjectDescriptor project = event.getProject();

        if (appContext.getFactory() != null) {
            initContributeModeWithFactory(appContext.getFactory(), project);
        } else {
            initContributeModeWithProjectAttributes(project);
        }
    }

    private void initContributeModeWithFactory(final Factory factory, final ProjectDescriptor project) {
        final Map<String, List<String>> attributes = project.getAttributes();

        if (factory.getProject() != null && factory.getProject().getAttributes() != null) {
            final Map<String, List<String>> attributesFromFactory = factory.getProject().getAttributes();

            if (attributesFromFactory.containsKey(ATTRIBUTE_CONTRIBUTE_KEY)) {

                setTheContributionFlag(attributes, attributesFromFactory.get(ATTRIBUTE_CONTRIBUTE_KEY));

                setTheClonedBranch(attributes, factory);

                persistToProjectAttributes(project, attributes, new AsyncRequestCallback<ProjectDescriptor>(
                        dtoUnmarshallerFactory.newUnmarshaller(ProjectDescriptor.class)) {
                    @Override
                    protected void onSuccess(final ProjectDescriptor project) {
                        initContributeModeWithProjectAttributes(project);
                    }

                    @Override
                    protected void onFailure(final Throwable exception) {
                        notificationHelper.showError(getClass(), messages.contributorExtensionErrorUpdatingContributionAttributes(
                                exception.getMessage()), exception);
                    }
                });
            }
        }
    }

    private void initContributeModeWithProjectAttributes(final ProjectDescriptor project) {
        final Map<String, List<String>> attributes = project.getAttributes();

        if (attributes == null || !attributes.containsKey(ATTRIBUTE_CONTRIBUTE_KEY)) {
            return;
        }
        if (!String.valueOf(TRUE).equalsIgnoreCase(attributes.get(ATTRIBUTE_CONTRIBUTE_KEY).get(0))) {
            return;
        }
        if (!isGitRepository(project)) {
            return;
        }

        startContributionWorkflow();
    }

    private void setTheContributionFlag(final Map<String, List<String>> attributesToUpdate, final List<String> contributeFlagFromFactory) {
        attributesToUpdate.put(ATTRIBUTE_CONTRIBUTE_KEY, contributeFlagFromFactory);
    }

    private void setTheClonedBranch(final Map<String, List<String>> attributesToUpdate, final Factory factory) {
        final Map<String, String> parametersMap = factory.getSource().getProject().getParameters();

        for (final String parameter : parametersMap.keySet()) {
            if ("branch".equals(parameter)) {
                final List<String> clonedBranchAttribute = asList(parametersMap.get(parameter));
                attributesToUpdate.put(ATTRIBUTE_CONTRIBUTE_BRANCH, clonedBranchAttribute);
                break;
            }
        }
    }

    private void persistToProjectAttributes(final ProjectDescriptor currentProject,
                                            final Map<String, List<String>> attributesToUpdate,
                                            final AsyncRequestCallback<ProjectDescriptor> updateCallback) {
        final ProjectUpdate projectToUpdate = dtoFactory.createDto(ProjectUpdate.class);
        copyProjectInfo(currentProject, projectToUpdate);
        projectToUpdate.setAttributes(attributesToUpdate);

        projectService.updateProject(currentProject.getPath(), projectToUpdate, updateCallback);
    }

    private void copyProjectInfo(final ProjectDescriptor projectDescriptor, final ProjectUpdate projectUpdate) {
        projectUpdate.setType(projectDescriptor.getType());
        projectUpdate.setDescription(projectDescriptor.getDescription());
        projectUpdate.setAttributes(projectDescriptor.getAttributes());
        projectUpdate.setRunners(projectDescriptor.getRunners());
        projectUpdate.setBuilders(projectDescriptor.getBuilders());
    }

    private void startContributionWorkflow() {
        workflow.executeStep();
    }

    private void exitContributeMode() {
        contributePartPresenter.remove();
    }
}
