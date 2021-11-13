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
        if (outMsg.getParentId() > 0){
            outMsg.setParent(messageService.getParent(outMsg));
        }
        this.simpMessagingTemplate.convertAndSendToUser(inMsg.getTo(), "/queue/msg", outMsg);

        if( inMsg.getType().equals("message") ){
            //persist to sender collection
            Message msg = mapper.map(outMsg, Message.class);
            msg.setTo(inMsg.getTo());
            msg.setMessageId(UUID.randomUUID().toString());
            messageService.save(inMsg.getFrom(), msg);

            //persist to reciever collection
            messageService.save(inMsg.getTo(), msg);

            //update unseen of reciever
            userPrefService.incUnseenCount(inMsg.getTo(), inMsg.getFrom());

            //update notViewed of reciever
            userPrefService.incNotViewedCount(inMsg.getTo(), inMsg.getFrom());
        }

    }
}
