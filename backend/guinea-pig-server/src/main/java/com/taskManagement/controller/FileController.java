package com.taskManagement.controller;

import com.taskManagement.result.Result;
import com.taskManagement.service.FileService;
import com.taskManagement.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) userId = 1L; // 默认用户ID
            
            // 使用项目文件上传代替通用上传
            // 创建一个临时项目ID，表示通用文件
            Long tempProjectId = 0L;
            String fileUrl = fileService.uploadProjectFile(file, tempProjectId, userId);
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
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) userId = 1L; // 默认用户ID
            
            // 使用项目文件批量上传代替通用批量上传
            // 创建一个临时项目ID，表示通用文件
            Long tempProjectId = 0L;
            List<String> fileUrls = fileService.uploadProjectFiles(files, tempProjectId, userId);
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
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) userId = 1L; // 默认用户ID
            
            // 使用完整参数版本，提供一个临时任务ID
            Long tempTaskId = 0L;
            String fileUrl = fileService.uploadTaskFile(file, tempTaskId, userId);
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
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) userId = 1L; // 默认用户ID
            
            // 使用完整参数版本，提供一个临时任务ID
            Long tempTaskId = 0L;
            List<String> fileUrls = fileService.uploadTaskFiles(files, tempTaskId, userId);
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
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("上传用户头像: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            String fileUrl = fileService.uploadAvatar(file, userId);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败: " + e.getMessage());
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

    /**
     * 下载解密后的文件
     * 支持通过URL或路径获取文件
     * @param fileUrl 文件URL，完整的OSS URL
     * @param filePath 文件路径，OSS对象键
     * @return 解密后的文件内容
     */
    @GetMapping("/download")
    public void downloadDecryptedFile(
            @RequestParam(required = false) String fileUrl,
            @RequestParam(required = false) String filePath,
            HttpServletResponse response) {
        // 参数验证
        if ((fileUrl == null || fileUrl.isEmpty()) && (filePath == null || filePath.isEmpty())) {
            throw new RuntimeException("fileUrl和filePath不能同时为空");
        }
        
        String objectKey = null;
        if (fileUrl != null && !fileUrl.isEmpty()) {
            objectKey = fileService.getObjectKeyFromUrl(fileUrl);
        } else {
            objectKey = filePath;
        }
        
        try {
            // 获取文件元数据
            com.aliyun.oss.model.ObjectMetadata metadata = fileService.getFileMetadata(objectKey);
            
            // 获取解密后的文件内容
            InputStream decryptedStream = fileService.getDecryptedFile(objectKey);
            
            // 设置响应头
            response.setContentType(metadata.getContentType());
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                    URLEncoder.encode(getOriginalFileName(objectKey), "UTF-8"));
            
            // 将文件内容写入响应
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = decryptedStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            
            // 关闭流
            decryptedStream.close();
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 预览解密后的文件（用于在线查看）
     * @param fileUrl 文件URL，完整的OSS URL
     * @param filePath 文件路径，OSS对象键
     * @return 解密后的文件内容
     */
    @GetMapping("/preview")
    public void previewDecryptedFile(
            @RequestParam(required = false) String fileUrl,
            @RequestParam(required = false) String filePath,
            HttpServletResponse response) {
        // 参数验证
        if ((fileUrl == null || fileUrl.isEmpty()) && (filePath == null || filePath.isEmpty())) {
            throw new RuntimeException("fileUrl和filePath不能同时为空");
        }
        
        String objectKey = null;
        if (fileUrl != null && !fileUrl.isEmpty()) {
            objectKey = fileService.getObjectKeyFromUrl(fileUrl);
        } else {
            objectKey = filePath;
        }
        
        try {
            // 获取文件元数据
            com.aliyun.oss.model.ObjectMetadata metadata = fileService.getFileMetadata(objectKey);
            
            // 获取解密后的文件内容
            InputStream decryptedStream = fileService.getDecryptedFile(objectKey);
            
            // 设置响应头，inline表示在浏览器中直接显示
            response.setContentType(metadata.getContentType());
            response.setHeader("Content-Disposition", "inline; filename=" + 
                    URLEncoder.encode(getOriginalFileName(objectKey), "UTF-8"));
            
            // 将文件内容写入响应
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = decryptedStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            
            // 关闭流
            decryptedStream.close();
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("文件预览失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件预览失败: " + e.getMessage());
        }
    }
    
    /**
     * 从对象键获取原始文件名（去除encrypted_前缀）
     * @param objectKey 对象键
     * @return 原始文件名
     */
    private String getOriginalFileName(String objectKey) {
        // 提取文件名部分
        String fileName = objectKey;
        int lastSlashIndex = objectKey.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < objectKey.length() - 1) {
            fileName = objectKey.substring(lastSlashIndex + 1);
        }
        
        // 去除encrypted_前缀
        if (fileName.startsWith("encrypted_")) {
            fileName = fileName.substring("encrypted_".length());
        }
        
        return fileName;
    }
} 