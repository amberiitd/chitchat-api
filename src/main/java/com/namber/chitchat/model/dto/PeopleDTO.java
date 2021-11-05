package com.namber.chitchat.model.dto;

import lombok.Data;

@Data
public class PeopleDTO {
    private String publicUsername;
    private String nickName;
    private byte[] dp;
}
