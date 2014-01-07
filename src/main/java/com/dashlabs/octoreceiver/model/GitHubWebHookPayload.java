package com.dashlabs.octoreceiver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:28 PM
 */
public class GitHubWebHookPayload {

    private final String after;

    private final String before;

    private final List<GitHubCommit> commits;

    private final String compare;

    private final Boolean created;

    private final Boolean deleted;

    private final Boolean forced;

    @JsonProperty("head_commit")
    private final GitHubCommit headCommit;

    private final GitHubUser pusher;

    private final String ref;

    private final GitHubRepo repository;

    private GitHubWebHookPayload() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public GitHubWebHookPayload(String after, String before, List<GitHubCommit> commits, String compare, Boolean created,
                                Boolean deleted, Boolean forced, GitHubCommit headCommit, GitHubUser pusher, String ref,
                                GitHubRepo repository) {
        this.after = after;
        this.before = before;
        this.commits = commits;
        this.compare = compare;
        this.created = created;
        this.deleted = deleted;
        this.forced = forced;
        this.headCommit = headCommit;
        this.pusher = pusher;
        this.ref = ref;
        this.repository = repository;
    }

    public String getAfter() {
        return after;
    }

    public String getBefore() {
        return before;
    }

    public List<GitHubCommit> getCommits() {
        return commits;
    }

    public String getCompare() {
        return compare;
    }

    public Boolean getCreated() {
        return created;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Boolean getForced() {
        return forced;
    }

    public GitHubCommit getHeadCommit() {
        return headCommit;
    }

    public GitHubUser getPusher() {
        return pusher;
    }

    public String getRef() {
        return ref;
    }

    public GitHubRepo getRepository() {
        return repository;
    }
}
