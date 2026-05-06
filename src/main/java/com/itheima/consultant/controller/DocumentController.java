package com.itheima.consultant.controller;

import com.itheima.consultant.common.Result;
import com.itheima.consultant.entity.UserDocument;
import com.itheima.consultant.service.DocumentPipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocumentPipelineService documentPipelineService;

    /** 上传文件 */
    @PostMapping("/upload")
    public Result<UserDocument> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        if (file.getSize() > 20 * 1024 * 1024) return Result.error("文件大小不能超过20MB");
        try {
            return Result.success(documentPipelineService.uploadDocument(file));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 文件列表 */
    @GetMapping("/list")
    public Result<List<UserDocument>> list() {
        return Result.success(documentPipelineService.listDocuments());
    }

    /** 删除文件 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        documentPipelineService.deleteDocument(id);
        return Result.success();
    }
}
