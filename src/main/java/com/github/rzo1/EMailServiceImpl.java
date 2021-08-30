package com.github.rzo1;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

@Stateless
public class EMailServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMailServiceImpl.class);

    private static final String HEADER_HTML_EMAIL = "text/html; charset=UTF-8";
    private static final String RESOURCE_SEPARATOR = "/";
    private static final String VELOCITY_RESOURCE_CLASS_LOADER_KEY= "resource.loader.class.class";
    private static final String VELOCITY_RESOURCE_CLASS_LOADER= "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
    private static final String VELOCITY_RESOURCE_LOADER_KEY = "resource.loaders";
    private static final String VELOCITY_RESOURCE_LOADER = "class";


    @Resource(mappedName = "java:comp/env/tomee/mail/exampleSMTP")
    private Session mailSession;

    private VelocityEngine velocityEngine;

    //TODO Change me
    private String mailFrom = "no-reply@test.xyz";

    private String templateDirectory = "templates";

    private String templateResourcePath;


    public EMailServiceImpl() {}

    @PostConstruct
    public void init() {
        templateResourcePath = templateDirectory + RESOURCE_SEPARATOR;

        // Properties documented here: https://wiki.apache.org/velocity/VelocityAndWeblogic
        // and hints by "frank": http://stackoverflow.com/questions/9051413/unable-to-find-velocity-template-resources
        Properties prop = new Properties();
        prop.setProperty(VELOCITY_RESOURCE_LOADER_KEY, VELOCITY_RESOURCE_LOADER);
        prop.setProperty(VELOCITY_RESOURCE_CLASS_LOADER_KEY, VELOCITY_RESOURCE_CLASS_LOADER);

        velocityEngine = new VelocityEngine();
        velocityEngine.init(prop);

        /* Ensures that smtp authentication mechanism works as configured */
        boolean authenticate = "true".equals(mailSession.getProperty("mail.smtp.auth"));
        if (authenticate) {
            final String username = mailSession.getProperty("mail.smtp.user");
            final String password = mailSession.getProperty("mail.smtp.password");

            final URLName url = new URLName(
                    mailSession.getProperty("mail.transport.protocol"),
                    mailSession.getProperty("mail.smtp.host"), -1, null,
                    username, null);

            // Important line here as it configures the 'mailSession' object to hold the credentials
            mailSession.setPasswordAuthentication(url, new PasswordAuthentication(username, password));
        } else {
            LOGGER.warn("Using EMailService without SMTP auth configured. This might be valid, but could also be dangerous!");
        }

    }

    public EMail createMailHTML(Collection<String> toEmail, String subject, String body, Collection<String> toCC, Collection<String> toBCC) {
        EMail mail = new EMail(MailType.MAIL_HTML, toEmail, subject, body, toCC, toBCC);
        mail.setMailFrom(mailFrom);
        return mail;
    }

    public void sendMail(EMail eMail, String htmlTemplate, Map<String, String> templateResources) {
        if(!eMail.getMailType().equals(MailType.MAIL_HTML)) {
            throw new RuntimeException("You can't send an HTML eMail with the Mail instance provided: '"
                    + eMail.getMailType().toString()+"'!");
        } else {
            htmlTemplate = templateResourcePath + htmlTemplate;
            try {
                MimeMessage message = createMimeMessage(eMail);
                // applying the template
                if (!velocityEngine.resourceExists(htmlTemplate)) {
                    throw new RuntimeException("Could not find the given email template '"+htmlTemplate+"' in the classpath.");
                }
                else {
                    Template template = velocityEngine.getTemplate(htmlTemplate);
                    VelocityContext velocityContext = new VelocityContext();
                    for(Map.Entry<String, String> templateEntry: templateResources.entrySet()) {
                        velocityContext.put(templateEntry.getKey(), templateEntry.getValue());
                    }

                    StringWriter stringWriter = new StringWriter();
                    template.merge(velocityContext, stringWriter);
                    // setting the eMail's content as HTML mail body
                    Multipart mp = new MimeMultipart();
                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(stringWriter.toString(), HEADER_HTML_EMAIL);
                    mp.addBodyPart(htmlPart);
                    message.setContent(mp);

                    Transport.send(message);
                    // mark this eMail as sent with the current date
                    eMail.setSentDate(new Date());
                }

            } catch (MessagingException ex) {
                LOGGER.warn("Could not send template HTML eMail: {}", ex.getLocalizedMessage());
                throw new RuntimeException(ex.getLocalizedMessage(), ex);
            }
        }
    }

    private MimeMessage createMimeMessage(EMail eMail) throws MessagingException {
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(mailFrom));
        for(String mailTo : eMail.getMailTo()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
        }

        message.setSubject(eMail.getMailSubject());
        message.setSentDate(new Date());

        for(String ccRecipient :eMail.getMailCc()) {
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccRecipient));
        }
        for(String bccRecipient :eMail.getMailBcc()) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccRecipient));
        }
        return message;
    }


    @PreDestroy
    public void close() {
        if(mailSession!=null) {
            mailSession = null;
        }
    }
}
