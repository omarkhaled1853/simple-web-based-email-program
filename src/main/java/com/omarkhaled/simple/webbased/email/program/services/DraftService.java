package com.omarkhaled.simple.webbased.email.program.services;

import com.omarkhaled.simple.webbased.email.program.classes.Mail;
import com.omarkhaled.simple.webbased.email.program.classes.User;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DraftService {
    //build mail
    public Mail buildMail(Mail mail){
        return new Mail
                .Builder()
                .setId(UUID.randomUUID().toString())
                .setReceiver(mail.getReceiver())
                .setSender(mail.getSender())
                .setSubject(mail.getSubject())
                .setContent(mail.getContent())
                .setAttachments(mail.getAttachments())
                .setPriority(mail.getPriority())
                .setDate(LocalDateTime.now().toString())
                .build();
    }

    //get mails
    public Collection<Mail> getMails(String id, Map<String, User> usersDB){
        return usersDB.get(id).getDraft().values();
    }

    //draft mail
    public void draftMail(Mail mail, Map<String, User> usersDB){
        usersDB.get(mail.getSender()).getDraft().put(mail.getId(), mail);
    }

    //delete mail
    public List<Mail> deleteMails(String id, List<String> ids, Map<String, User> usersDB){
        List<Mail> mails = new ArrayList<>();
        for (String maiId : ids){
            mails.add(usersDB.get(id).getDraft().remove(maiId));
        }
        return mails;
    }

}
