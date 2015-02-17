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

/**
 * Causes for the abortion of review initialization.
 */
public enum AbortReviewCause {

    /** The project doesn't have a remote that is identified as upstream. */
    NO_UPSTREAM_REMOTE,

    /** The upstream remote soesn't have an URL that matches the VCS hosting service. */
    REMOTE_URL_HOSTING_MISMATCH,
    NO_AUTH
}
