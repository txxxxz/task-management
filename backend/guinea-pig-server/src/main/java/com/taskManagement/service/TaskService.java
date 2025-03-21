package com.taskManagement.service;

import com.taskManagement.dto.TaskDTO;
import com.taskManagement.entity.Task;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 创建任务
     * @param taskDTO 任务数据
     * @return 创建的任务
     */
    TaskDTO createTask(TaskDTO taskDTO);

    /**
     * 获取任务列表
     * @param keyword 关键词
     * @param status 状态
     * @param priority 优先级
     * @param projectId 项目ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数
     */
    Map<String, Object> getTaskList(String keyword, Integer status, Integer priority, 
                                   Long projectId, Integer page, Integer pageSize);

    /**
     * 获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDTO getTaskDetail(Long id);

    /**
     * 更新任务
     * @param id 任务ID
     * @param taskDTO 任务数据
     * @return 更新后的任务
     */
    TaskDTO updateTask(Long id, TaskDTO taskDTO);

    /**
     * 删除任务
     * @param id 任务ID
     */
    void deleteTask(Long id);

    /**
     * 获取任务统计信息
     * @param projectId 项目ID (可选)
     * @return 统计信息
     */
    Map<String, Object> getTaskStats(Long projectId);

    /**
     * 获取项目任务列表
     * @param projectId 项目ID
     * @param keyword 关键词
     * @param status 状态
     * @param priority 优先级
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数
     */
    Map<String, Object> getProjectTasks(Long projectId, String keyword, Integer status, 
                                       Integer priority, Integer page, Integer pageSize);

    /**
     * 上传任务附件
     * @param taskId 任务ID
     * @param file 文件
     * @param userId 用户ID
     * @return 文件URL
     */
    String uploadTaskAttachment(Long taskId, MultipartFile file, Long userId);
    
    /**
     * 批量上传任务附件
     * @param taskId 任务ID
     * @param files 文件列表
     * @param userId 用户ID
     * @return 文件URL列表
     */
    List<String> batchUploadTaskAttachments(Long taskId, List<MultipartFile> files, Long userId);
    
    /**
     * 获取任务附件列表
     * @param taskId 任务ID
     * @return 附件列表
     */
    List<Map<String, Object>> getTaskAttachments(Long taskId);
} 