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

import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.api.core.rest.shared.dto.ServiceError;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.project.shared.dto.ImportSourceDescriptor;
import com.codenvy.api.project.shared.dto.Source;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.commons.exception.ServerException;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.AsyncRequestFactory;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.HTTPMethod;
import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.NotificationHelper;
import com.codenvy.plugin.contribution.client.jso.Blob;
import com.codenvy.plugin.contribution.client.jso.FormData;
import com.codenvy.plugin.contribution.client.jso.JsBlob;
import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;

import static com.codenvy.api.project.shared.Constants.VCS_PROVIDER_NAME;
import static com.codenvy.ide.MimeType.APPLICATION_JSON;
import static com.codenvy.ide.rest.HTTPHeader.ACCEPT;
import static com.codenvy.plugin.contribution.client.ContributeConstants.ATTRIBUTE_CONTRIBUTE_KEY;

/**
 * Generates a factory for the contribution reviewer.
 */
public class GenerateReviewFactory implements Step {
    /** The following step. */
    private final Step nextStep;

    /** The following step when failing. */
    private final Step failureNextStep;

    /** The i18n-able messages. */
    private final ContributeMessages messages;

    /** Template for building api urls. */
    private final ApiUrlTemplate apiTemplate;

    /** The DTO factory. */
    private final DtoFactory dtoFactory;

    /** Factory for async requests. */
    private final AsyncRequestFactory asyncRequestFactory;

    /** Unmarshaller for DTOs. */
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    /** The app context. */
    private final AppContext appContext;

    /** The remote VCS repository. */
    private final RepositoryHost repositoryHost;

    /** The notification manager. */
    private final NotificationHelper notificationHelper;

    @Inject
    public GenerateReviewFactory(@Nonnull final AddReviewFactoryLinkStep nextStep,
                                 @Nonnull final ProposePersistStep failureNextStep,
                                 @Nonnull final ApiUrlTemplate apiUrlTemplate,
                                 @Nonnull final ContributeMessages messages,
                                 @Nonnull final DtoFactory dtoFactory,
                                 @Nonnull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                 @Nonnull final AsyncRequestFactory asyncRequestFactory,
                                 @Nonnull final AppContext appContext,
                                 @Nonnull final RepositoryHost repositoryHost,
                                 @Nonnull final NotificationHelper notificationHelper) {
        this.nextStep = nextStep;
        this.failureNextStep = failureNextStep;

        this.apiTemplate = apiUrlTemplate;
        this.messages = messages;

        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dtoFactory = dtoFactory;

        this.appContext = appContext;
        this.repositoryHost = repositoryHost;
        this.notificationHelper = notificationHelper;
    }

    /**
     * Sends the request, passing the form data as content.
     *
     * @param xhr
     *         the request
     * @param formData
     *         the form data
     * @return true iff the request was sent correctly - Note: doesn't mean the request will be succesful
     */
    private static native boolean sendFormData(XMLHttpRequest xhr, FormData formData) /*-{
        try {
            xhr.send(formData);
            return true;
        } catch (e) {
            return false;
        }
    }-*/;

