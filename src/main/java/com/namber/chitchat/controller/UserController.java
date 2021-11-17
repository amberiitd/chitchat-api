package com.namber.chitchat.controller;

import com.namber.chitchat.model.dto.UserSignUpDTO;
import com.namber.chitchat.service.UserPrefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("/user")
public class UserController {
    @Autowired
    private UserPrefService userPrefService;

    @GetMapping("/check-exist")
    public String exist(@RequestParam String username, @RequestParam String publicUsername){
        String existingPublicUsername = userPrefService.getPublicUsername(username);
        if (existingPublicUsername != null){
            return "true";
        }

        String existingUsername = userPrefService.getUsername(publicUsername);
        if(existingUsername != null){
            return "true";
        }

        return  "false";
    }

    @PostMapping("/signup")
    public void signup(@RequestBody UserSignUpDTO signUpDTO){
        userPrefService.signup(signUpDTO);
    }
}
