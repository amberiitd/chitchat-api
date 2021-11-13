package com.namber.chitchat.model;

import lombok.Data;

@Data
public class MessageQuery {
    private String username;
    private String from;
    private int count = 0 ;
    private int offset = 0;
    private long startTime = -1L;
    private long endTime = -1L;
    private long pivotTime = -1L;
    private long timestamp = 0;
    private String searchText;
    private int sort = -1;

}
