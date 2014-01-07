package com.dashlabs.octoreceiver.model;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:31 PM
 */
public class GitHubUser {

    private final String email;

    private final String name;

    private final String username;

    private GitHubUser() {
        this(null, null, null);
    }

    public GitHubUser(String email, String name, String username) {
        this.email = email;
        this.name = name;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }
}
