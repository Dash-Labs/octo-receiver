package com.dashlabs.octoreceiver.health;

import com.dashlabs.octoreceiver.resources.OctoReceiverResource;
import com.yammer.metrics.core.HealthCheck;

/**
 * User: blangel
 * Date: 1/8/14
 * Time: 9:30 AM
 */
public class OctoReceiverHealthCheck extends HealthCheck {

    private final OctoReceiverResource resource;

    public OctoReceiverHealthCheck(OctoReceiverResource resource) {
        super("octo-receiver-resource");
        this.resource = resource;
    }

    @Override protected Result check() throws Exception {
        try {
            resource.receiveHook(null); // verify null/empty-string handling
            resource.receiveHook("");
            return Result.healthy();
        } catch (Exception e) {
            return Result.unhealthy(e);
        }
    }

}
