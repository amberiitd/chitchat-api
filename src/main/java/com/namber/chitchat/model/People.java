package com.namber.chitchat.model;

import lombok.Data;

@Data
public class People {
    private String publicUsername;
    private String nickName;
    private String dpSrc;
    private int unseenCount = 0;
    private int notViewedCount = 0;
}
