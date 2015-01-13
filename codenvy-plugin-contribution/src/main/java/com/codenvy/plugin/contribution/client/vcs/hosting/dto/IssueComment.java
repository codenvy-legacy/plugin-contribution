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
package com.codenvy.plugin.contribution.client.vcs.hosting.dto;

import com.codenvy.dto.shared.DTO;

@DTO
public interface IssueComment {
    /**
     * Get comment id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(String id);

    IssueComment withId(String id);

    /**
     * Get comment URL.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(String url);

    IssueComment withUrl(String url);

    /**
     * Get comment body.
     *
     * @return {@link String} body
     */
    String getBody();

    void setBody(String body);

    IssueComment withBody(String body);
}
