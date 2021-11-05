package com.namber.chitchat.controller;

import com.namber.chitchat.model.People;
import com.namber.chitchat.model.dto.AppUserDTO;
import com.namber.chitchat.model.dto.PeopleDTO;
import com.namber.chitchat.service.AppDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/data")
public class DataController {
    @Autowired
    private AppDataService dataService;

    @GetMapping("/user")
    public AppUserDTO getUserDTO (){
        return dataService.getUserProfile();
    }

    @GetMapping("/conversations")
    public List<PeopleDTO> getConversations(){
        return dataService.getConversations();
    }
}
