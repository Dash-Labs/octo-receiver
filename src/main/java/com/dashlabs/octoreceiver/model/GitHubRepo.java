package com.dashlabs.octoreceiver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:35 PM
 */
public class GitHubRepo {

    @JsonProperty("created_at")
    private final Long createdAt;

    private final String description;

    private final Boolean fork;

    private final Integer forks;

    @JsonProperty("has_downloads")
    private final Boolean hasDownloads;

    @JsonProperty("has_issues")
    private final Boolean hasIssues;

    @JsonProperty("has_wiki")
    private final Boolean hasWiki;

    private final String homepage;

    private final String id;

    private final String language;

    @JsonProperty("master_branch")
    private final String masterBranch;

    private final String name;

    @JsonProperty("open_issues")
    private final Integer openIssues;

    private final GitHubUser owner;

    @JsonProperty("private")
    private final Boolean repoPrivate;

    @JsonProperty("pushed_at")
    private final Long pushedAt;

    private final Long size;

    private final Integer stargazers;

    private final String url;

    private final Integer watchers;

    private GitHubRepo() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public GitHubRepo(Long createdAt, String description, Boolean fork, Integer forks, Boolean hasDownloads, Boolean hasIssues,
                      Boolean hasWiki, String homepage, String id, String language, String masterBranch, String name,
                      Integer openIssues, GitHubUser owner, Boolean repoPrivate, Long pushedAt, Long size, Integer stargazers,
                      String url, Integer watchers) {
        this.createdAt = createdAt;
        this.description = description;
        this.fork = fork;
        this.forks = forks;
        this.hasDownloads = hasDownloads;
        this.hasIssues = hasIssues;
        this.hasWiki = hasWiki;
        this.homepage = homepage;
        this.id = id;
        this.language = language;
        this.masterBranch = masterBranch;
        this.name = name;
        this.openIssues = openIssues;
        this.owner = owner;
        this.repoPrivate = repoPrivate;
        this.pushedAt = pushedAt;
        this.size = size;
        this.stargazers = stargazers;
        this.url = url;
        this.watchers = watchers;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getFork() {
        return fork;
    }

    public Integer getForks() {
        return forks;
    }

    public Boolean getHasDownloads() {
        return hasDownloads;
    }

    public Boolean getHasIssues() {
        return hasIssues;
    }

    public Boolean getHasWiki() {
        return hasWiki;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public String getMasterBranch() {
        return masterBranch;
    }

    public String getName() {
        return name;
    }

    public Integer getOpenIssues() {
        return openIssues;
    }

    public GitHubUser getOwner() {
        return owner;
    }

    public Boolean getRepoPrivate() {
        return repoPrivate;
    }

    public Long getPushedAt() {
        return pushedAt;
    }

    public Long getSize() {
        return size;
    }

    public Integer getStargazers() {
        return stargazers;
    }

    public String getUrl() {
        return url;
    }

    public Integer getWatchers() {
        return watchers;
    }
}
