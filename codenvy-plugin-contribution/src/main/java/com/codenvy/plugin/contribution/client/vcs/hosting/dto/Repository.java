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
public interface Repository {
    String getName();

    void setName(String name);

    Repository withName(String name);

    String getUrl();

    void setUrl(String url);

    Repository withUrl(String url);

    boolean isFork();

    void setFork(boolean isFork);

    Repository withFork(boolean isFork);

    boolean isPrivateRepo();

    void setPrivateRepo(boolean isPrivateRepo);

    Repository withPrivateRepo(boolean isPrivateRepo);
}
