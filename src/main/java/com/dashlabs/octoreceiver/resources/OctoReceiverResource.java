package com.dashlabs.octoreceiver.resources;

import com.dashlabs.octoreceiver.model.GitHubWebHookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:05 PM
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
public class OctoReceiverResource {

    private static final Logger LOG = LoggerFactory.getLogger(OctoReceiverResource.class);

    private final String script;

    public OctoReceiverResource(String script) {
        this.script = script;
    }

    @POST
    public void receiveHook(GitHubWebHookPayload payload) {
        LOG.info("Commit before - ^cyan^{}^r^", payload.getBefore());
        LOG.info("Commit after - ^cyan^{}^r^", payload.getAfter());
    }

}
