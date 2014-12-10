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
package com.codenvy.ide.contributor.client;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.ext.git.client.GitServiceClient;
import com.codenvy.ide.ext.git.shared.Branch;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.inject.Inject;

public class GitAgent {

    private final GitServiceClient service;
    
    @Inject
    public GitAgent(GitServiceClient service) {
        this.service = service;
    }
    
    public void checkoutBranch(ProjectDescriptor project, String name, boolean createNew, AsyncRequestCallback<String> callback) {
        service.branchCheckout(project, name, null, createNew, callback);
    }
    
    public void createBranch(ProjectDescriptor project, String name, String startPoint, AsyncRequestCallback<Branch> callback) {
        service.branchCreate(project, name, startPoint, callback);
    }
}
