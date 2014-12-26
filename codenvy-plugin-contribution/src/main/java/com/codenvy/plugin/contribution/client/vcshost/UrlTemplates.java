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

import com.google.gwt.i18n.client.Messages;

/**
 * Templates for repository URLs.
 */
public interface UrlTemplates extends Messages {

    /**
     * The git URL to a github repository.
     *
     * @param user
     *         the user name
     * @param repo
     *         the repository name
     * @return the URL
     */
    @DefaultMessage("git@github.com:{0}/{1}.git")
    String gitRemoteTemplate(String user, String repo);
}
