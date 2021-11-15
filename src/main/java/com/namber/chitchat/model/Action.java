package com.namber.chitchat.model;

import lombok.Data;

import java.util.List;

@Data
public class Action {
    private String name;
    private List<Long> timestamps ;

    public static String STAR = "star";
    public static String DELETE = "delete";
}
