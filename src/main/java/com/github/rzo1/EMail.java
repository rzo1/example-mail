package com.github.rzo1;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.Date;

@Data
@Getter
@Setter
@ToString
public class EMail {

    private MailType mailType;

    private String mailFrom;
    private String mailSubject;
    private String mailContent;

    private Collection<String> mailTo;
    private Collection<String> mailCc;
    private Collection<String> mailBcc;

    private Date sentDate;
    private String templateName;

    public EMail(MailType mailType, Collection<String> toRecipients, String subject, String mailContent, Collection<String> toCC, Collection<String> toBCC) {
        setMailTo(toRecipients);
        setMailSubject(subject);
        setMailContent(mailContent);
        setMailCc(toCC);
        setMailBcc(toBCC);
        setMailType(mailType);
    }

}
