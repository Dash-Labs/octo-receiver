package com.dashlabs.octoreceiver.config;

import com.yammer.dropwizard.config.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:06 PM
 */
public class OctoReceiverConfiguration extends Configuration {

    @Valid
    @NotNull
    private final String script;

    @Valid
    @NotNull
    private final Map<String, String> repositoryMapping;

    @Valid
    @NotNull
    private final Map<String, String> repositoryDependencyMapping;

    private OctoReceiverConfiguration() {
        this(null, null, null);
    }

    public OctoReceiverConfiguration(String script, Map<String, String> repositoryMapping, Map<String, String> repositoryDependencyMapping) {
        this.script = script;
        this.repositoryMapping = repositoryMapping;
        this.repositoryDependencyMapping = repositoryDependencyMapping;
    }

    public String getScript() {
        return script;
    }

    public Map<String, String> getRepositoryMapping() {
        return repositoryMapping;
    }

    public Map<String, String> getRepositoryDependencyMapping() {
        return repositoryDependencyMapping;
    }
}
