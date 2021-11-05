package com.namber.chitchat.controller;

import com.namber.chitchat.model.Message;
import com.namber.chitchat.model.OutputMessage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {
    @Autowired
    private ModelMapper mapper;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/broadcast")
    @SendTo("/topic/msg")
    public OutputMessage broadcast(Message msg){
        return mapper.map(msg, OutputMessage.class);
    }

    @MessageMapping("/monocast")
//    @SendToUser("/queue/msg")
    public void sentoUser(SimpMessageHeaderAccessor sha, @Payload Message msg){
        this.simpMessagingTemplate.convertAndSendToUser(msg.getTo(), "/queue/msg", mapper.map(msg, OutputMessage.class));

    }
}
