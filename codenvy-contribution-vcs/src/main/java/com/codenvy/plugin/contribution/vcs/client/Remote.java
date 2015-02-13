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
package com.codenvy.plugin.contribution.vcs.client;

import com.codenvy.dto.shared.DTO;

/**
 * Info about remote configuration.
 */
@DTO
public interface Remote {
    String getName();

    Remote withName(String name);

    String getUrl();

    Remote withUrl(String url);
}