package com.taskManagement.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.taskManagement.config.AliyunOSSConfig;
import com.taskManagement.entity.ProjectAttachment;
import com.taskManagement.entity.TaskAttachment;
import com.taskManagement.entity.User;
import com.taskManagement.mapper.ProjectAttachmentMapper;
import com.taskManagement.mapper.TaskAttachmentMapper;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.service.FileService;
import com.taskManagement.utils.FileEncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private AliyunOSSConfig ossConfig;
    
    @Autowired
    private ProjectAttachmentMapper projectAttachmentMapper;
    
    @Autowired
    private TaskAttachmentMapper taskAttachmentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    // 文件夹路径常量，在service层硬编码
    private static final String PROJECT_FOLDER = "project/";
    private static final String TASK_FOLDER = "task/";
    private static final String AVATAR_FOLDER = "avatar/";
    
    /**
     * 内部私有方法，用于加密上传文件到OSS
     * @param file 文件
     * @param folderPath 文件夹路径
     * @return OSS文件URL
     */
    private String uploadEncryptedFileToOSS(MultipartFile file, String folderPath) {
        String fileName = file.getOriginalFilename();
        String objectName = null;
        
        try {
            // 生成加密文件名
            String encryptedFileName = FileEncryptionUtil.generateEncryptedFileName(fileName);
            objectName = folderPath + encryptedFileName;
            
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
    
    /**
     * 上传任务文件并保存到任务附件表
     * @param file 文件
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 文件URL
     */
    @Override
    public String uploadTaskFile(MultipartFile file, Long taskId, Long userId) {
        String fileName = file.getOriginalFilename();
        
        try {
            // 上传文件到OSS
            String fileUrl = uploadEncryptedFileToOSS(file, TASK_FOLDER);
            
            // 如果提供了任务ID和用户ID，保存到任务附件表
            if (taskId != null && userId != null && taskId > 0) {
                TaskAttachment taskAttachment = new TaskAttachment();
                taskAttachment.setTaskId(taskId);
                taskAttachment.setFileName(fileName);
                taskAttachment.setFilePath(fileUrl);
                taskAttachment.setFileSize(file.getSize());
                taskAttachment.setFileType(file.getContentType());
                taskAttachment.setCreateUser(userId);
                taskAttachment.setUpdateUser(userId);
                
                taskAttachmentMapper.insert(taskAttachment);
                log.info("任务文件[{}]记录已保存到数据库, 任务ID: {}, 文件大小: {}, 文件类型: {}", 
                        fileName, taskId, file.getSize(), file.getContentType());
            }
            
            // 返回访问URL
            return fileUrl;
        } catch (Exception e) {
            log.error("任务文件[{}]加密上传失败, 错误: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("任务文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传任务文件到阿里云OSS的task/文件夹并保存到任务附件表
     * @param files 文件列表
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 文件URL列表
     */
    @Override
    public List<String> uploadTaskFiles(List<MultipartFile> files, Long taskId, Long userId) {
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            String url = uploadTaskFile(file, taskId, userId);
            urls.add(url);
        }
        
        return urls;
    }
    
    /**
     * 上传项目文件到阿里云OSS的project/文件夹并保存至项目附件表
     * @param file 文件
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 文件URL
     */
    @Override
    public String uploadProjectFile(MultipartFile file, Long projectId, Long userId) {
        String fileName = file.getOriginalFilename();
        
        try {
            // 上传文件到OSS
            String fileUrl = uploadEncryptedFileToOSS(file, PROJECT_FOLDER);
            
            // 只有当项目ID > 0时才保存到项目附件表
            if (projectId != null && projectId > 0 && userId != null) {
                ProjectAttachment projectAttachment = new ProjectAttachment();
                projectAttachment.setProjectId(projectId);
                projectAttachment.setFileName(fileName);
                projectAttachment.setFilePath(fileUrl);
                projectAttachment.setFileSize(file.getSize());
                projectAttachment.setFileType(file.getContentType());
                projectAttachment.setCreateUser(userId);
                projectAttachment.setUpdateUser(userId);
                
                projectAttachmentMapper.insert(projectAttachment);
                log.info("项目文件[{}]记录已保存到数据库, 项目ID: {}, 文件大小: {}, 文件类型: {}", 
                        fileName, projectId, file.getSize(), file.getContentType());
            }
            
            return fileUrl;
        } catch (Exception e) {
            log.error("项目文件[{}]上传失败, 错误: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("项目文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传项目文件到阿里云OSS的project/文件夹并保存至项目附件表
     * @param files 文件列表
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 文件URL列表
     */
    @Override
    public List<String> uploadProjectFiles(List<MultipartFile> files, Long projectId, Long userId) {
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            String url = uploadProjectFile(file, projectId, userId);
            urls.add(url);
        }
        
        return urls;
    }
    
    /**
     * 上传用户头像到阿里云OSS的avatar/文件夹并更新用户头像
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像URL
     */
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        String fileName = file.getOriginalFilename();
        String objectName = null;
        
        try {
            // 生成唯一文件名（头像不加密，方便直接访问）
            String uniqueFileName = userId + "_" + UUID.randomUUID().toString().replace("-", "") + 
                    (fileName != null ? fileName.substring(fileName.lastIndexOf(".")) : ".jpg");
            objectName = AVATAR_FOLDER + uniqueFileName;
            
            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            // 上传到OSS
            ossClient.putObject(ossConfig.getBucketName(), objectName, file.getInputStream(), metadata);
            log.info("用户头像[{}]上传成功，OSS路径：{}", fileName, objectName);
            
            // 获取文件URL
            String fileUrl = ossConfig.getUrlPrefix() + "/" + objectName;
            
            // 更新用户头像
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setAvatar(fileUrl);
                userMapper.updateById(user);
                log.info("用户[{}]头像已更新", userId);
            }
            
            return fileUrl;
        } catch (Exception e) {
            log.error("用户头像[{}]上传失败", fileName, e);
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 从OSS获取文件并解密
     * @param objectKey OSS对象键（文件路径）
     * @return 解密后的文件流
     */
    @Override
    public InputStream getDecryptedFile(String objectKey) {
        try {
            // 检查文件是否存在
            boolean exists = ossClient.doesObjectExist(ossConfig.getBucketName(), objectKey);
            if (!exists) {
                log.error("文件不存在，路径: {}", objectKey);
                throw new RuntimeException("文件不存在");
            }
            
            // 获取文件对象
            com.aliyun.oss.model.OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), objectKey);
            
            // 读取加密数据
            InputStream encryptedStream = ossObject.getObjectContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = encryptedStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] encryptedBytes = baos.toByteArray();
            encryptedStream.close();
            
            // 关闭OSS对象流
            ossObject.close();
            
            // 直接解密二进制数据
            ByteArrayInputStream bais = new ByteArrayInputStream(encryptedBytes);
            
            // 创建解密密钥
            SecretKeySpec secretKey = generateSecretKey(ossConfig.getEncryptionKey());
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            // 使用解密Cipher处理输入流
            CipherInputStream cipherInputStream = new CipherInputStream(bais, cipher);
            
            // 读取解密后的数据到输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            buffer = new byte[4096];
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            cipherInputStream.close();
            
            // 返回解密后的数据流
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("获取并解密文件失败，路径: {}", objectKey, e);
            throw new RuntimeException("获取文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成AES密钥
     */
    private SecretKeySpec generateSecretKey(String key) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        keyBytes = sha.digest(keyBytes);
        byte[] truncatedBytes = new byte[16]; // AES需要16字节密钥
        System.arraycopy(keyBytes, 0, truncatedBytes, 0, truncatedBytes.length);
        return new SecretKeySpec(truncatedBytes, "AES");
    }
    
    /**
     * 获取文件元数据（MIME类型、文件大小等）
     * @param objectKey OSS对象键（文件路径）
     * @return 文件元数据
     */
    @Override
    public ObjectMetadata getFileMetadata(String objectKey) {
        try {
            return ossClient.getObjectMetadata(ossConfig.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("获取文件元数据失败，路径: {}", objectKey, e);
            throw new RuntimeException("获取文件元数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 从对象键中提取文件名
     * @param objectKey OSS对象键
     * @return 文件名
     */
    private String getFileNameFromObjectKey(String objectKey) {
        int lastSlashIndex = objectKey.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < objectKey.length() - 1) {
            return objectKey.substring(lastSlashIndex + 1);
        }
        return objectKey;
    }
    
    /**
     * 从URL中提取OSS对象键
     * @param url 完整的OSS URL
     * @return OSS对象键
     */
    @Override
    public String getObjectKeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        String prefix = ossConfig.getUrlPrefix() + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        
        return url;
    }
} 