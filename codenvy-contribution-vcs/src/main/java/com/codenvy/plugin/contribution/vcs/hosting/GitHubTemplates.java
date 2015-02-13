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
package com.codenvy.plugin.contribution.vcs.hosting;

import com.google.gwt.i18n.client.Messages;

/**
 * Templates for github constants.
 *
 * @author Kevin Pollet
 */
public interface GitHubTemplates extends Messages {
    /**
     * The SSH URL to a github repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the URL
     */
    @DefaultMessage("git@github.com:{0}/{1}.git")
    String sshUrlTemplate(String username, String repository);

    /**
     * The HTTP URL to a github repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the URL
     */
    @DefaultMessage("https://github.com/{0}/{1}.git")
    String httpUrlTemplate(String username, String repository);

    /**
     * The URL to a pull request.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @param pullRequestNumber
     *         the pull request number.
     * @return the URL
     */
    @DefaultMessage("https://github.com/{0}/{1}/pull/{2}")
    String pullRequestUrlTemplate(String username, String repository, String pullRequestNumber);

    /**
     * The formatted version of the review factory url using the GitHub markup language (markdown).
     *
     * @param protocol
     *         the protocol used http or https
     * @param host
     *         the host.
     * @param reviewFactoryUrl
     *         the review factory url.
     * @return the formatted version of the review factory url
     */
    @DefaultMessage("[![Review]({0}//{1}/factory/resources/codenvy-review.svg)]({2})")
    String formattedReviewFactoryUrlTemplate(String protocol, String host, String reviewFactoryUrl);
}
