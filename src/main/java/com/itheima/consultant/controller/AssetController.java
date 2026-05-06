package com.itheima.consultant.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.itheima.consultant.common.Result;
import com.itheima.consultant.dto.UploadedImageAsset;
import com.itheima.consultant.service.MinioStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/asset")
public class AssetController {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;

    @Autowired
    private MinioStorageService minioStorageService;

    @PostMapping("/image")
    public Result<UploadedImageAsset> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("图片不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            return Result.error("图片大小不能超过 10MB");
        }
        if (!isImage(file)) {
            return Result.error("仅支持图片文件");
        }

        try {
            Long userId = Long.valueOf(StpUtil.getLoginId().toString());
            return Result.success(minioStorageService.uploadChatImage(file, userId));
        } catch (Exception exception) {
            return Result.error(exception.getMessage());
        }
    }

    @DeleteMapping("/image")
    public Result<?> deleteImage(@RequestParam("objectKey") String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return Result.error("图片对象不能为空");
        }

        try {
            Long userId = Long.valueOf(StpUtil.getLoginId().toString());
            minioStorageService.deleteChatImage(objectKey, userId);
            return Result.success();
        } catch (Exception exception) {
            return Result.error(exception.getMessage());
        }
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}
