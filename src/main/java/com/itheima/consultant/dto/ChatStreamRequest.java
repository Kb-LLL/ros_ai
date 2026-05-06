package com.itheima.consultant.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatStreamRequest {
    private Long memoryId;
    private String message;
    private List<String> imageKeys = new ArrayList<>();
}
