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
package com.codenvy.plugin.contribution.client.steps.prerequisites;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.steps.Context;
import com.codenvy.plugin.contribution.client.steps.ContributorWorkflow;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.Call;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;

import com.codenvy.plugin.contribution.client.steps.Prerequisite;
import com.codenvy.plugin.contribution.client.steps.prerequisites.AsyncPromiseHelper.RequestCall;
import com.codenvy.plugin.contribution.vcs.client.hosting.NoVcsHostingServiceImplementationException;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.HostUser;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;

import static com.codenvy.plugin.contribution.client.steps.StepIdentifier.AUTHORIZE_CODENVY_ON_VCS_HOST;

/**
 * Prerequisite for steps that need the current user to be authenticated on codenvy.
 */
public class EnsureVcsHostAuthentication implements Prerequisite {

    private final AppContext appContext;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final ContributeMessages messages;
    private final NotificationHelper notificationHelper;

    @Inject
    public EnsureVcsHostAuthentication(final AppContext appContext,
                                       @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                       @Nonnull final NotificationHelper notificationHelper,
                                       final ContributeMessages messages) {
        this.appContext = appContext;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.messages = messages;
        this.notificationHelper = notificationHelper;
    }

    private void isFulfilled(final Callback<Boolean, Throwable> callback, final Promise<VcsHostingService> hostingPromise) {
        hostingPromise.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(final PromiseError error) throws OperationException {
                callback.onFailure(new NoVcsHostingServiceImplementationException());
            }
        });
        final Promise<HostUser> userPromise = getUserInfo(hostingPromise);

        userPromise.then(new Operation<HostUser>() {
            @Override
            public void apply(final HostUser user) throws OperationException {
                callback.onSuccess(true);
            }
        });
        userPromise.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(final PromiseError err) throws OperationException {
                if (err instanceof GwtPromiseError && ((GwtPromiseError) err).getException() instanceof UnauthorizedException) {
                    callback.onSuccess(false);
                } else {
                    callback.onFailure(new Exception(err.toString()));
                }
            }
        });
    }

    @Override
    public void fulfill(final Context context, final Callback<Void, Throwable> callback) {
        final Promise<VcsHostingService> hostingPromise = getVcsHostingService();
        isFulfilled(new Callback<Boolean, Throwable>() {
            @Override
            public void onFailure(final Throwable reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(final Boolean fulfilled) {
                if (fulfilled) {
                    callback.onSuccess(null);
                } else {
                    doAuthentication(context, hostingPromise, callback);
                }
            }
        }, hostingPromise);
    }

    private void doAuthentication(final Context context, final Promise<VcsHostingService> hostingPromise, final Callback<Void, Throwable> callback) {
        hostingPromise.then(new Operation<VcsHostingService>() {
            @Override
            public void apply(final VcsHostingService hosting) throws OperationException {
                hosting.authenticate(appContext.getCurrentUser(), new AsyncCallback<HostUser>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        if (throwable instanceof UnauthorizedException) {
                            notificationHelper.showError(EnsureCodenvyAuthentication.class,
                                    messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHost());
                        } else {
                            notificationHelper.showError(EnsureCodenvyAuthentication.class, throwable);
                        }
                    }

                    @Override
                    public void onSuccess(final HostUser hostUser) {
                        context.setHostUserLogin(hostUser.getLogin());
                    }
                });
            }
        });
    }

    private Promise<VcsHostingService> getVcsHostingService() {
        return AsyncPromiseHelper.createFromAsyncRequest(new RequestCall<VcsHostingService>() {
            @Override
            public void makeCall(final AsyncCallback<VcsHostingService> callback) {
                vcsHostingServiceProvider.getVcsHostingService(callback);
            }
        });
    }

    private Promise<HostUser> getUserInfo(final Promise<VcsHostingService> hostingPromise) {
        return AsyncPromiseHelper.createFromAsyncRequest(new RequestCall<HostUser>() {
            @Override
            public void makeCall(final AsyncCallback<HostUser> callback) {
                hostingPromise.then(new Operation<VcsHostingService>() {
                    @Override
                    public void apply(final VcsHostingService hosting) throws OperationException {
                        hosting.getUserInfo(callback);
                    }
                });
            }
        });
    }

    @Override
    public Promise<Void> fulfill(final Context context) {
        return CallbackPromiseHelper.createFromCallback(new Call<Void, Throwable>() {
            @Override
            public void makeCall(final Callback<Void, Throwable> callback) {
                fulfill(context, callback);
            }
        });
    }
}
