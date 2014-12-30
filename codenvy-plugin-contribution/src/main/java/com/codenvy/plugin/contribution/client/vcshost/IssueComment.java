package com.codenvy.plugin.contribution.client.vcshost;

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
