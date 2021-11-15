package com.namber.chitchat.service;

import com.namber.chitchat.dao.AppUserRepo;
import com.namber.chitchat.model.People;
import com.namber.chitchat.model.PublicUserPreference;
import com.namber.chitchat.model.UserPreference;
import com.namber.chitchat.model.dto.PeopleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Service
public class UserPrefService {
    @Autowired
    private AppUserRepo userRepo;

    @Value("${default.userIconPath}")
    private String defaultIconPath;


    public String getPublicUsername(String username) {
        UserPreference userPref = userRepo.getUserPrefByUsername(username);
        if (userPref != null && !StringUtils.isEmpty(userPref.getPublicUsername()) ){
            return userPref.getPublicUsername();
        }

        return username;
    }

    public String getUsername(String publicUsername) {
        UserPreference userPref = userRepo.getUserPrefByPublicUsername(publicUsername);
        if (userPref != null && !StringUtils.isEmpty(userPref.getUsername()) ){
            return userPref.getUsername();
        }

        return publicUsername;
    }

    public void incUnseenCount(String to, String from) {
        userRepo.incUserPrefUnseenCount(to, from, 1);
    }
    public void incNotViewedCount(String to, String from) {
        userRepo.incUserPrefNotViewedCount(to, from, 1);
    }

    public void unsetUnseenCount(String to, String from) {
        userRepo.unsetUserPrefUnseenCount(to, from);
    }
    public void unsetNotViewedCount(String to, String from) {
        userRepo.unsetUserPrefNotViewedCount(to, from);
    }

    public UserPreference getUserPrefByUsername(String username) {
        return userRepo.getUserPrefByUsername(username);
    }

    public String getPublicStatus(String publicUsername) {
        PublicUserPreference userPreference = userRepo.getPublicUserPref(publicUsername);
        return  userPreference.getStatus();
    }

    public PublicUserPreference getPublicUserPref(String publicUsername) {
        PublicUserPreference userPreference = userRepo.getPublicUserPref(publicUsername);
        return  userPreference;
    }
    public void addConversation(String publicUsername) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        People publicContact = getUserPrefByUsername(user.getUsername()).getContacts()
                .stream()
                .filter(contact -> contact.getPublicUsername().equals(publicUsername))
                .collect(Collectors.toList()).get(0);

        userRepo.pushToUserPrefConv(user.getUsername(), publicContact);
    }

    public PeopleDTO findPeople(String publicUsername) {
        PublicUserPreference userPref = userRepo.findPublicUserPref(publicUsername);
        if(userPref != null){
            PeopleDTO peopleDTO = new PeopleDTO();
            peopleDTO.setStatus(userPref.getStatus());
            peopleDTO.setNickName(userPref.getFirstName());
            try{
                peopleDTO.setDp(Files.readAllBytes(new File(userPref.getDpSrc()).toPath()));
            }catch (Exception e){
                // add default dp
                try{
                    peopleDTO.setDp(Files.readAllBytes(new File(defaultIconPath).toPath()));
                }catch (Exception e2){

                }
            }
            return peopleDTO;
        }else{
            return null;
        }
    }

    public void addContact(String publicUsername, String nickName) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        PublicUserPreference userPref = userRepo.findPublicUserPref(publicUsername);
        People people = new People();
        people.setDpSrc(userPref.getDpSrc());
        people.setNickName(nickName);
        people.setPublicUsername(userPref.getPublicUsername());

        userRepo.pushToUserPrefContact(user.getUsername(), people);
    }
}
