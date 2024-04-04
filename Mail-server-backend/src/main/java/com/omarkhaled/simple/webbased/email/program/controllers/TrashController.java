package com.omarkhaled.simple.webbased.email.program.controllers;

import com.omarkhaled.simple.webbased.email.program.classes.Attachment;
import com.omarkhaled.simple.webbased.email.program.classes.Mail;
import com.omarkhaled.simple.webbased.email.program.classes.User;
import com.omarkhaled.simple.webbased.email.program.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping
@CrossOrigin
public class TrashController {

    private final UsersService usersService;
    private final TrashService trashService;

    private final SortingService sortingService;

    private final FilterService filterService;

    private final SearchingService searchingService;
    private final AttachmentService attachmentService;

    @Autowired
    public TrashController(UsersService usersService, TrashService trashService, SortingService sortingService, FilterService filterService, SearchingService searchingService, AttachmentService attachmentService) {
        this.usersService = usersService;
        this.trashService = trashService;
        this.sortingService = sortingService;
        this.filterService = filterService;
        this.searchingService = searchingService;
        this.attachmentService = attachmentService;
    }

    //get trashed mails
    @GetMapping ("/mails/trash")
    public Collection<Mail> getTrash (@RequestParam String id){
        User user = usersService.getUser(id);

        if(user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        List<String> ids = trashService.destroyMails(trashService.getMails(id, usersService.getUsersDB()));

        trashService.deleteMails(id, ids, usersService.getUsersDB());

        return trashService.getMails(id, usersService.getUsersDB());
    }

    //download attachment
    @GetMapping ("/mail/attachments/trash/download")
    public ResponseEntity<byte[]> download(@RequestParam String id, @RequestParam String mailId, @RequestParam String attachmentID){
        Mail mail = trashService.getMail(id, mailId, usersService.getUsersDB());
        Attachment attachment = attachmentService.getAttachment(mail, attachmentID);
        //check if attachment not found
        if (attachment == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        //data of attachment
        byte[] data = attachment.getData();
        //headers of attachment
        HttpHeaders headers = new HttpHeaders();
        //set content type of attachment
        headers.setContentType(MediaType.valueOf(attachment.getContentType()));
        //build content disposition
        ContentDisposition build = ContentDisposition
                .builder("attachment")
                .filename(attachment.getAttachmentName())
                .build();
        //set content disposition builder for header
        headers.setContentDisposition(build);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    //sort mails
    @GetMapping ("/mails/trash/sort")
    public Collection<Mail> sortInbox(@RequestParam String id, @RequestParam String type){
        Collection<Mail> mails = trashService.getMails(id, usersService.getUsersDB());
        sortingService.setStrategy(type);
        return sortingService.getMails(mails);
    }

    //filter mails
    @GetMapping ("/trash/filter")
    public Collection<Mail> filterInbox(@RequestParam String id, @RequestParam String criteria, @RequestParam String keyWord){
        String[] criteriaArray = criteria.split("-");
        String[] keyWordArray = keyWord.split("-");

        Collection<Mail> mails = trashService.getMails(id, usersService.getUsersDB());

        Collection<Mail> filteredMails = filterService.getFilter(criteriaArray, keyWordArray, mails);

        for (Mail mail: filteredMails)
            if (mail == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return filteredMails;
    }

    //search mails
    @GetMapping ("/mails/trash/search")
    public Collection<Mail> searchInbox(@RequestParam String id, @RequestParam String type, @RequestParam String keyWord){
        Collection<Mail> mails = trashService.getMails(id, usersService.getUsersDB());
        searchingService.setStrategy(type);
        return searchingService.getMails(keyWord, mails);
    }

    //delete trashed mail
    @DeleteMapping ("/mail/delete")
    public void delete(@RequestParam String id, @RequestParam List<String> ids){
        List<Mail> mails = trashService.deleteMails(id, ids, usersService.getUsersDB());

        for (Mail mail : mails)
            if(mail == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
