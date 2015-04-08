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
package com.codenvy.plugin.contribution.client.exception;

public class NoAuthenticationException extends Exception {

    private static final long serialVersionUID = 1L;

    public NoAuthenticationException() {
    }

    public NoAuthenticationException(String message) {
        super(message);
    }

    public NoAuthenticationException(Throwable cause) {
        super(cause);
    }

    public NoAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
