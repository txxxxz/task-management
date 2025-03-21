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
@RequestMapping("/files")
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
    
    /**
     * 上传单个任务文件到阿里云OSS的task/文件夹
     */
    @PostMapping("/task/upload")
    public Result<String> uploadTaskFile(@RequestParam("file") MultipartFile file) {
        log.info("上传任务文件: {}, 大小: {}", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            String fileUrl = fileService.uploadTaskFile(file);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("任务文件上传失败", e);
            return Result.error("任务文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传任务文件到阿里云OSS的task/文件夹
     */
    @PostMapping("/task/batch-upload")
    public Result<List<String>> batchUploadTaskFiles(@RequestParam("files") List<MultipartFile> files) {
        log.info("批量上传任务文件, 数量: {}", files.size());
        
        if (files.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            List<String> fileUrls = fileService.uploadTaskFiles(files);
            return Result.success(fileUrls);
        } catch (Exception e) {
            log.error("批量任务文件上传失败", e);
            return Result.error("批量任务文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传单个项目文件到阿里云OSS的project/文件夹并保存到项目附件表
     */
    @PostMapping("/project/upload")
    public Result<String> uploadProjectFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam("projectId") Long projectId,
                                          @RequestParam("userId") Long userId) {
        log.info("上传项目文件: {}, 大小: {}, 项目ID: {}", file.getOriginalFilename(), file.getSize(), projectId);
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            String fileUrl = fileService.uploadProjectFile(file, projectId, userId);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("项目文件上传失败", e);
            return Result.error("项目文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传项目文件到阿里云OSS的project/文件夹并保存到项目附件表
     */
    @PostMapping("/project/batch-upload")
    public Result<List<String>> batchUploadProjectFiles(@RequestParam("files") List<MultipartFile> files,
                                                       @RequestParam("projectId") Long projectId,
                                                       @RequestParam("userId") Long userId) {
        log.info("批量上传项目文件, 数量: {}, 项目ID: {}", files.size(), projectId);
        
        if (files.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            List<String> fileUrls = fileService.uploadProjectFiles(files, projectId, userId);
            return Result.success(fileUrls);
        } catch (Exception e) {
            log.error("批量项目文件上传失败", e);
            return Result.error("批量项目文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar/upload")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                   @RequestParam("userId") Long userId) {
        log.info("上传用户头像: {}, 大小: {}, 用户ID: {}", file.getOriginalFilename(), file.getSize(), userId);
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            String fileUrl = fileService.uploadAvatar(file, userId);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("用户头像上传失败", e);
            return Result.error("用户头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传单个任务文件并保存到任务附件表
     */
    @PostMapping("/task/attachment/upload")
    public Result<String> uploadTaskAttachment(@RequestParam("file") MultipartFile file,
                                          @RequestParam("taskId") Long taskId,
                                          @RequestParam("userId") Long userId) {
        log.info("上传任务附件: {}, 大小: {}, 任务ID: {}", file.getOriginalFilename(), file.getSize(), taskId);
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            String fileUrl = fileService.uploadTaskFile(file, taskId, userId);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("任务附件上传失败", e);
            return Result.error("任务附件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传任务文件并保存到任务附件表
     */
    @PostMapping("/task/attachment/batch-upload")
    public Result<List<String>> batchUploadTaskAttachments(@RequestParam("files") List<MultipartFile> files,
                                               @RequestParam("taskId") Long taskId,
                                               @RequestParam("userId") Long userId) {
        log.info("批量上传任务附件, 数量: {}, 任务ID: {}", files.size(), taskId);
        
        if (files.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            List<String> fileUrls = fileService.uploadTaskFiles(files, taskId, userId);
            return Result.success(fileUrls);
        } catch (Exception e) {
            log.error("批量任务附件上传失败", e);
            return Result.error("批量任务附件上传失败: " + e.getMessage());
        }
    }
} 