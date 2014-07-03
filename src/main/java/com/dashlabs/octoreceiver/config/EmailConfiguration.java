package com.dashlabs.octoreceiver.config;

import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * User: blangel
 * Date: 1/9/14
 * Time: 8:52 AM
 */
public class EmailConfiguration extends Configuration {

    @NotEmpty
    private final String user;

    @NotEmpty
    private final String password;

    @NotEmpty
    private final String smtpHost;

    @NotEmpty
    private final String smtpPort;

    private EmailConfiguration() {
        this(null, null, null, null);
    }

    public EmailConfiguration(String user, String password, String smtpHost, String smtpPort) {
        this.user = user;
        this.password = password;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }
}
