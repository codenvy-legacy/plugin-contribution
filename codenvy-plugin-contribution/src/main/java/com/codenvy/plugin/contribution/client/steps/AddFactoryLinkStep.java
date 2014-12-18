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

import java.util.HashMap;

import javax.inject.Inject;

import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.api.core.rest.shared.dto.ServiceError;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.project.shared.Constants;
import com.codenvy.api.project.shared.dto.ImportSourceDescriptor;
import com.codenvy.api.project.shared.dto.Source;
import com.codenvy.ide.MimeType;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.AsyncRequestFactory;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.HTTPHeader;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.plugin.contribution.client.ContributeConstants;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.jso.Blob;
import com.codenvy.plugin.contribution.client.jso.FormData;
import com.codenvy.plugin.contribution.client.jso.JsBlob;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Adds a factory link to the contribution in a comment of the pull request.
 */
public class AddFactoryLinkStep implements Step {

    /**
     * The following step.
     */
    private final Step nextStep;

    /**
     * The remote VCS repository.
     */
    private final RepositoryHost repository;

    /**
     * The i18n-able messages.
     */
    private final ContributeMessages messages;

    /**
     * The notification manager.
     */
    private final NotificationManager notificationManager;

    /**
     * The DTO factory.
     */
    private final DtoFactory dtoFactory;

    /**
     * The app context.
     */
    private final AppContext appContext;

    /**
     * The repository host.
     */
    private final RepositoryHost repositoryHost;

    /**
     * Template for building api urls.
     */
    private final ApiUrlTemplate apiTemplate;

    /**
     * Factory for async requests.
     */
    private final AsyncRequestFactory asyncRequestFactory;

    /**
     * Unmarshaller for DTOs.
     */
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public AddFactoryLinkStep(final ProposePersistStep nextStep,
                              final RepositoryHost repository,
                              final ContributeMessages messages,
                              final NotificationManager notificationManager,
                              final DtoFactory dtoFactory,
                              final AppContext appContext,
                              final RepositoryHost repositoryHost,
                              final ApiUrlTemplate apiUrlTemplate,
                              final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                              final AsyncRequestFactory asyncRequestFactory) {
        this.repository = repository;
        this.messages = messages;
        this.notificationManager = notificationManager;
        this.nextStep = nextStep;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.repositoryHost = repositoryHost;
        this.apiTemplate = apiUrlTemplate;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        createFactoryUrl(context, new AsyncCallback<Factory>() {

            @Override
            public void onSuccess(final Factory factory) {
                // find factory URL inside factory
                String factoryUrl = null;
                final String createProject = "create-project";
                for (final Link link : factory.getLinks()) {
                    if (createProject.equals(link.getRel())) {
                        factoryUrl = link.getHref();
                        break;
                    }
                }
                if (factoryUrl == null) {
                    onFailure(new Exception("No matching link in generated factory"));
                } else {
                    // post the comment
                    sendComment(context, config, factoryUrl);
                }
            }

            @Override
            public void onFailure(final Throwable caught) {
                notificationManager.showWarning(messages.warnCreateFactoryFailed());
                // continue anyway, this is not a hard failure
                proceed(context, config);
            }
        });

    }

