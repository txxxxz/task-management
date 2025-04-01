package com.taskManagement.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.io.InputStream;

public interface FileService {
    
    /**
     * 上传任务文件到阿里云OSS的task/文件夹并保存到任务附件表
     * @param file 文件
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 文件URL
     */
    String uploadTaskFile(MultipartFile file, Long taskId, Long userId);
    /**
     * 批量上传任务文件到阿里云OSS的task/文件夹并保存到任务附件表
     * @param files 文件列表
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 文件URL列表
     */
    List<String> uploadTaskFiles(List<MultipartFile> files, Long taskId, Long userId);
    
    /**
     * 上传项目文件到阿里云OSS的project/文件夹并保存至项目附件表
     * @param file 文件
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 文件URL
     */
    String uploadProjectFile(MultipartFile file, Long projectId, Long userId);
    
    /**
     * 批量上传项目文件到阿里云OSS的project/文件夹并保存至项目附件表
     * @param files 文件列表
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 文件URL列表
     */
    List<String> uploadProjectFiles(List<MultipartFile> files, Long projectId, Long userId);
    
    /**
     * 上传用户头像到阿里云OSS的avatar/文件夹并更新用户头像
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像URL
     */
    String uploadAvatar(MultipartFile file, Long userId);
    
    /**
     * 从OSS获取文件并解密
     * @param objectKey OSS对象键（文件路径）
     * @return 解密后的文件流
     */
    InputStream getDecryptedFile(String objectKey);
    
    /**
     * 获取文件元数据（MIME类型、文件大小等）
     * @param objectKey OSS对象键（文件路径）
     * @return 文件元数据
     */
    com.aliyun.oss.model.ObjectMetadata getFileMetadata(String objectKey);
    
    /**
     * 从URL中提取OSS对象键
     * @param url 完整的OSS URL
     * @return OSS对象键
     */
    String getObjectKeyFromUrl(String url);
} 