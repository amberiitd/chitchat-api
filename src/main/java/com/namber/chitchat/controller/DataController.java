package com.namber.chitchat.controller;

import com.namber.chitchat.model.MessageQuery;
import com.namber.chitchat.model.OutputMessage;
import com.namber.chitchat.model.People;
import com.namber.chitchat.model.UINotification;
import com.namber.chitchat.model.dto.AppUserDTO;
import com.namber.chitchat.model.dto.PeopleDTO;
import com.namber.chitchat.service.AppDataService;
import com.namber.chitchat.service.MessageService;
import com.namber.chitchat.service.UserPrefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/data")
public class DataController {
    @Autowired
    private AppDataService dataService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserPrefService userPrefService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/user")
    public AppUserDTO getUserDTO (){
        return dataService.getUserProfile();
    }

    @GetMapping("/conversations")
    public List<PeopleDTO> getConversations(){
        return dataService.getConversations();
    }

    @PostMapping("/msgs")
    public List<OutputMessage> getMsgs(@RequestBody MessageQuery query){
        return  messageService.getMessages(query);
    }

    @PostMapping("/notif")
    public void processNotification(@RequestBody UINotification notif){

        if(notif.getType().equalsIgnoreCase("msg_seen")){
            userPrefService.unsetUnseenCount(notif.getTo(), notif.getFrom());
            messageService.setViewed(notif.getTo(), notif.getFrom(), notif.getEndTime());
            userPrefService.unsetNotViewedCount(notif.getTo(), notif.getFrom());

            // notify other party
            OutputMessage outMsg= new OutputMessage();
            outMsg.setType("viewNotif");
            outMsg.setFrom(notif.getTo());
            this.simpMessagingTemplate.convertAndSendToUser(notif.getFrom(), "/queue/msg", outMsg);
        }
    }
    @PostMapping("/add-conv")
    public void addConv(@RequestParam String publicUsername){
        this.userPrefService.addConversation(publicUsername);
    }

    @GetMapping("/contacts")
    public List<PeopleDTO> getContacts(){
        return dataService.getContacts();
    }

    @GetMapping(value = "notif-sound", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] getNotifSound(){
        return dataService.getNotifSound();
    }
}
