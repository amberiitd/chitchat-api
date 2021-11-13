package com.namber.chitchat.model;

import lombok.Data;

@Data
public class OutputMessage {
    private String type;
    private String from;
    private String text;
    private boolean notViewed;
    private long timestamp;
    private long parentId; // timestamp for now
    private OutputMessage parent;

}
