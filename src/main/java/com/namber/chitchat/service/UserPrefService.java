package com.namber.chitchat.service;

import com.namber.chitchat.dao.AppUserRepo;
import com.namber.chitchat.model.AppUser;
import com.namber.chitchat.model.People;
import com.namber.chitchat.model.PublicUserPreference;
import com.namber.chitchat.model.UserPreference;
import com.namber.chitchat.model.dto.PeopleDTO;
import com.namber.chitchat.model.dto.UserSignUpDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserPrefService {
    @Autowired
    private AppUserRepo userRepo;

    @Value("${default.userIconPath}")
    private String defaultIconPath;

    @Value("${default.resourcePath}")
    private String resourcePath;

    @Autowired
    ModelMapper mapper;

    @Autowired
    PasswordEncoder encoder;


    public String getPublicUsername(String username) {
        UserPreference userPref = userRepo.getUserPref(username);
        if (userPref != null && !StringUtils.isEmpty(userPref.getPublicUsername()) ){
            return userPref.getPublicUsername();
        }

        return null;
    }

    public String getUsername(String publicUsername) {
        UserPreference userPref = userRepo.getUserPrefByPublicUsername(publicUsername);
        if (userPref != null && !StringUtils.isEmpty(userPref.getUsername()) ){
            return userPref.getUsername();
        }

        return null;
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

    public UserPreference getUserPref(String username) {
        return userRepo.getUserPref(username);
    }

    public String getPublicStatus(String publicUsername) {
        PublicUserPreference userPreference = userRepo.getPublicUserPref(publicUsername);
        return  userPreference.getStatus();
    }

    public PublicUserPreference getPublicUserPref(String publicUsername) {
        PublicUserPreference userPreference = userRepo.getPublicUserPref(publicUsername);
        return  userPreference;
    }
    public void addConversation(String user, String publicUsername) {
        People publicContact = getUserPref(user).getContacts()
                .stream()
                .filter(contact -> contact.getPublicUsername().equals(publicUsername))
                .collect(Collectors.toList()).get(0);

        userRepo.pushToUserPrefConv(user, publicContact);
    }

    public PeopleDTO findPeople(String publicUsername) {
        PublicUserPreference userPref = userRepo.findPublicUserPref(publicUsername);
        if(userPref != null){
            PeopleDTO peopleDTO = new PeopleDTO();
            peopleDTO.setStatus(userPref.getStatus());
            peopleDTO.setNickName(userPref.getFirstName());
            peopleDTO.setPublicUsername(userPref.getPublicUsername());
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

    public void addContact(String username, String publicUsername, String nickName) {

        if(userRepo.contactExists(username, publicUsername) > 0){
            return;
        }

        PublicUserPreference userPref = userRepo.findPublicUserPref(publicUsername);
        People people = new People();
        people.setDpSrc(userPref.getDpSrc());

        if(nickName.equals(publicUsername)){
            people.setNickName(userPref.getFirstName());
        }else{
            people.setNickName(nickName);
        }
        people.setPublicUsername(userPref.getPublicUsername());

        userRepo.pushToUserPrefContact(username, people);
    }

    public void updatePinned(String publicUsername, long val) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userRepo.updateUserPrefPinned(getPublicUsername(user.getUsername()), publicUsername, val);
    }

    public void signup(UserSignUpDTO signUpDTO) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(signUpDTO.getUsername());
        user.setPassword(encoder.encode(signUpDTO.getPassword()));
        user.setAuthorities(Arrays.asList("USER"));

        PublicUserPreference publicUserPreference = mapper.map(signUpDTO, PublicUserPreference.class);
        publicUserPreference.setDpSrc(resourcePath + "/"+ publicUserPreference.getPublicUsername() + ".jpeg");

        UserPreference userPreference = mapper.map(publicUserPreference, UserPreference.class);
        userPreference.setUsername(signUpDTO.getUsername());
        userPreference.setContacts(Arrays.asList());
        userPreference.setConversations(Arrays.asList());

        userRepo.saveUser(user);
        userRepo.saveUserPrefence(userPreference);
        userRepo.savePublicUserPrefence(publicUserPreference);
    }


    public void deleteChat(String publicUsername) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userRepo.deleteChat(user.getUsername(), publicUsername);
    }

    public void setPeople(String user, String people) {
        if(userRepo.convExists(user, people) > 0){
            return;
        }
        this.addContact(user, people, people);
        this.addConversation(user, people);
    }

    public void deleteContact(String publicUsername) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userRepo.deleteContact(user.getUsername(), publicUsername);
    }
}
