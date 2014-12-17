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
package com.codenvy.plugin.contribution.client.value;

import com.codenvy.dto.shared.DTO;

/**
 * Contribution configuration, which contains the values choosen by the user.
 */
@DTO
public interface Configuration {

    String getBranchName();

    void setBranchName(String name);

    Configuration withBranchName(String name);

    String getPullRequestComment();

    void setPullRequestComment(String comment);

    Configuration withPullRequestComment(String comment);

    String getContributionTitle();

    void setContributionTitle(String title);

    Configuration withContributionTitle(String title);
}