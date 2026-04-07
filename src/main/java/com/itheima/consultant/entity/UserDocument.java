package com.itheima.consultant.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDocument {
    private Long id;
    private Long userId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer status;
    private LocalDateTime createTime;
}