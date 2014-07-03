package com.dashlabs.octoreceiver.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by mpuri on 6/23/14.
 */
public class CodeDeploymentConfiguration extends Configuration {

    @NotNull
    private String loadBalancerName;

    @NotNull
    private String codeDeploymentScript;

    @NotNull
    private String codeCheckoutScript;

    @NotNull
    private String awsAccessKey;

    @NotNull
    private String awsSecretKey;

    private CodeDeploymentConfiguration() {
        this(null, null, null, null);
    }

    public CodeDeploymentConfiguration(String loadBalancerName, String codeDeploymentScript, String awsAccessKey, String awsSecretKey) {
        this.loadBalancerName = loadBalancerName;
        this.codeDeploymentScript = codeDeploymentScript;
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
}
