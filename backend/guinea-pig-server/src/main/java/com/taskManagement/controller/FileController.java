package com.taskManagement.controller;

import com.taskManagement.result.Result;
import com.taskManagement.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/files")
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传单个加密文件到阿里云OSS
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("上传文件: {}, 大小: {}", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            String fileUrl = fileService.uploadEncryptedFile(file);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传加密文件到阿里云OSS
     */
    @PostMapping("/batch-upload")
    public Result<List<String>> batchUploadFiles(@RequestParam("files") List<MultipartFile> files) {
        log.info("批量上传文件, 数量: {}", files.size());
        
        if (files.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            List<String> fileUrls = fileService.uploadEncryptedFiles(files);
            return Result.success(fileUrls);
        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return Result.error("批量文件上传失败: " + e.getMessage());
        }
    }
} 