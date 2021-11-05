package com.namber.chitchat.model.dto;

import lombok.Data;

@Data
public class AppUserDTO {
    private String publicUsername;
    private String firstName;
    private String lastName;
    private byte[] dp;
}
