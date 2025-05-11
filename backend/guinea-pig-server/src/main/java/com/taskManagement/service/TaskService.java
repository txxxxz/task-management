package com.taskManagement.service;

import com.taskManagement.dto.TaskAttachmentDTO;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.vo.TaskVO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
     * @param startTime 创建开始日期
     * @param endTime 创建结束日期
     * @param dueStartTime 截止开始日期
     * @param dueEndTime 截止结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数
     */
    Map<String, Object> getTaskList(String keyword, Integer status, Integer priority,
                                 Long projectId, LocalDate startTime, LocalDate endTime,
                                 LocalDate dueStartTime, LocalDate dueEndTime, Integer page, Integer pageSize);

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
     * @param startTime 创建开始日期
     * @param endTime 创建结束日期
     * @param dueStartTime 截止开始日期
     * @param dueEndTime 截止结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数（仅包含标签ID，不包含颜色等额外信息）
     */
    Map<String, Object> getProjectTasks(Long projectId, String keyword, Integer status,
                                      Integer priority, LocalDate startTime, LocalDate endTime,
                                      LocalDate dueStartTime, LocalDate dueEndTime, Integer page, Integer pageSize);

    /**
     * 根据项目ID获取任务VO列表
     * @param projectId 项目ID
     * @return 任务VO列表
     */
    List<TaskVO> getTaskVOListByProjectId(Long projectId);

    /**
     * 上传任务附件
     * @param taskId 任务ID
     * @param file 文件
     * @return 附件信息
     */
    public TaskAttachmentDTO uploadTaskAttachment(Long taskId, Object file);
    
    /**
     * 批量上传任务附件
     * @param taskId 任务ID
     * @param files 文件列表
     * @return 附件信息列表
     */
    public List<TaskAttachmentDTO> batchUploadTaskAttachments(Long taskId, List<Object> files);
    
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
    List<TaskAttachmentDTO> getTaskAttachments(Long taskId);
    
    /**
     * 删除任务附件
     * @param taskId 任务ID
     * @param attachmentId 附件ID
     */
    void deleteTaskAttachment(Long taskId, Long attachmentId);

    /**
     * 获取任务成员列表
     * @param taskId 任务ID
     * @return 成员用户名列表
     */
    List<String> getTaskMembers(Long taskId);
    
    /**
     * 添加任务成员
     * @param taskId 任务ID
     * @param username 用户名
     */
    void addTaskMember(Long taskId, String username);
    
    /**
     * 批量添加任务成员
     * @param taskId 任务ID
     * @param usernames 用户名列表
     * @return 添加成功的数量
     */
    int batchAddTaskMembers(Long taskId, List<String> usernames);
    
    /**
     * 移除任务成员
     * @param taskId 任务ID
     * @param username 用户名
     */
    void removeTaskMember(Long taskId, String username);
    
    /**
     * 根据成员搜索任务
     * @param memberUsername 成员用户名
     * @param page 页码
     * @param pageSize 每页大小
     * @return 任务列表和总数
     */
    Map<String, Object> getTasksByMember(String memberUsername, Integer page, Integer pageSize);
    
    /**
     * 获取用户任务统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getUserTaskStats(Long userId);
    
    /**
     * 获取用户任务列表根据状态
     * @param userId 用户ID
     * @param status 任务状态，null表示所有状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数
     */
    Map<String, Object> getUserTasksByStatus(Long userId, Integer status, Integer page, Integer pageSize);
    
    /**
     * 获取用户今日到期的任务列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数
     */
    Map<String, Object> getUserTodayExpiredTasks(Long userId, Integer page, Integer pageSize);
} 