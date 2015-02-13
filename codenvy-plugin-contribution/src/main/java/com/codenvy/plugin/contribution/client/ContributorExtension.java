/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
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
import com.codenvy.api.project.shared.dto.ImportSourceDescriptor;
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
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.Remote;
import com.codenvy.plugin.contribution.vcs.VcsService;
import com.codenvy.plugin.contribution.vcs.VcsServiceProvider;
import com.codenvy.plugin.contribution.vcs.hosting.VcsHostingService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;
import java.util.Map;

import static com.codenvy.plugin.contribution.client.ContributeConstants.ATTRIBUTE_CONTRIBUTE_BRANCH;
import static java.util.Arrays.asList;

/**
 * @author Stephane Tournie
 * @author Kevin Pollet
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributorExtension implements ProjectActionHandler {
    private final ContributeMessages      messages;
    private final AppContext              appContext;
    private final NotificationHelper      notificationHelper;
    private final ContributePartPresenter contributePartPresenter;
    private final ProjectServiceClient    projectService;
    private final DtoFactory              dtoFactory;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final ContributorWorkflow     workflow;
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
                                @Nonnull final VcsServiceProvider vcsServiceProvider) {
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

        resources.contributeCss().ensureInjected();
        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    @Override
    public void onProjectOpened(final ProjectActionEvent event) {
        initializeContributorExtension(event.getProject());
    }

    @Override
    public void onProjectClosed(final ProjectActionEvent event) {
        contributePartPresenter.remove();
    }

    private void initializeContributorExtension(final ProjectDescriptor project) {
        final VcsService vcsService = vcsServiceProvider.getVcsService();
        final List<String> projectPermissions = project.getPermissions();

        if (vcsService != null && projectPermissions != null && projectPermissions.contains("write")) {
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
                            final Factory factory = appContext.getFactory();
                            final Map<String, List<String>> projectAttributes = project.getAttributes();

                            setClonedBranch(projectAttributes, factory, vcsService, project, new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(final Throwable exception) {
                                    notificationHelper.showError(ContributorExtension.class, exception);
                                }

                                @Override
                                public void onSuccess(final Void result) {
                                    updateProjectAttributes(project, projectAttributes, new AsyncRequestCallback<ProjectDescriptor>(
                                            dtoUnmarshallerFactory.newUnmarshaller(ProjectDescriptor.class)) {
                                        @Override
                                        protected void onFailure(final Throwable exception) {
                                            notificationHelper
                                                    .showError(getClass(), messages.contributorExtensionErrorUpdatingContributionAttributes(
                                                            exception.getMessage()), exception);
                                        }

                                        @Override
                                        protected void onSuccess(final ProjectDescriptor project) {
                                            contributePartPresenter.open();
                                            workflow.init();
                                            workflow.executeStep();
                                        }
                                    });
                                }
                            });

                            break;
                        }
                    }
                }
            });
        }
    }

    private void setClonedBranch(final Map<String, List<String>> projectAttributes,
                                 final Factory factory,
                                 final VcsService vcsService,
                                 final ProjectDescriptor project,
                                 final AsyncCallback<Void> callback) {

        if (projectAttributes.containsKey(ATTRIBUTE_CONTRIBUTE_BRANCH)) {
            callback.onSuccess(null);

        } else {
            if (factory != null && factory.getSource() != null) {
                final ImportSourceDescriptor factoryProject = factory.getSource().getProject();
                final Map<String, String> parameters = factoryProject.getParameters();

                final String branchName = parameters.get("branch");
                if (branchName != null) {
                    projectAttributes.put(ATTRIBUTE_CONTRIBUTE_BRANCH, asList(branchName));
                    callback.onSuccess(null);
                    return;
                }
            }

            vcsService.getBranchName(project, new AsyncCallback<String>() {
                @Override
                public void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }

                @Override
                public void onSuccess(final String branchName) {
                    projectAttributes.put(ATTRIBUTE_CONTRIBUTE_BRANCH, asList(branchName));
                    callback.onSuccess(null);
                }
            });
        }
    }

    private void updateProjectAttributes(final ProjectDescriptor currentProject,
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
        projectUpdate.setMixinTypes(projectDescriptor.getMixins());
        projectUpdate.setBuilders(projectDescriptor.getBuilders());
    }
}
