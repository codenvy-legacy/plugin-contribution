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
package com.codenvy.plugin.review.client;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.google.inject.Provider;


public class ReviewState {

    private final Provider<Context> contextProvider;
    private Context context;

    @Inject
    public ReviewState(@Nonnull final Provider<Context> contextProvider) {
        this.contextProvider = contextProvider;
    }

    public void init() {
        this.context = this.contextProvider.get();
    }

    public void reset() {
        this.context = null;
    }

    public Context getContext() {
        return this.context;
    }
}
