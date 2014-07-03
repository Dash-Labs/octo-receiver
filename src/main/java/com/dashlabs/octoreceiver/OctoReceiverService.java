package com.dashlabs.octoreceiver;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.dashlabs.octoreceiver.config.CodeDeploymentConfiguration;
import com.dashlabs.octoreceiver.config.EmailConfiguration;
import com.dashlabs.octoreceiver.config.OctoReceiverConfiguration;
import com.dashlabs.octoreceiver.health.OctoReceiverHealthCheck;
import com.dashlabs.octoreceiver.resources.OctoReceiverResource;
import com.dashlabs.octoreceiver.tasks.DeployCodeTask;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:05 PM
 */
public class OctoReceiverService extends Application<OctoReceiverConfiguration> {

    public static void main(String[] args) throws Exception {
        new OctoReceiverService().run(args);
    }

    @Override public void initialize(Bootstrap<OctoReceiverConfiguration> bootstrap) { }

    @Override public void run(OctoReceiverConfiguration configuration, Environment environment) throws Exception {

        EmailConfiguration emailConfig = configuration.getEmailConfiguration();
        OctoReceiverEmailer emailer = new OctoReceiverEmailer(emailConfig.getUser(), emailConfig.getPassword(), emailConfig.getSmtpHost(), emailConfig.getSmtpPort());

        Executor executor = Executors.newSingleThreadExecutor();

        OctoReceiverResource octoReceiverResource = new OctoReceiverResource(environment.getObjectMapper(),
                configuration.getScript(), configuration.getOnlyUseScriptArgs(), configuration.getRepositoryMapping(), configuration.getRepositoryDependencyMapping(),
                emailer, configuration.getFailureSubjectPrefix(), configuration.getFailureBodyPrefix(), configuration.getFailureEmail(),
                executor);

        environment.jersey().register(octoReceiverResource);
        CodeDeploymentConfiguration codeDeploymentConfiguration = configuration.getCodeDeploymentConfiguration();
        if (codeDeploymentConfiguration != null) {
            AWSCredentials awsCredentials = new BasicAWSCredentials(codeDeploymentConfiguration.getAwsAccessKey(), codeDeploymentConfiguration.getAwsSecretKey());
            AmazonElasticLoadBalancingClient loadBalancingClient = new AmazonElasticLoadBalancingClient(awsCredentials);
            AmazonEC2Client ec2Client = new AmazonEC2Client(awsCredentials);
            DeployCodeTask deployCodeTask = new DeployCodeTask(codeDeploymentConfiguration, loadBalancingClient, ec2Client);
            environment.admin().addTask(deployCodeTask);
        }
        OctoReceiverHealthCheck check = new OctoReceiverHealthCheck(octoReceiverResource);
        environment.healthChecks().register("octo-receiver-resource", check);

    }
}
