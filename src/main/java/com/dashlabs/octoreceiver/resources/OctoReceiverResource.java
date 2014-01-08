package com.dashlabs.octoreceiver.resources;

import com.dashlabs.octoreceiver.model.GitHubWebHookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:05 PM
 */
@Path("/")
public class OctoReceiverResource {

    private static final Logger LOG = LoggerFactory.getLogger(OctoReceiverResource.class);

    private static final Pattern GIT_REF_BRANCH_REGEX = Pattern.compile("refs/heads/(.*)");

    private static final Pattern GIT_REF_TAG_REGEX = Pattern.compile("refs/tags/(.*)");

    private final ObjectMapper mapper;

    private final String script;

    private final Map<String, String> repositoryMapping;

    private final Map<String, String> repositoryDependencyMapping;

    public OctoReceiverResource(ObjectMapper mapper, String script, Map<String, String> repositoryMapping,
                                Map<String, String> repositoryDependencyMapping) {
        this.mapper = mapper;
        this.script = script;
        this.repositoryMapping = repositoryMapping;
        this.repositoryDependencyMapping = repositoryDependencyMapping;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void receiveHook(@FormParam("payload") String payload) throws Exception {
        if ((payload == null) || payload.isEmpty()) {
            return;
        }
        GitHubWebHookPayload gitHubPayload = mapper.readValue(payload, GitHubWebHookPayload.class);
        Optional<String> branch = getBranch(gitHubPayload.getRef());
        Optional<String> tag = getTag(gitHubPayload.getRef());
        String repositoryName = gitHubPayload.getRepository().getName();
        Optional<String> repoPath = Optional.fromNullable(repositoryMapping.get(repositoryName));
        String key = repositoryDependencyMapping.get(repositoryName);
        String value = null;
        if (key != null) {
            value = repositoryMapping.get(key);
        }
        Optional<String> repoDepPath = Optional.fromNullable(value);
        String headCommitMessage = gitHubPayload.getHeadCommit().getMessage();
        String author = String.format("%s <%s>", gitHubPayload.getPusher().getName(), gitHubPayload.getPusher().getEmail());
        // invoke the script with the arguments
        String prefix = String.format("%s%s@%s", branch.or(""), tag.or(""), repositoryName);
        ProcessBuilder processBuilder = new ProcessBuilder(script, repoPath.or(""), repoDepPath.or(""), branch.or(""), tag.or(""),
                headCommitMessage, author).redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        int result = waitFor(prefix, reader, process);
        if (result != 0) {
            LOG.error("{} {} - failed with result code [ {} ]", prefix, script, result);
            if (tag.isPresent()) {
                // TODO - send email
            } else {
                // TODO - send email
            }
        }
    }

    private int waitFor(String prefix, BufferedReader reader, Process process) throws IOException, InterruptedException {
        // take the child's input and reformat for output on parent process
        String processStdoutLine;
        while ((processStdoutLine = reader.readLine()) != null) {
            LOG.info("{} - {}", prefix, processStdoutLine);
        }
        return process.waitFor();
    }

    private Optional<String> getBranch(String ref) {
        return Optional.fromNullable(parseRef(ref, GIT_REF_BRANCH_REGEX));
    }

    private Optional<String> getTag(String ref) {
        return Optional.fromNullable(parseRef(ref, GIT_REF_TAG_REGEX));
    }

    private String parseRef(String ref, Pattern regex) {
        Matcher matcher = regex.matcher(ref);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

}
