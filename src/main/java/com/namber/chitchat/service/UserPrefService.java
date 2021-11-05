package com.namber.chitchat.service;

import com.namber.chitchat.dao.AppUserRepo;
import com.namber.chitchat.model.UserPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserPrefService {
    @Autowired
    private AppUserRepo userRepo;


    public String getPublicUsername(String username) {
        UserPreference userPref = userRepo.getUserPref(username);
        if (userPref != null && !StringUtils.isEmpty(userPref.getPublicUsername()) ){
            return userPref.getPublicUsername();
        }

        return username;
    }
}
