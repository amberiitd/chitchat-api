package com.namber.chitchat.model;

import lombok.Data;

@Data
public class OutputMessage {
    private String from;
    private String text;
    private String timestamp;
}
