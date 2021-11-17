package com.namber.chitchat.service;

import com.namber.chitchat.model.UserPreference;
import com.namber.chitchat.model.dto.AppUserDTO;
import com.namber.chitchat.model.dto.PeopleDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AppDataService {
    @Autowired
    private  MessageService msgService;

    @Autowired
    private  UserPrefService userPrefService;


    @Autowired
    ModelMapper mapper;

    @Value("${default.userIconPath}")
    private String defaultIconPath;

    @Value("${default.staticPath}")
    private String staticPath;

    public List<PeopleDTO> getConversations() {
        List<PeopleDTO> peopleDTOList = new ArrayList<>();

        UserPreference userPref = getUserPreference();
        userPref.getConversations().forEach( people -> {
            PeopleDTO peopleDTO = mapper.map(people, PeopleDTO.class);
            peopleDTO.setLastMessage(msgService.getLastMessage(userPref.getUsername(), people.getPublicUsername()));
            peopleDTO.setStatus(userPrefService.getPublicStatus(people.getPublicUsername()));
            try{
                peopleDTO.setDp(Files.readAllBytes(new File(people.getDpSrc()).toPath()));
            }catch (Exception e){
                try{
                    peopleDTO.setDp(Files.readAllBytes(new File(defaultIconPath).toPath()));
                }catch (Exception e2){

                }
            }
            peopleDTOList.add(peopleDTO);
        });

        return peopleDTOList;
    }

    public AppUserDTO getUserProfile() {
        UserPreference userPref = getUserPreference();
        AppUserDTO userDTO = mapper.map(userPref, AppUserDTO.class);

        try{
            userDTO.setDp(Files.readAllBytes(new File(userPref.getDpSrc()).toPath()));
        }catch (Exception e){
            // add default dp
            try{
                userDTO.setDp(Files.readAllBytes(new File(defaultIconPath).toPath()));
            }catch (Exception e2){

            }
        }
        return userDTO;
    }

    public List<PeopleDTO> getContacts() {
        List<PeopleDTO> peopleDTOList = new ArrayList<>();

        UserPreference userPref = getUserPreference();
        userPref.getContacts().forEach( people -> {
            PeopleDTO peopleDTO = mapper.map(people, PeopleDTO.class);
            peopleDTO.setLastMessage(msgService.getLastMessage(userPref.getUsername(), people.getPublicUsername()));
            peopleDTO.setStatus(userPrefService.getPublicStatus(people.getPublicUsername()));
            try{
                peopleDTO.setDp(Files.readAllBytes(new File(people.getDpSrc()).toPath()));
            }catch (Exception e){
                // add default dp
                try{
                    peopleDTO.setDp(Files.readAllBytes(new File(defaultIconPath).toPath()));
                }catch (Exception e2){

                }
            }
            peopleDTOList.add(peopleDTO);
        });

        return peopleDTOList;
    }

    private UserPreference getUserPreference(){
        UserDetails user= (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null){
            return null;
        }
        return userPrefService.getUserPref(user.getUsername());
    }

    public byte[] getNotifSound() {
        try{
            byte[] data = IOUtils.toByteArray(new FileInputStream(staticPath + "/notification.mp3"));

            return data;
        }catch (Exception e){
            log.error("Could not get Notification sound");
        }

        return null;
    }
}
