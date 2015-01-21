package com.codenvy.plugin.contribution.client.vcs.hosting;

import javax.annotation.Nonnull;

/**
 * Exception raised when a pull request already exists for a branch.
 *
 * @author Kevin Pollet
 */
public class PullRequestAlreadyExistsException extends Exception {
    /**
     * Constructs an instance of {@link com.codenvy.plugin.contribution.client.vcs.hosting.PullRequestAlreadyExistsException}.
     *
     * @param headBranch
     *         the head branch name.
     */
    public PullRequestAlreadyExistsException(@Nonnull final String headBranch) {
        super("A pull request for " + headBranch + " already exists");
    }
}
