package com.dashlabs.octoreceiver.tasks;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.*;
import com.dashlabs.octoreceiver.OctoReceiverEmailer;
import com.dashlabs.octoreceiver.config.CodeDeploymentConfiguration;
import com.dashlabs.octoreceiver.model.ProcessResult;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mpuri on 6/23/14.
 */
public class DeployCodeTask extends Task {

    private static final Logger LOG = LoggerFactory.getLogger(DeployCodeTask.class);

    private CodeDeploymentConfiguration configuration;

    private AmazonElasticLoadBalancingClient client;

    private AmazonEC2Client ec2Client;

    private final OctoReceiverEmailer emailer;

    public DeployCodeTask(CodeDeploymentConfiguration configuration, AmazonElasticLoadBalancingClient client, AmazonEC2Client ec2Client,
                          OctoReceiverEmailer emailer) {
        super("deploy");
        this.configuration = configuration;
        this.client = client;
        this.ec2Client = ec2Client;
        this.emailer = emailer;
    }

    @Override public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        LOG.info("Fetching the latest code for deployment ...");
        int result = invokeScript(configuration.getCodeCheckoutScript(), null);
        LOG.info("Result of invoking the code checkout script: {}", result);
        if (result != 0) {
            throw new RuntimeException(String.format("There was an error invoking the code checkout script. Status code [%d]", result));
        }
        List<String> loadBalancerNames = new ArrayList<String>(1);
        loadBalancerNames.add(configuration.getLoadBalancerName());
        DescribeLoadBalancersRequest loadBalancersRequest = new DescribeLoadBalancersRequest(loadBalancerNames);
        DescribeLoadBalancersResult loadBalancersResult = client.describeLoadBalancers(loadBalancersRequest);
        if (loadBalancersResult == null) {
            throw new IllegalArgumentException(
                    String.format("No load balancer found for the given name [%s]", configuration.getLoadBalancerName()));
        }
        List<LoadBalancerDescription> loadBalancerDescriptions = loadBalancersResult.getLoadBalancerDescriptions();
        if (loadBalancerDescriptions == null || loadBalancerDescriptions.isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not fetch load balancer information for the given name [%s]",
                    configuration.getLoadBalancerName()));
        }
        LoadBalancerDescription loadBalancerDescription = loadBalancerDescriptions.get(0); //we only fetch details on one load balancer
        if (loadBalancerDescription == null) {
            throw new IllegalArgumentException(String.format("Could not fetch load balancer information for the given name [%s]",
                    configuration.getLoadBalancerName()));
        }
        List<Instance> instances = loadBalancerDescription.getInstances();
        deploy(instances);
        LOG.info("Done deploying to all instances.");
        emailer.sendSuccessfulDeploymentMessage(configuration.getProjectName(), configuration.getEnvironment(), configuration.getDeploymentEmail());
    }

    /**
     * Deploys code to remote instances by performing the following steps:
     * <ul>
     * <li> Removes the instance from the load balancer </li>
     * <li> Invokes a shell script on the instance to redeploy</li>
     * <li> Adds the instance back to the load balancer</li>
     * </ul>
     *
     * @param instances the instances that need to be redeployed
     * @throws IOException
     * @throws InterruptedException
     */
    private void deploy(List<Instance> instances) throws IOException, InterruptedException {
        int result;
        Map<String, String> instanceAddresses = getInstanceIPAddresses(instances);
        for (Instance instance : instances) {
            LOG.info("Removing instance [{}] from the load balancer ", instance.getInstanceId());
            removeFromLoadBalancer(instance);
            LOG.info("Invoking remote deployment on the  instance [{}]", instance.getInstanceId());
            result = invokeScript(configuration.getCodeDeploymentScript(), instanceAddresses.get(instance.getInstanceId()));
            if (result != 0) {
                throw new RuntimeException(String.format("There was an error invoking the deployment script. Status code [%d]", result));
            }
            LOG.info("Adding back the instance to the load balancer [{}]", instance.getInstanceId());
            addToLoadBalancer(instance);
        }
    }

    /**
     * Fetches the Public IP address of the given ec2 instances
     *
     * @param instances the instances who'se ip addresses need to be retrieved
     * @return a map containing the instanceId as the key and the public ip address as the value
     */
    private Map<String, String> getInstanceIPAddresses(List<Instance> instances) {
        Map<String, String> instanceIPs = new HashMap<String, String>(instances.size(), 1.0f);
        DescribeInstancesRequest instancesRequest = new DescribeInstancesRequest();
        List<String> instanceIds = new ArrayList<String>(instances.size());
        for (Instance instance : instances) {
            instanceIds.add(instance.getInstanceId());
        }
        instancesRequest = instancesRequest.withInstanceIds(instanceIds);
        DescribeInstancesResult instancesResult = ec2Client.describeInstances(instancesRequest);
        for (Reservation reservation : instancesResult.getReservations()) {
            for (com.amazonaws.services.ec2.model.Instance instance : reservation.getInstances()) {
                instanceIPs.put(instance.getInstanceId(), instance.getPublicIpAddress());
            }
        }
        return instanceIPs;
    }

    /**
     * Removes the given instance from the load balancer
     *
     * @param instance
     */
    private void removeFromLoadBalancer(Instance instance) {
        List<Instance> instances = new ArrayList<Instance>(1);
        instances.add(instance);
        DeregisterInstancesFromLoadBalancerRequest request =
                new DeregisterInstancesFromLoadBalancerRequest(configuration.getLoadBalancerName(), instances);
        client.deregisterInstancesFromLoadBalancer(request);
    }

    /**
     * Registers the given instance with the load balancer
     *
     * @param instance
     */
    private void addToLoadBalancer(Instance instance) {
        List<Instance> instances = new ArrayList<Instance>(1);
        instances.add(instance);
        RegisterInstancesWithLoadBalancerRequest request =
                new RegisterInstancesWithLoadBalancerRequest(configuration.getLoadBalancerName(), instances);
        client.registerInstancesWithLoadBalancer(request);
    }

    private int invokeScript(String scriptName, String arg) throws IOException, InterruptedException {
        List<String> commands = new ArrayList<String>(2);
        commands.add(scriptName);
        if (arg != null) {
            commands.add(arg);
        }
        String[] commandArr = commands.toArray(new String[commands.size()]);
        ProcessBuilder processBuilder = new ProcessBuilder(commandArr).redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        ProcessResult result = waitFor(reader, process);
        return result.getResult();
    }

    private ProcessResult waitFor(BufferedReader reader, Process process) throws IOException, InterruptedException {
        StringBuilder processOutput = new StringBuilder();
        // take the child's input and reformat for output on parent process
        String processStdoutLine;
        while ((processStdoutLine = reader.readLine()) != null) {
            processOutput.append(processStdoutLine);
            processOutput.append('\n');
            LOG.info("{}", processStdoutLine);
        }
        return new ProcessResult(processOutput, process.waitFor());
    }
}
