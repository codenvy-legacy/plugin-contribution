package com.codenvy.plugin.contribution.client.vcs.hosting;

import javax.annotation.Nonnull;

/**
 * Exception raised when a pull request is created with no commits.
 *
 * @author Kevin Pollet
 */
public class NoCommitsInPullRequestException extends Exception {
    /**
     * Constructs an instance of {@link com.codenvy.plugin.contribution.client.vcs.hosting.NoCommitsInPullRequestException}.
     *
     * @param headBranch
     *         the head branch name.
     * @param baseBranch
     *         the base branch name.
     */
    public NoCommitsInPullRequestException(@Nonnull final String headBranch, @Nonnull final String baseBranch) {
        super("No commits between " + baseBranch + " and " + headBranch);
    }
}
