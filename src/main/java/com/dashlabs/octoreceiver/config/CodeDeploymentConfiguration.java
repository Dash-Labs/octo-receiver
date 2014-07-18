package com.dashlabs.octoreceiver.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by mpuri on 6/23/14.
 */
public class CodeDeploymentConfiguration extends Configuration {

    @NotNull
    private final String loadBalancerName;

    @NotNull
    private final String codeDeploymentScript;

    @NotNull
    private final String codeCheckoutScript;

    @NotNull
    private final String deploymentEmail;

    @NotNull
    private final String projectName;

    @NotNull
    private final String environment;

    private CodeDeploymentConfiguration() {
        this(null, null, null, null, null, null);
    }

    public CodeDeploymentConfiguration(String loadBalancerName, String codeDeploymentScript, String codeCheckoutScript,
                                       String deploymentEmail, String projectName, String environment) {
        this.loadBalancerName = loadBalancerName;
        this.codeDeploymentScript = codeDeploymentScript;
        this.codeCheckoutScript = codeCheckoutScript;
        this.deploymentEmail = deploymentEmail;
        this.projectName = projectName;
        this.environment = environment;
    }

    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public String getCodeDeploymentScript() {
        return codeDeploymentScript;
    }

    public String getCodeCheckoutScript() {
        return codeCheckoutScript;
    }

    public String getDeploymentEmail() {
        return deploymentEmail;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getEnvironment() {
        return environment;
    }
}
