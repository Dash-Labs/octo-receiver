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
    private final String awsAccessKey;

    @NotNull
    private final String awsSecretKey;

    @NotNull
    private final String deploymentEmail;

    private CodeDeploymentConfiguration() {
        this(null, null, null, null, null, null);
    }

    public CodeDeploymentConfiguration(String loadBalancerName, String codeDeploymentScript, String codeCheckoutScript, String awsAccessKey, String awsSecretKey, String deploymentEmail) {
        this.loadBalancerName = loadBalancerName;
        this.codeDeploymentScript = codeDeploymentScript;
        this.codeCheckoutScript = codeCheckoutScript;
        this.deploymentEmail = deploymentEmail;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
    }

    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public String getCodeDeploymentScript() {
        return codeDeploymentScript;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public String getCodeCheckoutScript() {
        return codeCheckoutScript;
    }

    public String getDeploymentEmail() {
        return deploymentEmail;
    }
}
