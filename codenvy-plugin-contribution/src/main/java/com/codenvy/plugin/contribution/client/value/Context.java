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

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.dto.shared.DTO;

/**
 * Contribution context, with information on current project, branch etc.
 */
@DTO
public interface Context {

    ProjectDescriptor getProject();

    void setProject(ProjectDescriptor desc);

    Context withProject(ProjectDescriptor desc);

    String getWorkBranchName();

    void setWorkBranchName(String name);

    Context withWorkBranchName(String name);
}
