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
package com.codenvy.plugin.contribution.client.contribdialog;

import javax.annotation.Nonnull;

import com.codenvy.plugin.contribution.client.value.Configuration;
import com.codenvy.plugin.contribution.client.value.Context;


/**
 * Interface for the operation following the configuration in the contribution workflow.
 */
public interface FinishContributionOperation {

    /**
     * Continue contribution.
     * 
     * @param configuration the configuration obtained from the user
     */
    void finishContribution(@Nonnull Context context, @Nonnull Configuration configuration);
}
