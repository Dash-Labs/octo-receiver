package com.dashlabs.octoreceiver.config;

import com.yammer.dropwizard.config.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:06 PM
 */
public class OctoReceiverConfiguration extends Configuration {

    @Valid
    @NotNull
    private final String script;

    private OctoReceiverConfiguration() {
        this(null);
    }

    public OctoReceiverConfiguration(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }
}
