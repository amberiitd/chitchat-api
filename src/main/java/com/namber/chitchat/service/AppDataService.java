package com.namber.chitchat.service;

import com.namber.chitchat.dao.AppUserRepo;
import com.namber.chitchat.model.AppUser;
import com.namber.chitchat.model.People;
import com.namber.chitchat.model.UserPreference;
import com.namber.chitchat.model.dto.AppUserDTO;
import com.namber.chitchat.model.dto.PeopleDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppDataService {
    @Autowired
    private AppUserRepo dataRepo;

    @Autowired
    ModelMapper mapper;


    public List<PeopleDTO> getConversations() {
        List<PeopleDTO> peopleDTOList = new ArrayList<>();

        UserDetails user= (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null){
            return null;
        }
        UserPreference userPref = dataRepo.getUserPref(user.getUsername());

        userPref.getConversations().forEach( people -> {
            PeopleDTO peopleDTO = mapper.map(people, PeopleDTO.class);
            try{
                peopleDTO.setDp(Files.readAllBytes(new File(people.getDpSrc()).toPath()));
            }catch (Exception e){
                // add default dp
            }
            peopleDTOList.add(peopleDTO);
        });

        return peopleDTOList;
    }

    public AppUserDTO getUserProfile() {
        UserDetails user= (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null){
            return null;
        }
        UserPreference userPref = dataRepo.getUserPref(user.getUsername());
        AppUserDTO userDTO = mapper.map(userPref, AppUserDTO.class);

        try{
            userDTO.setDp(Files.readAllBytes(new File(userPref.getDpSrc()).toPath()));
        }catch (Exception e){
            // add default dp
        }
        return userDTO;
    }

}
