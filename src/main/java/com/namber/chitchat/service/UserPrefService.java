package com.namber.chitchat.service;

import com.namber.chitchat.dao.AppUserRepo;
import com.namber.chitchat.model.People;
import com.namber.chitchat.model.PublicUserPreference;
import com.namber.chitchat.model.UserPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class UserPrefService {
    @Autowired
    private AppUserRepo userRepo;


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



}
