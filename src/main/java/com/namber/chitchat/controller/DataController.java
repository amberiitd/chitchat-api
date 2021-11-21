package com.namber.chitchat.controller;

import com.namber.chitchat.model.*;
import com.namber.chitchat.model.dto.AppUserDTO;
import com.namber.chitchat.model.dto.PeopleDTO;
import com.namber.chitchat.service.AppDataService;
import com.namber.chitchat.service.MessageService;
import com.namber.chitchat.service.UserPrefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PostMapping("/pin-conv")
    public void pinChat(@RequestParam String publicUsername, @RequestParam long stamp){
        userPrefService.updatePinned(publicUsername, stamp);
    }

    @PostMapping("/add-conv")
    public void addConv(@RequestParam String publicUsername){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.userPrefService.addConversation(userPrefService.getPublicUsername(user.getUsername()), publicUsername);
    }

    @PostMapping("/add-contact")
    public void addContact(@RequestParam String publicUsername, @RequestParam String nickName){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.userPrefService.addContact(userPrefService.getPublicUsername(user.getUsername()), publicUsername, nickName);
    }

    @GetMapping("/contacts")
    public List<PeopleDTO> getContacts(){
        return dataService.getContacts();
    }

    @GetMapping(value = "notif-sound", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] getNotifSound(){
        return dataService.getNotifSound();
    }

    @PostMapping("/msg-action")
    public  void processMessage(@RequestBody Action action){
        messageService.updateMessage(action.getName(), action.getTimestamps());
    }

    @GetMapping("/people")
    public PeopleDTO findPeople(@RequestParam String publicUsername){
        return this.userPrefService.findPeople(publicUsername);
    }

    @GetMapping("/login")
    public String login(){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(user != null){
            return user.getUsername();
        }else {
            return null;
        }
    }

    @PostMapping("/delete")
    public  void deleteAction(@RequestBody DeleteRequest request){
        if ("messages".equals(request.getTargetType())){
            messageService.deleteMessages(request.getPublicUsername());
        }
        if("chat".equals(request.getTargetType())){
            userPrefService.deleteChat(request.getPublicUsername());
        }

        if("contact".equals(request.getTargetType())){
            userPrefService.deleteContact(request.getPublicUsername());
        }
    }
}
