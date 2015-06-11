package com.dashlabs.octoreceiver.config;

import io.dropwizard.Configuration;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotNull;

/**
 * Created by mpuri on 6/23/14
 */
public class CodeDeploymentConfiguration extends Configuration {

    private static final String DEFAULT_CODE_DEPLOYMENT_SCRIPT = "/home/ubuntu/octoreceiver/application.remote.deploy";

    private static final String DEFAULT_CODE_CHECKOUT_SCRIPT = " /home/ubuntu/octoreceiver/application.checkout";

    private final String loadBalancerName;

    private final String instanceGroupingTagValue;

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
        this(null, null, DEFAULT_CODE_DEPLOYMENT_SCRIPT, DEFAULT_CODE_CHECKOUT_SCRIPT, null, null, null);
    }

    public CodeDeploymentConfiguration(String loadBalancerName, String instanceGroupingTagValue, String codeDeploymentScript, String codeCheckoutScript,
                                       String deploymentEmail, String projectName, String environment) {
        this.loadBalancerName = loadBalancerName;
        this.instanceGroupingTagValue = instanceGroupingTagValue;
        this.codeDeploymentScript = codeDeploymentScript;
        this.codeCheckoutScript = codeCheckoutScript;
        this.deploymentEmail = deploymentEmail;
        this.projectName = projectName;
        this.environment = environment;
    }

    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public String getInstanceGroupingTagValue() {
        return instanceGroupingTagValue;
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

    @ValidationMethod(message = "Must specify one of either loadBalancerName or instanceGroupingTagValue")
    public boolean isLoadBalancerNameOrInstanceGroupingTagValuePresent() {
        return ((loadBalancerName != null) || (instanceGroupingTagValue != null));
    }

    @Override public String toString() {
        return "CodeDeploymentConfiguration{" +
                "loadBalancerName='" + loadBalancerName + '\'' +
                ", instanceGroupingTagValue='" + instanceGroupingTagValue + '\'' +
                ", codeDeploymentScript='" + codeDeploymentScript + '\'' +
                ", codeCheckoutScript='" + codeCheckoutScript + '\'' +
                ", deploymentEmail='" + deploymentEmail + '\'' +
                ", projectName='" + projectName + '\'' +
                ", environment='" + environment + '\'' +
                '}';
    }
}
