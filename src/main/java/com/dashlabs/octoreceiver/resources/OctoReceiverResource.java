package com.dashlabs.octoreceiver.resources;

import com.dashlabs.octoreceiver.OctoReceiverEmailer;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: blangel
 * Date: 1/7/14
 * Time: 6:05 PM
 */
@Path("/")
public class OctoReceiverResource {

    private static class ProcessResult {

        private final StringBuilder logs;

        private final int result;

        private ProcessResult(StringBuilder logs, int result) {
            this.logs = logs;
            this.result = result;
        }
    }

    private static class GitHubWebHookPayloadCompact {

        private static final String REPO = "${repo}";

        private static final String REPO_DEP = "${repo_dep}";

        private static final String BRANCH = "${branch}";

        private static final String TAG = "${tag}";

        private static final String HEAD_COMMIT_MSG = "${head_commit_msg}";

        private static final String AUTHOR = "${author}";

        private final String repoPath;

        private final String repoDepPath;

        private final String branch;

        private final String tag;

        private final String headCommitMessage;

        private final String author;

        private GitHubWebHookPayloadCompact(String repoPath, String repoDepPath, String branch, String tag, String headCommitMessage,
                                            String author) {
            this.repoPath = repoPath;
            this.repoDepPath = repoDepPath;
            this.branch = branch;
            this.tag = tag;
            this.headCommitMessage = headCommitMessage;
            this.author = author;
        }

        private String[] getArgs() {
            return new String[] { repoPath, repoDepPath, branch, tag, headCommitMessage, author };
        }

        private String filter(String value) {
            return value.replaceAll(Pattern.quote(REPO), repoPath)
                    .replaceAll(Pattern.quote(REPO_DEP), repoDepPath)
                    .replaceAll(Pattern.quote(BRANCH), branch)
                    .replaceAll(Pattern.quote(TAG), tag)
                    .replaceAll(Pattern.quote(HEAD_COMMIT_MSG), headCommitMessage)
                    .replaceAll(Pattern.quote(AUTHOR), author);
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(OctoReceiverResource.class);

    private static final Pattern GIT_REF_BRANCH_REGEX = Pattern.compile("refs/heads/(.*)");

    private static final Pattern GIT_REF_TAG_REGEX = Pattern.compile("refs/tags/(.*)");

    private final ObjectMapper mapper;

    private final String script;

    private final boolean onlyUseScriptArgs;

    private final String[] supplementalArgs;

    private final Map<String, String> repositoryMapping;

    private final Map<String, String> repositoryDependencyMapping;

    private final OctoReceiverEmailer emailer;

    private final String failureSubjectPrefix;

    private final String failureBodyPrefix;

    private final String failureEmail;

    private final Executor executor;

    public OctoReceiverResource(ObjectMapper mapper, String script, Boolean onlyUseScriptArgs, Map<String, String> repositoryMapping,
                                Map<String, String> repositoryDependencyMapping, OctoReceiverEmailer emailer,
                                String failureSubjectPrefix, String failureBodyPrefix, String failureEmail,
                                Executor executor) {
        this.mapper = mapper;
        this.script = getScript(script);
        this.onlyUseScriptArgs = (onlyUseScriptArgs == null ? false : onlyUseScriptArgs);
        this.supplementalArgs = getSupplementalArgs(script);
        this.repositoryMapping = (repositoryMapping == null ? Collections.<String, String>emptyMap() : repositoryMapping);
        this.repositoryDependencyMapping = (repositoryDependencyMapping == null ? Collections.<String, String>emptyMap() : repositoryDependencyMapping);
        this.emailer = emailer;
        this.failureSubjectPrefix = failureSubjectPrefix;
        this.failureBodyPrefix = failureBodyPrefix;
        this.failureEmail = failureEmail;
        this.executor = executor;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void receiveHook(@FormParam("payload") String payload) throws Exception {
        if ((payload == null) || payload.isEmpty()) {
            return;
        }
        final GitHubWebHookPayload gitHubPayload = mapper.readValue(payload, GitHubWebHookPayload.class);
        executor.execute(new Runnable() {
            @Override public void run() {
                try {
                    handleGitHubPayload(gitHubPayload);
                } catch (Exception e) {
                    LOG.error("Failed processing [ {} ]", gitHubPayload);
                    emailer.sendMessage(failureSubjectPrefix, failureBodyPrefix, String.format("%s", e.getMessage()), gitHubPayload, failureEmail);
                }
            }
        });
    }

    private void handleGitHubPayload(GitHubWebHookPayload gitHubPayload) throws Exception {
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
        GitHubWebHookPayloadCompact payloadCompact = new GitHubWebHookPayloadCompact(repoPath.or(""), repoDepPath.or(""), branch.or(""),
                tag.or(""), headCommitMessage, author);
        ProcessBuilder processBuilder = new ProcessBuilder(getProcessArgs(payloadCompact)).redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        ProcessResult result = waitFor(prefix, reader, process);
        if (result.result != 0) {
            LOG.error("{} {} - failed with result code [ {} ]", prefix, script, result.result);
            String htmlLogs = result.logs.toString().replaceAll("\n", "<br/>");
            emailer.sendMessage(failureSubjectPrefix, failureBodyPrefix, htmlLogs, gitHubPayload, failureEmail);
        }
    }

    private ProcessResult waitFor(String prefix, BufferedReader reader, Process process) throws IOException, InterruptedException {
        StringBuilder processOutput = new StringBuilder();
        // take the child's input and reformat for output on parent process
        String processStdoutLine;
        while ((processStdoutLine = reader.readLine()) != null) {
            processOutput.append(processStdoutLine);
            processOutput.append('\n');
            LOG.info("{} - {}", prefix, processStdoutLine);
        }
        return new ProcessResult(processOutput, process.waitFor());
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

    private String getScript(String input) {
        if (!input.contains(" ")) {
            return input;
        }
        String[] parsed = input.split(" ");
        return parsed[0];
    }

    private String[] getSupplementalArgs(String input) {
        if (!input.contains(" ")) {
            return new String[0];
        }
        String[] parsed = input.split(" ");
        return Arrays.copyOfRange(parsed, 1, parsed.length);
    }

    private String[] getProcessArgs(GitHubWebHookPayloadCompact gitHubWebHookPayloadCompact) {
        String[] args = gitHubWebHookPayloadCompact.getArgs();
        int argsLength = (onlyUseScriptArgs || (args == null) ? 0 : args.length);
        String[] filtered = filterSupplementalArgs(gitHubWebHookPayloadCompact);
        String[] combined = new String[filtered.length + argsLength + 1];
        combined[0] = script;
        System.arraycopy(filtered, 0, combined, 1, filtered.length);
        if (argsLength > 0) {
            System.arraycopy(args, 0, combined, filtered.length + 1, args.length);
        }
        return combined;
    }

    private String[] filterSupplementalArgs(GitHubWebHookPayloadCompact gitHubWebHookPayloadCompact) {
        String[] filtered = new String[supplementalArgs.length];
        for (int i = 0; i < supplementalArgs.length; i++) {
            filtered[i] = gitHubWebHookPayloadCompact.filter(supplementalArgs[i]);
        }
        return filtered;
    }

}
