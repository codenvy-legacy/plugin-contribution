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
package com.codenvy.plugin.contribution.client.vcs.hosting;

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
     * @param reviewFactoryUrl
     *         the review factory url.
     * @return the formatted version of the review factory url
     */
    @DefaultMessage(
            "[![Review](https://rawgit.com/kevinpollet/2ced2ffddf1636f9116c/raw/85408e7a0233f54d4b3fda1fd5c98afb2bdeb3e8/codenvy-review.svg)]({0})")
    String formattedReviewFactoryUrlTemplate(String reviewFactoryUrl);
}
