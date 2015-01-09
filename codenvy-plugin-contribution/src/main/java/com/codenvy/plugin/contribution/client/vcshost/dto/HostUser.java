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
public interface HostUser {
    String getId();

    void setId(String id);

    HostUser withId(String id);

    String getName();

    void setName(String name);

    HostUser withName(String name);

    String getLogin();

    void setLogin(String login);

    HostUser withLogin(String login);

    String getUrl();

    void setUrl(String url);

    HostUser withUrl(String url);
}
