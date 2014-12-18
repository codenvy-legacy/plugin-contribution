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
package com.codenvy.plugin.contribution.client.vcshost;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RepositoryHost {
    void getUserInfo(AsyncCallback<HostUser> callback);

    void getRepositoriesList(AsyncCallback<List<Repository>> callback);

    void getForks(String login, String repository, AsyncCallback<List<Repository>> callback);

    void fork(String login, String repository, AsyncCallback<Repository> callback);

    String makeRemoteUrl(String username, String repository);

    /**
     * Add a comment to a pull request.
     * @param username the username of the owner
     * @param repository the repository
     * @param pullRequestId the id f the pull request
     * @param commentText the text of the comment
     * @param callback the callback
     */
    void commentPullRequest(String username, String repository,
                            String pullRequestId, String commentText, AsyncCallback<Void> callback);
}
