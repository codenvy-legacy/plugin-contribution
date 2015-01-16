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
public interface PullRequestHead {
    /**
     * Get pull request head label.
     *
     * @return {@link String} label
     */
    String getLabel();

    void setLabel(String label);

    PullRequestHead withLabel(String label);

    /**
     * Get pull request head ref.
     *
     * @return {@link String} ref
     */
    String getRef();

    void setRef(String ref);

    PullRequestHead withRef(String ref);

    /**
     * Get pull request head sha.
     *
     * @return {@link String} sha
     */
    String getSha();

    void setSha(String sha);

    PullRequestHead withSha(String sha);
}
