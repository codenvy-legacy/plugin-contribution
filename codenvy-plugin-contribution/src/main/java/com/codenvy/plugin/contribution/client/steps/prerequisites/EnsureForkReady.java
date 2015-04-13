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

import javax.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.Call;
import org.eclipse.che.ide.api.app.AppContext;

import com.codenvy.plugin.contribution.client.steps.Context;
import com.codenvy.plugin.contribution.client.steps.Prerequisite;
import com.google.gwt.core.client.Callback;

/**
 * Prerequisite for steps that need the fork to be ready on the VCS host.
 */
public class EnsureForkReady implements Prerequisite {

    private final AppContext appContext;

    @Inject
    public EnsureForkReady(final AppContext appContext) {
        this.appContext = appContext;
    }

    private void isFulfilled(final Callback<Boolean, Throwable> callback) {

    }

    @Override
    public void fulfill(final Context context, final Callback<Void, Throwable> callback) {
        isFulfilled(new Callback<Boolean, Throwable>() {
            @Override
            public void onFailure(final Throwable reason) {
                // TODO
            }
            @Override
            public void onSuccess(final Boolean fullfilled) {
                if (fullfilled) {
                    callback.onSuccess(null);
                } else {
                    // TODO
                }
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
