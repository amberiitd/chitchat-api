package com.namber.chitchat.model;

import lombok.Data;

import java.util.List;

@Data
public class UserPreference {
    private String username;
    private String publicUsername;
    private String firstName;
    private String dpSrc;
    private List<People> conversations;
    private List<People> contacts;
}
