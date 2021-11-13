package com.namber.chitchat.model;

import lombok.Data;

@Data
public class UINotification {
    private String type;
    private String to;
    private String from;
    private long endTime;
}
