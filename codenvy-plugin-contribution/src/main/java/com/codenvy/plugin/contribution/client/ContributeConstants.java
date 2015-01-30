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
package com.codenvy.plugin.contribution.client;

/**
 * Shared constants for the contribution plugin.
 */
public final class ContributeConstants {

    private ContributeConstants() {
    }

    /**
     * The factory and project attribute for the contribution workflow.
     */
    public static final String ATTRIBUTE_CONTRIBUTE_KEY = "contribute";

    /**
     * github flow contribute value for the contribute attribute.
     */
    public static final String GITHUB_CONTRIBUTE_FLAG = "github";

    /**
     * Project attribute key for the branch initially cloned.
     */
    public static final String ATTRIBUTE_CONTRIBUTE_BRANCH = "contribute_branch";
}
