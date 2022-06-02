package com.namber.chitchat.service;

import com.namber.chitchat.dao.MessageRepo;
import com.namber.chitchat.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {
    @Autowired
    private MessageRepo msgRepo;

    @Autowired
    private UserPrefService userPrefService;

    @Autowired
    private ModelMapper mapper;

    public void save(InputMessage inMsg) {
        String user;

        Message msg = mapper.map(inMsg, Message.class);
        msg.setTo(inMsg.getTo());
        msg.setMessageId(UUID.randomUUID().toString());

        //save in sender
        user = userPrefService.getUsername(inMsg.getFrom());
        msgRepo.save(user, msg);

        //save in reciever
        user = userPrefService.getUsername(inMsg.getTo());
        msgRepo.save(user, msg);
    }

    public OutputMessage getLastMessage(String username, String from) {
        MessageQuery query = new MessageQuery();
        query.setUsername(username);
        query.setCount(1);
        query.setFrom(from);
        List<Message> msgs = msgRepo.fetchMessages( query);

        if(!msgs.isEmpty()){
            return mapper.map(msgs.get(0), OutputMessage.class);
        }

        return null;
    }

    public List<OutputMessage> getMessages(MessageQuery query) {
        List<OutputMessage> outMsgs = new ArrayList<>();

        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        query.setUsername(user.getUsername());
        if (query.getCount() == 0){
            query.setCount(20);
        }

        if( query.getPivotTime() > 0L){
            // first part
            query.setCount(10);
            query.setEndTime(query.getPivotTime());
            query.setStartTime(-1L);

            msgRepo.fetchMessages(query).forEach( msg ->{
                outMsgs.add(mapper.map(msg, OutputMessage.class));
            });

            // second part
            query.setStartTime(query.getPivotTime());
            query.setEndTime(-1L);
            query.setSort(1); // get oldest
            List<Message> secondPart = msgRepo.fetchMessages(query);
            Collections.reverse(secondPart);
            secondPart.forEach(msg ->{
                outMsgs.add(mapper.map(msg, OutputMessage.class));
            });
        }
        else if(query.getStartTime() > 0L){
            query.setSort(1); // get oldest
            List<Message> startMsgs = msgRepo.fetchMessages(query);
            Collections.reverse(startMsgs);
            startMsgs.forEach(msg ->{
                outMsgs.add(mapper.map(msg, OutputMessage.class));
            });
        }
        else{
            msgRepo.fetchMessages(query).forEach( msg ->{
                outMsgs.add(mapper.map(msg, OutputMessage.class));
            });
        }

        outMsgs.forEach(msg -> {
            if (msg.getParentId() > 0){
                msg.setParent(getParent(msg));
            }
        });

        return outMsgs;
    }

    public OutputMessage getParent(OutputMessage msg){
        String username  = userPrefService.getUsername(msg.getFrom());

        MessageQuery pQuery = new MessageQuery();
        pQuery.setUsername(username);
        pQuery.setFrom(msg.getFrom());
        pQuery.setTimestamp(msg.getParentId());
        pQuery.setCount(1);

        return mapper.map(msgRepo.fetchMessages(pQuery).get(0), OutputMessage.class);
    }

    public void setViewed(String to, String from, long endTime){
        msgRepo.setViewed(
                to,
                userPrefService.getUsername(from),
                endTime);
    }

    public void updateMessage(String action, List<Long> timestamps) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Action.STAR.equals(action)){
            msgRepo.updateStarred(user.getUsername(), timestamps);
        }else if(Action.DELETE.equals(action)){
            msgRepo.deleteMessage(user.getUsername(), timestamps);
        }
    }

    public void deleteMessages(String publicUsername) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        msgRepo.deleteMessages(user.getUsername(), publicUsername);
    }

}
