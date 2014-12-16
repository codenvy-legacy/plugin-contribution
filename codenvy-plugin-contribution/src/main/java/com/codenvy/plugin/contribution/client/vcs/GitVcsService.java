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
package com.codenvy.plugin.contribution.client.vcs;

import java.util.ArrayList;
import java.util.List;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.ext.git.client.GitServiceClient;
import com.codenvy.ide.ext.git.shared.Status;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Git backed implementation for {@link VcsService}.
 */
public class GitVcsService implements VcsService {

    /**
     * The git client service.
     */
    private final GitServiceClient service;

    /**
     * The DTO factory.
     */
    private final DtoFactory dtoFactory;

    /**
     * Unmarshaller for DTOs.
     */
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public GitVcsService(final DtoFactory dtoFactory,
                         final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         final GitServiceClient service) {
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.service = service;
    }

    @Override
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

    @Override
    public void createBranch(final ProjectDescriptor project, final String name,
                             final String startPoint, final AsyncCallback<Branch> callback) {
        final Unmarshallable<com.codenvy.ide.ext.git.shared.Branch> unMarshaller = dtoUnmarshallerFactory.newUnmarshaller(com.codenvy.ide.ext.git.shared.Branch.class);
        service.branchCreate(project, name, startPoint, new AsyncRequestCallback<com.codenvy.ide.ext.git.shared.Branch>(unMarshaller) {
            @Override
            protected void onSuccess(final com.codenvy.ide.ext.git.shared.Branch result) {
                callback.onSuccess(fromGitBranch(result));
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

    @Override
    public void renameBranch(final ProjectDescriptor project,
                             final String oldName,
                             final String newName,
                             final AsyncCallback<Void> callback) {

        service.branchRename(project, oldName, newName, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String result) {
                callback.onSuccess(null);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void listLocalBranches(final ProjectDescriptor project, final AsyncCallback<List<Branch>> callback) {
        final Unmarshallable<Array<com.codenvy.ide.ext.git.shared.Branch>> unMarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(com.codenvy.ide.ext.git.shared.Branch.class);
        this.service.branchList(project, "false",
            new AsyncRequestCallback<Array<com.codenvy.ide.ext.git.shared.Branch>>(unMarshaller) {
                @Override
                protected void onSuccess(final Array<com.codenvy.ide.ext.git.shared.Branch> branches) {
                    final List<Branch> result = new ArrayList<>();
                    for (final com.codenvy.ide.ext.git.shared.Branch branch: branches.asIterable()) {
                        result.add(fromGitBranch(branch));
                    }
                    callback.onSuccess(result);
                }
                @Override
                protected void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }
            });
    }

    /**
     * Converts a git branch DTO to an abstracted branch object.
     * 
     * @param gitBracnh the object to convert
     * @return the converted object
     */
    private Branch fromGitBranch(final com.codenvy.ide.ext.git.shared.Branch gitBracnh) {
        final Branch branch = GitVcsService.this.dtoFactory.createDto(Branch.class);
        branch.withActive(gitBracnh.isActive()).withRemote(gitBracnh.isRemote())
              .withName(gitBracnh.getName()).withDisplayName(gitBracnh.getDisplayName());
        return branch;
    }
}
