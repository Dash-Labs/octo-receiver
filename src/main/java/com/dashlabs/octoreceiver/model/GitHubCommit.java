package com.dashlabs.octoreceiver.model;

import java.util.Date;
import java.util.List;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:29 PM
 */
public class GitHubCommit {

    private final List<String> added;

    private final GitHubUser author;

    private final GitHubUser committer;

    private final Boolean distinct;

    private final String id;

    private final String message;

    private final List<String> modified;

    private final List<String> removed;

    private final Date timestamp;

    private final String url;

    private GitHubCommit() {
        this(null, null, null, null, null, null, null, null, null, null);
    }

    public GitHubCommit(List<String> added, GitHubUser author, GitHubUser committer, Boolean distinct, String id, String message,
                        List<String> modified, List<String> removed, Date timestamp, String url) {
        this.added = added;
        this.author = author;
        this.committer = committer;
        this.distinct = distinct;
        this.id = id;
        this.message = message;
        this.modified = modified;
        this.removed = removed;
        this.timestamp = timestamp;
        this.url = url;
    }

    public List<String> getAdded() {
        return added;
    }

    public GitHubUser getAuthor() {
        return author;
    }

    public GitHubUser getCommitter() {
        return committer;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getModified() {
        return modified;
    }

    public List<String> getRemoved() {
        return removed;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUrl() {
        return url;
    }
}