    @Override
    public void execute(@Nonnull final Context context, @Nonnull final Configuration config) {
        createFactory(context, new AsyncCallback<Factory>() {
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
                    recover(context, config, messages.warnCreateFactoryFailed());
                } else {
                    // store the factory url in the context
                    context.setReviewFactoryUrl(factoryUrl);

                    // continue workflow
                    proceed(context, config);
                }
            }

            @Override
            public void onFailure(final Throwable caught) {
                recover(context, config, messages.warnCreateFactoryFailed());
            }
        });
    }

    /**
     * Continue to the following step.
     *
     * @param context
     *         the context of the contribution
     * @param config
     *         the configuration of the contribution
     */
    private void proceed(final Context context, final Configuration config) {
        nextStep.execute(context, config);
    }

    /**
     * Continue to the step that is next when this one failed.
     *
     * @param context
     *         the context of the contribution
     * @param config
     *         the configuration of the contribution
     */
    private void recover(final Context context, final Configuration config, final String cause) {
        notificationHelper.showWarning(cause);
        // continue anyway, this is not a hard failure
        failureNextStep.execute(context, config);
    }

    private void createFactory(final Context context, final AsyncCallback<Factory> callback) {
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

    private void getFactory(final Context context, final AsyncCallback<Factory> callback) {
        exportProject(new AsyncCallback<Factory>() {
            @Override
            public void onSuccess(final Factory factory) {
                factory.setSource(getSource(context));

                /* customize some values */

                // project must be public to be shared
                factory.getProject().setVisibility("public");

                // the new factory is not a 'contribute workflow factory'
                factory.getProject().getAttributes().remove(ATTRIBUTE_CONTRIBUTE_KEY);
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

        final CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            final String projectName = currentProject.getRootProject().getName();
            final String requestUrl = apiTemplate.getFactoryJson(workspaceId, projectName);

            asyncRequestFactory.createGetRequest(requestUrl)
                               .header(ACCEPT, APPLICATION_JSON)
                               .send(new AsyncRequestCallback<Factory>(dtoUnmarshallerFactory.newUnmarshaller(Factory.class)) {

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
    }

    private Source getSource(final Context context) {
        final ImportSourceDescriptor importSourceDescriptor = dtoFactory.createDto(ImportSourceDescriptor.class);

        final String forkRepoUrl = repositoryHost.makeSSHRemoteUrl(context.getHostUserLogin(), context.getForkedRepositoryName());
        importSourceDescriptor.setLocation(forkRepoUrl);

        final String vcsType = context.getProject().getAttributes().get(VCS_PROVIDER_NAME).get(0);
        importSourceDescriptor.setType(vcsType);


        importSourceDescriptor.setParameters(new HashMap<String, String>());
        // keep VCS information
        importSourceDescriptor.getParameters().put("keepVcs", "true");
        // Use the contribution branch
        importSourceDescriptor.getParameters().put("branch", context.getWorkBranchName());

        return dtoFactory.createDto(Source.class).withProject(importSourceDescriptor);
    }

    private void saveFactory(final FormData formData, final AsyncCallback<Factory> callback) {
        final String requestUrl = apiTemplate.saveFactory();

        final XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.open(HTTPMethod.POST, requestUrl);
        xhr.setRequestHeader(ACCEPT, APPLICATION_JSON);
        xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {

            @Override
            public void onReadyStateChange(final XMLHttpRequest request) {
                if (request.getReadyState() == XMLHttpRequest.DONE) {
                    if (request.getStatus() == Response.SC_OK) {
                        request.clearOnReadyStateChange();
                        final String payLoad = request.getResponseText();
                        final Factory createdFactory = dtoFactory.createDtoFromJson(payLoad, Factory.class);

                        if (createdFactory.getId() == null || createdFactory.getId().isEmpty()) {
                            final ServiceError error = dtoFactory.createDtoFromJson(payLoad, ServiceError.class);
                            callback.onFailure(new Exception(error.getMessage()));
                        } else {
                            callback.onSuccess(createdFactory);
                        }
                    } else {
                        final Response response = new ResponseImpl(request);
                        callback.onFailure(new ServerException(response));
                    }
                }

            }
        });

        if (!sendFormData(xhr, formData)) {
            callback.onFailure(new Exception("Could not call service"));
        }
    }

    /**
     * Template for building api urls.
     */
    interface ApiUrlTemplate extends Messages {
        /**
         * Returns a 'getFactoryJson' call URL.
         *
         * @param workspaceId
         *         the workspace id
         * @param projectName
         *         the project name
         * @return the call URL
         */
        @DefaultMessage("/api/factory/{0}/{1}")
        String getFactoryJson(String workspaceId, String projectName);

        /**
         * Returns a 'getFactoryJson'/saveFactory call URL.
         *
         * @return the call URL
         */
        @DefaultMessage("/api/factory")
        String saveFactory();
    }

    /**
     * Concrete {@link Response}, {@link #getHeaders()} copied from GWT internal.
     */
    private class ResponseImpl extends Response {
        private final XMLHttpRequest request;

        ResponseImpl(final XMLHttpRequest request) {
            this.request = request;
        }

        @Override
        public String getText() {
            return request.getResponseText();
        }

        @Override
        public String getStatusText() {
            return request.getStatusText();
        }

        @Override
        public int getStatusCode() {
            return request.getStatus();
        }

        @Override
        public String getHeadersAsString() {
            return request.getAllResponseHeaders();
        }

        @Override
        public Header[] getHeaders() {
            final String allHeaders = request.getAllResponseHeaders();
            final String[] unparsedHeaders = allHeaders.split("\n");
            final Header[] parsedHeaders = new Header[unparsedHeaders.length];

            for (int i = 0, n = unparsedHeaders.length; i < n; ++i) {
                final String unparsedHeader = unparsedHeaders[i];

                if (unparsedHeader.length() == 0) {
                    continue;
                }

                final int endOfNameIdx = unparsedHeader.indexOf(':');
                if (endOfNameIdx < 0) {
                    continue;
                }

                final String name = unparsedHeader.substring(0, endOfNameIdx).trim();
                final String value = unparsedHeader.substring(endOfNameIdx + 1).trim();
                final Header header = new Header() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public String getValue() {
                        return value;
                    }

                    @Override
                    public String toString() {
                        return name + " : " + value;
                    }
                };

                parsedHeaders[i] = header;
            }

            return parsedHeaders;
        }

        @Override
        public String getHeader(final String header) {
            return request.getResponseHeader(header);
        }
    }
}
