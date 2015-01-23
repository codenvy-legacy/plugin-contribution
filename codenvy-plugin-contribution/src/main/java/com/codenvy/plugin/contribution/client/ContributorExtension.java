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
import com.codenvy.plugin.contribution.client.steps.AuthenticateUserStep;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.steps.Step;
import com.codenvy.plugin.contribution.client.vcs.Remote;
import com.codenvy.plugin.contribution.client.vcs.VcsService;
import com.codenvy.plugin.contribution.client.vcs.VcsServiceProvider;
import com.codenvy.plugin.contribution.client.vcs.hosting.VcsHostingService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import static com.codenvy.plugin.contribution.client.ContributeConstants.ATTRIBUTE_CONTRIBUTE_BRANCH;
import static com.codenvy.plugin.contribution.client.ContributeConstants.ATTRIBUTE_CONTRIBUTE_KEY;
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
    private final Step                    authenticateUserStep;
    private final VcsHostingService       vcsHostingService;
    private final VcsServiceProvider      vcsServiceProvider;

    @Inject
    public ContributorExtension(@Nonnull final EventBus eventBus,
                                @Nonnull final ContributeMessages messages,
                                @Nonnull final ContributeResources resources,
                                @Nonnull final AppContext appContext,
                                @Nonnull final NotificationHelper notificationHelper,
                                @Nonnull final ContributePartPresenter contributePartPresenter,
                                @Nonnull final ProjectServiceClient projectService,
                                @Nonnull final DtoFactory dtoFactory,
                                @Nonnull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                @Nonnull final ContributorWorkflow workflow,
                                @Nonnull final VcsHostingService vcsHostingService,
                                @Nonnull final VcsServiceProvider vcsServiceProvider,
                                @Nonnull final AuthenticateUserStep authenticateUserStep) {
        this.messages = messages;
        this.workflow = workflow;
        this.appContext = appContext;
        this.notificationHelper = notificationHelper;
        this.contributePartPresenter = contributePartPresenter;
        this.projectService = projectService;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.vcsHostingService = vcsHostingService;
        this.vcsServiceProvider = vcsServiceProvider;
        this.authenticateUserStep = authenticateUserStep;

        resources.contributeCss().ensureInjected();

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(final ProjectActionEvent event) {
                initializeContributorExtension(event);
            }

            @Override
            public void onProjectClosed(final ProjectActionEvent projectActionEvent) {
                exitContributeMode();
            }
        });
    }

    private void initializeContributorExtension(final ProjectActionEvent event) {
        final ProjectDescriptor project = event.getProject();

        // vcs supported and initialized in current project
        final VcsService vcsService = vcsServiceProvider.getVcsService();
        if (vcsService != null) {
            vcsService.listRemotes(project, new AsyncCallback<List<Remote>>() {
                @Override
                public void onFailure(final Throwable exception) {
                    notificationHelper.showError(ContributorExtension.class, exception);
                }

                @Override
                public void onSuccess(final List<Remote> remotes) {
                    for (final Remote oneRemote : remotes) {
                        final String remoteUrl = oneRemote.getUrl();
                        if (remoteUrl != null && vcsHostingService.isVcsHostRemoteUrl(remoteUrl)) {

                            if (appContext.getFactory() != null) {
                                initializeProjectAttributesFromFactory(appContext.getFactory(), project, new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(final Throwable exception) {
                                        notificationHelper
                                                .showError(getClass(), messages.contributorExtensionErrorUpdatingContributionAttributes(
                                                        exception.getMessage()), exception);
                                    }

                                    @Override
                                    public void onSuccess(final Void result) {
                                        startContributionWorkflow();
                                    }
                                });
                            }

                            startContributionWorkflow();

                            break;
                        }
                    }
                }
            });
        }
    }

    private void initializeProjectAttributesFromFactory(final Factory factory,
                                                        final ProjectDescriptor project,
                                                        final AsyncCallback<Void> callback) {
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
                        callback.onSuccess(null);
                    }

                    @Override
                    protected void onFailure(final Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        }
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
        workflow.setStep(authenticateUserStep);
        workflow.executeStep();
    }

    private void exitContributeMode() {
        contributePartPresenter.remove();
    }
}
