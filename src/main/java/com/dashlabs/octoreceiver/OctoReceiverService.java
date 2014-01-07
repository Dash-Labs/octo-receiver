package com.dashlabs.octoreceiver;

import com.dashlabs.octoreceiver.config.OctoReceiverConfiguration;
import com.dashlabs.octoreceiver.resources.OctoReceiverResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:05 PM
 */
public class OctoReceiverService extends Service<OctoReceiverConfiguration> {

    public static void main(String[] args) throws Exception {
        new OctoReceiverService().run(args);
    }

    @Override public void initialize(Bootstrap<OctoReceiverConfiguration> bootstrap) {
        bootstrap.setName("octo-receiver");
    }

    @Override public void run(OctoReceiverConfiguration configuration, Environment environment) throws Exception {

        OctoReceiverResource octoReceiverResource = new OctoReceiverResource(configuration.getScript());

        environment.addResource(octoReceiverResource);

    }
}
