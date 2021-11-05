package com.namber.chitchat.model;

import lombok.Data;
import nonapi.io.github.classgraph.json.Id;

import java.util.List;

@Data
public class AppUser {
    @Id
    private String id;
    private String username;
    private String password;
    private List<String> authorities;
}
