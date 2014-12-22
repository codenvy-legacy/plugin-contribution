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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
    public void checkoutBranch(@Nonnull final ProjectDescriptor project, final String name,
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
    public void createBranch(@Nonnull final ProjectDescriptor project, final String name,
                             final String startPoint, final AsyncCallback<Branch> callback) {
        final Unmarshallable<com.codenvy.ide.ext.git.shared.Branch> unMarshaller =
                dtoUnmarshallerFactory.newUnmarshaller(com.codenvy.ide.ext.git.shared.Branch.class);
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
    public void getBranchName(@Nonnull final ProjectDescriptor project, @Nonnull final AsyncCallback<String> callback) {
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
    public void renameBranch(@Nonnull final ProjectDescriptor project,
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
    public void listLocalBranches(@Nonnull final ProjectDescriptor project, final AsyncCallback<List<Branch>> callback) {
        listBranches(project, null, callback);
    }

    /**
     * List branches of a given type.
     *
     * @param project
     *         the project descriptor
     * @param whichBranches
     *         null -> list local branches; "r" -> list remote branches; "a" -> list all branches
     * @param callback
     */
    private void listBranches(final ProjectDescriptor project, final String whichBranches, final AsyncCallback<List<Branch>> callback) {
        final Unmarshallable<Array<com.codenvy.ide.ext.git.shared.Branch>> unMarshaller =
                dtoUnmarshallerFactory.newArrayUnmarshaller(com.codenvy.ide.ext.git.shared.Branch.class);
        this.service.branchList(project, whichBranches,
                                new AsyncRequestCallback<Array<com.codenvy.ide.ext.git.shared.Branch>>(unMarshaller) {
                                    @Override
                                    protected void onSuccess(final Array<com.codenvy.ide.ext.git.shared.Branch> branches) {
                                        final List<Branch> result = new ArrayList<>();
                                        for (final com.codenvy.ide.ext.git.shared.Branch branch : branches.asIterable()) {
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

    @Override
    public void listRemotes(final ProjectDescriptor project, final AsyncCallback<List<Remote>> callback) {
        final Unmarshallable<Array<com.codenvy.ide.ext.git.shared.Remote>> unMarshaller
            = dtoUnmarshallerFactory.newArrayUnmarshaller(com.codenvy.ide.ext.git.shared.Remote.class);
        this.service.remoteList(project, null, false,
                                new AsyncRequestCallback<Array<com.codenvy.ide.ext.git.shared.Remote>>(unMarshaller) {
                                    @Override
                                    protected void onSuccess(final Array<com.codenvy.ide.ext.git.shared.Remote> remotes) {
                                        final List<Remote> result = new ArrayList<>();
                                        for (final com.codenvy.ide.ext.git.shared.Remote remote : remotes.asIterable()) {
                                            result.add(fromGitRemote(remote));
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
     * @param gitBracnh
     *         the object to convert
     * @return the converted object
     */
    private Branch fromGitBranch(final com.codenvy.ide.ext.git.shared.Branch gitBranch) {
        final Branch branch = GitVcsService.this.dtoFactory.createDto(Branch.class);
        branch.withActive(gitBranch.isActive()).withRemote(gitBranch.isRemote())
              .withName(gitBranch.getName()).withDisplayName(gitBranch.getDisplayName());
        return branch;
    }

    private Remote fromGitRemote(final com.codenvy.ide.ext.git.shared.Remote gitRemote) {
        final Remote remote = GitVcsService.this.dtoFactory.createDto(Remote.class);
        remote.withName(gitRemote.getName()).withUrl(gitRemote.getUrl());
        return remote;
    }

    @Override
    public void addRemote(final ProjectDescriptor project, final String remote,
                          final String remoteUrl, final AsyncCallback<Void> callback) {
        this.service.remoteAdd(project, remote, remoteUrl, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String notUsed) {
                callback.onSuccess(null);
            }
            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void deleteRemote(final ProjectDescriptor project, final String remote, final AsyncCallback<Void> callback) {
        this.service.remoteDelete(project, remote, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String notUsed) {
                callback.onSuccess(null);
            }
            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }
}
