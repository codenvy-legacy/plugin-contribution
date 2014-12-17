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

import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;
import com.codenvy.plugin.contribution.client.vcshost.RepositoryHost;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class WaitForForkOnRemoteStep implements Step {

    /** The frequency of the checks on the remote. */
    private static final int POLL_FREQUENCY_MS = 1000;

    /**
     * The remote reposiory host.
     */
    private final RepositoryHost repositoryHost;

    /**
     * The following step.
     */
    private final Step next;

    /**
     * The timer used for waiting.
     */
    private Timer timer;

    @AssistedInject
    public WaitForForkOnRemoteStep(final RepositoryHost host,
                                   final @Assisted Step nextStep) {
        this.repositoryHost = host;
        this.next = nextStep;
    }

    @Override
    public void execute(final Context context, final Configuration config) {
        check(context, config);
    }

    private void wait(final Context context, final Configuration config) {
        if (this.timer == null) {
            this.timer = new Timer() {
                @Override
                public void run() {
                    checkRepository(context, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(final Throwable caught) {
                            check(context, config);
                        }
                        @Override
                        public void onSuccess(final Void result) {
                            context.setForkReady(true);
                            check(context, config);
                        }
                    });
                }
            };
        }
        timer.schedule(POLL_FREQUENCY_MS);
    }

    private void check(final Context context, final Configuration config) {
        if (context.getForkReady()) {
            this.next.execute(context, config);
        } else {
            wait(context, config);
        }
    }

    private void checkRepository(final Context context, AsyncCallback<Void> callback) {
        
    }
}
