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

import com.codenvy.dto.shared.DTO;

/**
 * Contribution configuration, which contains the values chosen by the user.
 */
@DTO
public interface Configuration {
    String getContributionBranchName();

    Configuration withContributionBranchName(String name);

    String getContributionComment();

    Configuration withContributionComment(String comment);

    String getContributionTitle();

    Configuration withContributionTitle(String title);
}
