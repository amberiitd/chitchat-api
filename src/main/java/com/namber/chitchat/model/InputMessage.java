package com.namber.chitchat.model;

import lombok.Data;

@Data
public class InputMessage extends OutputMessage{
    private String type;
    private String to;
    private String from;
    private String text;
    private boolean notViewed;
    private boolean deleted;
    private boolean starred;
    private long timestamp;
    private long parentId; // timestamp for now
    private long poll= 0L;
}
