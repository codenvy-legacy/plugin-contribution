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
package com.codenvy.plugin.contribution.client.steps;

import org.eclipse.che.api.promises.client.Promise;

import com.google.gwt.core.client.Callback;

/**
 * Prerequisite for a step execution.
 */
public interface Prerequisite {

    /**
     * Attempt to fulfill the prerequisite.
     *
     * @param callback the success and failure issues.
     */
    void fulfill(Context context, Callback<Void, Throwable> callback);

    Promise<Void> fulfill(Context context);
}
