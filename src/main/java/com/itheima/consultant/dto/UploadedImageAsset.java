package com.itheima.consultant.dto;

import lombok.Data;

@Data
public class UploadedImageAsset {
    private String objectKey;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String storageType;
}
