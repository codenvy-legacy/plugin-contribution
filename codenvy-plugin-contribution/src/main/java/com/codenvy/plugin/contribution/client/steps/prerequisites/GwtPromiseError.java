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

import org.eclipse.che.api.promises.client.PromiseError;

public class GwtPromiseError implements PromiseError {

    private final Throwable exception;

    public GwtPromiseError(final Throwable e) {
        this.exception = e;
    }

    public Throwable getException() {
        return exception;
    }

    public String getMessage() {
        return this.exception.getMessage();
    }
}
