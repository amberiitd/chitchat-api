package com.namber.chitchat.model;

import lombok.Data;

import java.util.List;

@Data
public class UserPreference extends PublicUserPreference{
    private String username;
    private List<People> conversations;
    private List<People> contacts;
}
