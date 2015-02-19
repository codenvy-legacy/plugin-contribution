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
package com.codenvy.plugin.contribution.shared.client;

/**
 * Shared constants for the contribution plugin.
 */
public final class SharedConstants {

    private SharedConstants() {
    }

    /**
     * The factory and project attribute for the review workflow.
     */
    public static final String ATTRIBUTE_REVIEW_KEY = "review";

    /**
     * The factory and project attribute for the reviewed pull request id.
     */
    public static final String ATTRIBUTE_REVIEW_PULLREQUEST_ID = "pullRequestId";

    /**
     * The key for the factory/project attribute for the owner of the upstream repository.
     */
    public static final String ATTRIBUTE_REVIEW_UPSTREAM_OWNER = "upstream_owner";

    /**
     * The key for the factory/project attribute for the upstream repository.
     */
    public static final String ATTRIBUTE_REVIEW_UPSTREAM_REPOSITORY = "upstream_repository";
}
