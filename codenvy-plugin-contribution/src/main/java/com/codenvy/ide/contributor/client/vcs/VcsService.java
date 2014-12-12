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
package com.codenvy.ide.contributor.client.vcs;

import javax.annotation.Nonnull;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface VcsService {
    void checkoutBranch(@Nonnull ProjectDescriptor project, String name, boolean createNew, AsyncCallback<String> callback);

    void createBranch(@Nonnull ProjectDescriptor project, String name, String startPoint, AsyncCallback<Branch> callback);

    void getBranchName(@Nonnull ProjectDescriptor project, @Nonnull AsyncCallback<String> callback);
}
