package com.namber.chitchat.model.dto;

import com.namber.chitchat.model.OutputMessage;
import lombok.Data;

@Data
public class PeopleDTO {
    private String publicUsername;
    private String nickName;
    private byte[] dp;
    private OutputMessage lastMessage;
    private int unseenCount =0;
    private int notViewedCount =0;
    private String status;
}
