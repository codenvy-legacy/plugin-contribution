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
package com.codenvy.plugin.contribution.vcs.client.hosting.dto;

import com.codenvy.dto.shared.DTO;

@DTO
public interface PullRequest {
    String getId();

    PullRequest withId(String id);

    String getUrl();

    PullRequest withUrl(String url);

    String getHtmlUrl();

    PullRequest withHtmlUrl(String htmlUrl);

    String getNumber();

    PullRequest withNumber(String number);

    String getState();

    PullRequest withState(String state);

    PullRequestHead getHead();

    PullRequest withHead(PullRequestHead head);

    /**
     * Tells if the pull request is merged.
     * 
     * @return true iff the pull request is merged
     */
    boolean getMerged();

    PullRequest withMerged(boolean merged);

    /**
     * Tells which user merged the pull request (if it was).
     * 
     * @return the user
     */
    HostUser getMergedBy();

    PullRequest withMergedBy(HostUser user);

    /**
     * Tells if the pull request is mergeable.
     * 
     * @return true iff the merge can be done automatically
     */
    boolean getMergeable();

    PullRequest withMergeable(boolean mergeable);
}
