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

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.ext.git.client.GitServiceClient;
import com.codenvy.ide.ext.git.shared.Status;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class GitVcsService implements VcsService {

    private final GitServiceClient service;

    @Inject
    public GitVcsService(final GitServiceClient service) {
        this.service = service;
    }

    public void checkoutBranch(final ProjectDescriptor project, final String name,
                               final boolean createNew, final AsyncCallback<String> callback) {
        service.branchCheckout(project, name, null, createNew, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(String result) {
                callback.onSuccess(result);
            }
            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    public void createBranch(final ProjectDescriptor project, final String name,
                             final String startPoint, final AsyncCallback<Branch> callback) {
        service.branchCreate(project, name, startPoint, new AsyncRequestCallback<com.codenvy.ide.ext.git.shared.Branch>() {
            @Override
            protected void onSuccess(final com.codenvy.ide.ext.git.shared.Branch result) {
                final Branch branch = new Branch();
                branch.withActive(result.isActive()).withRemote(result.isRemote())
                      .withName(branch.getName()).withDisplayName(branch.getDisplayName());
                callback.onSuccess(branch);
            }
            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void getBranchName(final ProjectDescriptor project, final AsyncCallback<String> callback) {
        service.status(project, new AsyncRequestCallback<Status>() {
            @Override
            protected void onSuccess(final Status result) {
                callback.onSuccess(result.getBranchName());
            }
            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }
}