    private void sendComment(final Context context, final Configuration config, final String factoryUrl) {
        final String commentText = messages.pullRequestlinkComment(factoryUrl);
        this.repository.commentPullRequest(context.getOriginRepositoryOwner(), context.getOriginRepositoryName(),
                                           context.getPullRequestId(), commentText, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void notUsed) {
                proceed(context, config);
            }
            @Override
            public void onFailure(final Throwable caught) {
                notificationManager.showWarning(messages.warnPostFactoryLinkFailed(factoryUrl));
                // continue anyway, this is not a hard failure
                proceed(context, config);
            }
        });
    }

    private void createFactoryUrl(final Context context, final AsyncCallback<Factory> callback) {
        getFactory(context, new AsyncCallback<Factory>() {
            @Override
            public void onSuccess(final Factory factory) {
                final String factoryJson = dtoFactory.toJson(factory);
                final FormData formData = FormData.create();

                final Blob blob = JsBlob.create(factoryJson);
                formData.append("factoryUrl", blob);
                saveFactory(formData, callback);
            }

            @Override
            public void onFailure(final Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    private void saveFactory(final FormData formData, final AsyncCallback<Factory> callback) {
        final String requestUrl = this.apiTemplate.getFactoryJson();

        final Unmarshallable<String> unmarshaller = this.dtoUnmarshallerFactory.newUnmarshaller(String.class);
        this.asyncRequestFactory.createPostRequest(requestUrl, formData)
                                .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                                .send(new AsyncRequestCallback<String>(unmarshaller) {
                                    @Override
                                    protected void onSuccess(final String result) {
                                        final Factory createdFactory = dtoFactory.createDtoFromJson(result, Factory.class);

                                        if (createdFactory.getId() == null || createdFactory.getId().isEmpty()) {
                                            final ServiceError error = dtoFactory.createDtoFromJson(result, ServiceError.class);
                                            callback.onFailure(new Exception(error.getMessage()));
                                        } else {
                                            callback.onSuccess(createdFactory);
                                        }
                                    }
                                    @Override
                                    protected void onFailure(final Throwable exception) {
                                        callback.onFailure(exception);
                                    }
                                });
    }

    private void proceed(final Context context, final Configuration config) {
        this.nextStep.execute(context, config);
    }

    public void getFactory(final Context context, final AsyncCallback<Factory> callback) {
        exportProject(new AsyncCallback<Factory>() {
            @Override
            public void onSuccess(final Factory factory) {
                factory.setSource(getSource(context));

                /* customize some values */

                // project must be public to be shared
                factory.getProject().setVisibility("public");

                // the new factory is not a 'contribute workflow factory'
                factory.getProject().getAttributes().remove(ContributeConstants.ATTRIBUTE_CONTRIBUTE_KEY);
                callback.onSuccess(factory);
            }

            @Override
            public void onFailure(final Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    private void exportProject(final AsyncCallback<Factory> callback) {
        final String workspaceId = appContext.getWorkspace().getId();
        final String projectName = appContext.getCurrentProject().getRootProject().getName();
        final String requestUrl = this.apiTemplate.getFactoryJson(workspaceId, projectName);

        final Unmarshallable<Factory> unmarshaller = this.dtoUnmarshallerFactory.newUnmarshaller(Factory.class);
        this.asyncRequestFactory.createGetRequest(requestUrl)
                                .header(HTTPHeader.ACCEPT, MimeType.TEXT_PLAIN)
                                .send(new AsyncRequestCallback<Factory>(unmarshaller) {

                                    @Override
                                    protected void onSuccess(final Factory result) {
                                        callback.onSuccess(result);
                                    }

                                    @Override
                                    protected void onFailure(final Throwable exception) {
                                        callback.onFailure(exception);
                                    }
                                });
    }

    private Source getSource(final Context context) {
        final ImportSourceDescriptor importSourceDescriptor = dtoFactory.createDto(ImportSourceDescriptor.class);

        final String forkRepoUrl = this.repositoryHost.makeRemoteUrl(context.getHostUserLogin(), context.getOriginRepositoryName());
        importSourceDescriptor.setLocation(forkRepoUrl);

        final String vcsType = context.getProject().getAttributes().get(Constants.VCS_PROVIDER_NAME).get(0);
        importSourceDescriptor.setType(vcsType);


        importSourceDescriptor.setParameters(new HashMap<String, String>());
        // keep VCS information
        importSourceDescriptor.getParameters().put("keepVcs", "true");
        // Use the contributin branch
        importSourceDescriptor.getParameters().put("branch", context.getWorkBranchName());

        return dtoFactory.createDto(Source.class).withProject(importSourceDescriptor);
    }
    /**
     * Template for building api urls.
     */
    interface ApiUrlTemplate extends Messages {
        /**
         * Returns a 'getFactoryJson' call URL.
         * 
         * @param workspaceId the workspace id
         * @param projectName the project name
         * @return the call URL
         */
        @DefaultMessage("/api/factory/{0}/{1}")
        String getFactoryJson(String workspaceId, String projectName);

        /**
         * Returns a 'getFactoryJson' call URL.
         * 
         * @return the call URL
         */
        @DefaultMessage("/api/factory")
        String getFactoryJson();
    }
}
