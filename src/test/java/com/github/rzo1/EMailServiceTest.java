package com.github.rzo1;

import org.apache.openejb.junit5.RunWithEjbContainer;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWithEjbContainer
public class EMailServiceTest {

    @Inject
    private EMailServiceImpl eMailService;

    @Test
    public void testInject() {
        assertNotNull(eMailService);
    }

    @Test
    public void testSendMail() {
        Collection<String> toEmail = new HashSet<>();
        //TODO Adjust me
        toEmail.add("test@zowalla.com");
        String subject = "Geronimo Java Mail Integration Test";

        Collection<String> toCC = new HashSet<>();
        Collection<String> toBCC = new HashSet<>();

        EMail mail = eMailService.createMailHTML(toEmail, subject, "",toCC, toBCC);

        Map<String, String> templateResources = new HashMap<>();

        templateResources.put("exceptionDetails","Hello world");
        eMailService.sendMail(mail, "email-html-template.vm", templateResources );
    }
}
