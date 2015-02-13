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
package com.codenvy.plugin.contribution.vcs;

import javax.annotation.Nonnull;

/**
 * Exception raised when the branch pushed is up to date.
 *
 * @author Kevin Pollet
 */
public class BranchUpToDateException extends Exception {
    /**
     * Constructs an instance of {@link com.codenvy.plugin.contribution.vcs.BranchUpToDateException}.
     *
     * @param branchName
     *         the branch name.
     */
    public BranchUpToDateException(@Nonnull final String branchName) {
        super("Branch '" + branchName + "' is up-to-date");
    }
}