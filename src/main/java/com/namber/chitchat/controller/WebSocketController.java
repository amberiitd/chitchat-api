package com.namber.chitchat.controller;

import com.namber.chitchat.model.InputMessage;
import com.namber.chitchat.model.Message;
import com.namber.chitchat.model.OutputMessage;
import com.namber.chitchat.service.MessageService;
import com.namber.chitchat.service.UserPrefService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WebSocketController {
    @Autowired
    private ModelMapper mapper;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserPrefService userPrefService;

    @MessageMapping("/broadcast")
    @SendTo("/topic/msg")
    public OutputMessage broadcast(InputMessage msg){
        return mapper.map(msg, OutputMessage.class);
    }

    @MessageMapping("/monocast")
//    @SendToUser("/queue/msg")
    public void sentoUser(SimpMessageHeaderAccessor sha, @Payload InputMessage inMsg){
        OutputMessage outMsg = mapper.map(inMsg, OutputMessage.class);

        if( inMsg.getType().equals("message") ){
            if(inMsg.getPoll() > 1L){
                OutputMessage lastMessage = messageService.getLastMessage(userPrefService.getUsername(inMsg.getTo()), inMsg.getFrom());
                if(lastMessage.getTimestamp() == inMsg.getTimestamp()){
                    return;
                }
            }

            if (outMsg.getParentId() > 0){
                outMsg.setParent(messageService.getParent(outMsg));
            }
            //persist to sender & reciever collection
            messageService.save(inMsg);

            //create conv if does not exist
            userPrefService.setPeople(inMsg.getTo(), inMsg.getFrom());

            //update unseen of reciever
            userPrefService.incUnseenCount(inMsg.getTo(), inMsg.getFrom());

            //update notViewed of sender
            userPrefService.incNotViewedCount(inMsg.getTo(), inMsg.getFrom());

            OutputMessage confirmSent = new OutputMessage();
            confirmSent.setType("msg_sent");
            confirmSent.setTimestamp(inMsg.getTimestamp());
            confirmSent.setFrom(inMsg.getFrom());
            this.simpMessagingTemplate.convertAndSendToUser(inMsg.getFrom(), "/queue/msg", confirmSent);
            this.simpMessagingTemplate.convertAndSendToUser(inMsg.getTo(), "/queue/msg", outMsg);
        }else{
            this.simpMessagingTemplate.convertAndSendToUser(inMsg.getTo(), "/queue/msg", outMsg);
        }

    }
}
