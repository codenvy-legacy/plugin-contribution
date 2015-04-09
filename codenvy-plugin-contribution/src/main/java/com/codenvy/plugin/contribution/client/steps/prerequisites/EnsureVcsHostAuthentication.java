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

/**
 * Prerequisite for steps that need the current user to be authenticated on codenvy.
 */
public class EnsureVcsHostAuthentication implements Prerequisite {

    private final AppContext appContext;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;

    @Inject
    public EnsureVcsHostAuthentication(final AppContext appContext,
                                       @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider) {
        this.appContext = appContext;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
    }

    private void isFulfilled(final Callback<Boolean, Throwable> callback,
                              final Promise<VcsHostingService> hostingPromise) {
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
                if (err instanceof GwtPromiseError && ((GwtPromiseError)err).getException() instanceof UnauthorizedException) {
                    callback.onSuccess(false);
                } else {
                    callback.onFailure(new Exception(err.toString()));
                }
            }
        });
    }

    @Override
    public void fulfill(final Callback<Void, Throwable> callback) {
        final Promise<VcsHostingService> hostingPromise = getVcsHostingService();
        isFulfilled(new Callback<Boolean, Throwable>() {
            @Override
            public void onFailure(final Throwable reason) {
                // TODO
            }

            @Override
            public void onSuccess(final Boolean fulfilled) {
                if (fulfilled) {
                    callback.onSuccess(null);
                } else {
                    doAuthentication(callback);
                }
            }
        }, hostingPromise);
    }

    private void doAuthentication(final Callback<Void, Throwable> callback) {

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
    public Promise<Void> fulfill() {
        return CallbackPromiseHelper.createFromCallback(new Call<Void, Throwable>() {
            @Override
            public void makeCall(final Callback<Void, Throwable> callback) {
                fulfill(callback);
            }
        });
    }
}
