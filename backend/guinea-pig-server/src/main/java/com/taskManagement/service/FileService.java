package com.taskManagement.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface FileService {
    
    /**
     * 上传加密文件到阿里云OSS
     * @param file 文件
     * @return 文件URL
     */
    String uploadEncryptedFile(MultipartFile file);
    
    /**
     * 批量上传加密文件到阿里云OSS
     * @param files 文件列表
     * @return 文件URL列表
     */
    List<String> uploadEncryptedFiles(List<MultipartFile> files);
    
    /**
     * 上传任务文件到阿里云OSS的task/文件夹
     * @param file 文件
     * @return 文件URL
     */
    String uploadTaskFile(MultipartFile file);
    
    /**
     * 批量上传任务文件到阿里云OSS的task/文件夹
     * @param files 文件列表
     * @return 文件URL列表
     */
    List<String> uploadTaskFiles(List<MultipartFile> files);
} 