package com.namber.chitchat.model;

import lombok.Data;

import java.util.List;

@Data
public class DeleteRequest {
    private String targetType;
    private List<Object> targetList;
    private String publicUsername;
}
