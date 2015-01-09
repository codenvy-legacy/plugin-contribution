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
package com.codenvy.plugin.contribution.client.vcshost.dto;

import com.codenvy.dto.shared.DTO;

@DTO
public interface PullRequest {
    /**
     * Get pull request id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(String id);

    PullRequest withId(String id);

    /**
     * Get pull request URL.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(String url);

    PullRequest withUrl(String url);

    /**
     * Get pull request html URL.
     *
     * @return {@link String} html_url
     */
    String getHtmlUrl();

    void setHtmlUrl(String htmlUrl);

    PullRequest withHtmlUrl(String htmlUrl);

    /**
     * Get pull request number.
     *
     * @return {@link String} number
     */
    String getNumber();

    void setNumber(String number);

    PullRequest withNumber(String number);

    /**
     * Get pull request state.
     *
     * @return {@link String} state
     */
    String getState();

    void setState(String state);

    PullRequest withState(String state);
}
