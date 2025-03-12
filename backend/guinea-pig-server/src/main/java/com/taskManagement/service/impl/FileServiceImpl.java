package com.taskManagement.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.taskManagement.config.AliyunOSSConfig;
import com.taskManagement.service.FileService;
import com.taskManagement.utils.FileEncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private AliyunOSSConfig ossConfig;

    @Override
    public String uploadEncryptedFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String objectName = null;
        
        try {
            // 生成加密文件名
            String encryptedFileName = FileEncryptionUtil.generateEncryptedFileName(fileName);
            objectName = ossConfig.getProjectFolder() + encryptedFileName;
            
            // 加密文件内容
            InputStream encryptedStream = FileEncryptionUtil.encryptFile(file, ossConfig.getEncryptionKey());
            
            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(encryptedStream.available());
            
            // 上传到OSS
            ossClient.putObject(ossConfig.getBucketName(), objectName, encryptedStream, metadata);
            log.info("文件[{}]加密上传成功，OSS路径：{}", fileName, objectName);
            
            // 返回访问URL
            return ossConfig.getUrlPrefix() + "/" + objectName;
        } catch (Exception e) {
            log.error("文件[{}]加密上传失败", fileName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadEncryptedFiles(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            String url = uploadEncryptedFile(file);
            urls.add(url);
        }
        
        return urls;
    }
} 