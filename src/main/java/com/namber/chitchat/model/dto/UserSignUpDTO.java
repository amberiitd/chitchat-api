package com.namber.chitchat.model.dto;

import com.namber.chitchat.model.AppUser;
import lombok.Data;

@Data
public class UserSignUpDTO extends AppUserDTO {
    private String username;
    private String password;
}
