package com.dashlabs.octoreceiver;

import com.dashlabs.octoreceiver.model.GitHubWebHookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * User: blangel
 * Date: 1/9/14
 * Time: 8:59 AM
 */
public class OctoReceiverEmailer {

    private static final Logger LOG = LoggerFactory.getLogger(OctoReceiverEmailer.class);

    private final String user;

    private final String password;

    private final String smtpHost;

    private final String smtpPort;

    public OctoReceiverEmailer(String user, String password, String smtpHost, String smtpPort) {
        this.user = user;
        this.password = password;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
    }

    public boolean sendMessage(String subjectPrefix, String bodyPrefix, String logs, GitHubWebHookPayload payload, String ... addresses) {
        if ((addresses == null) || (addresses.length < 1)) {
            return false;
        }
        String repositoryName = payload.getRepository().getName();
        String repositoryUrl = payload.getRepository().getUrl();
        String author = String.format("%s <%s>", payload.getPusher().getName(), payload.getPusher().getEmail());
        String commitId = payload.getHeadCommit().getId();
        String commitUrl = payload.getHeadCommit().getUrl();

        Session session = createSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            Address[] parsed = InternetAddress.parse(csv(addresses));
            message.setRecipients(Message.RecipientType.TO, parsed);
            message.setSubject(String.format("%s %s", subjectPrefix, repositoryName));
            String content = String.format("%s <a href=\"%s\">%s</a> after commit <a href=\"%s\">%s</a> by %s<br/><br/>%s", bodyPrefix, repositoryUrl,
                    repositoryName, commitUrl, commitId, author, logs);
            message.setContent(content, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException me) {
            LOG.error("Could not send email to {}", (Object[]) addresses);
            LOG.error(me.getMessage(), me);
            return false;
        }
        return true;
    }

    public boolean sendSuccessfulDeploymentMessage(String project, String environment, String ... addresses) {
        Session session = createSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            Address[] parsed = InternetAddress.parse(csv(addresses));
            message.setRecipients(Message.RecipientType.TO, parsed);
            message.setSubject(String.format("Deployment complete for %s (%s)", project, environment));
            String content = String.format("Deployment successfully completed for %s on %s.", project, environment);
            message.setContent(content, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException me) {
            LOG.error("Could not send email to {}", (Object[]) addresses);
            LOG.error(me.getMessage(), me);
            return false;
        }
        return true;
    }

    private String csv(String... addresses) {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (String address : addresses) {
            if (!first) {
                buffer.append(',');
            }
            buffer.append(address);
            first = false;
        }
        return buffer.toString();
    }

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.socketFactory.port", smtpPort);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", smtpPort);

        return Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });
    }

}
