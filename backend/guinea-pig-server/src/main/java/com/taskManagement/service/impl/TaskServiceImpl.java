package com.taskManagement.service.impl;

import com.taskManagement.context.BaseContext;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.vo.TaskVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.entity.Project;
import com.taskManagement.entity.Task;
import com.taskManagement.entity.TaskAttachment;
import com.taskManagement.dto.TaskAttachmentDTO;
import com.taskManagement.entity.TaskMember;
import com.taskManagement.entity.User;
import com.taskManagement.mapper.ProjectMapper;
import com.taskManagement.mapper.TaskMapper;
import com.taskManagement.mapper.TaskAttachmentMapper;
import com.taskManagement.mapper.TaskMemberMapper;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.service.TaskService;
import com.taskManagement.service.TaskTagService;
import com.taskManagement.service.FileService;
import com.taskManagement.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private TaskTagService taskTagService;
    
    @Autowired
    private TaskAttachmentMapper taskAttachmentMapper;
    
    @Autowired
    private TaskMemberMapper taskMemberMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * 创建任务
     * @param taskDTO 任务数据
     * @return 创建的任务
     */
    @Override
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        log.info("创建任务：{}", taskDTO);
        
        // 1. 检查项目是否存在
        Project project = projectMapper.selectById(taskDTO.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 2. 创建任务实体
        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task);
        
        // 3. 设置任务基本信息
        Long userId = BaseContext.getCurrentId();
        task.setCreateUser(userId);
        task.setUpdateUser(userId);
        task.setCommentCount(0);
        
        // 4. 持久化任务
        try {
            taskMapper.insert(task);
            log.info("任务创建成功，ID: {}", task.getId());
            
            // 5. 处理标签关联
            if (taskDTO.getTagIds() != null && !taskDTO.getTagIds().isEmpty()) {
                log.info("开始处理任务标签关联，任务ID: {}, 标签IDs: {}", task.getId(), taskDTO.getTagIds());
                try {
                    int addedCount = taskTagService.batchAddTaskTags(task.getId(), taskDTO.getTagIds());
                    log.info("成功添加 {} 个标签关联到任务 {}", addedCount, task.getId());
                } catch (Exception e) {
                    log.error("处理任务标签关联失败: {}", e.getMessage(), e);
                    // 不抛出异常，继续处理其他部分
                }
            }
            
            // 6. 处理成员关联
            if (taskDTO.getMembers() != null && !taskDTO.getMembers().isEmpty()) {
                log.info("开始处理任务成员关联，任务ID: {}, 成员: {}", task.getId(), taskDTO.getMembers());
                try {
                    int addedCount = batchAddTaskMembers(task.getId(), taskDTO.getMembers());
                    log.info("成功添加 {} 个成员关联到任务 {}", addedCount, task.getId());
                } catch (Exception e) {
                    log.error("处理任务成员关联失败: {}", e.getMessage(), e);
                    // 不抛出异常，继续处理其他部分
                }
            }
            
            // 7. 返回DTO
            TaskDTO resultDTO = new TaskDTO();
            BeanUtils.copyProperties(task, resultDTO);
            
            // 设置标签ID列表到返回DTO
            resultDTO.setTagIds(taskDTO.getTagIds());
            resultDTO.setMembers(taskDTO.getMembers());
            
            return resultDTO;
        } catch (Exception e) {
            log.error("创建任务失败: {}", e.getMessage(), e);
            throw new BusinessException("创建任务失败: " + e.getMessage());
        }
    }

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
    @Override
    public Map<String, Object> getTaskList(String keyword, Integer status, Integer priority,
                                          Long projectId, Integer page, Integer pageSize) {
        log.info("获取任务列表: keyword={}, status={}, priority={}, projectId={}, page={}, pageSize={}",
                keyword, status, priority, projectId, page, pageSize);
        
        // 使用LambdaQuery构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加关键词查询条件
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(Task::getName, keyword)
                    .or()
                    .like(Task::getDescription, keyword);
        }
        
        // 添加状态过滤条件
        if (status != null) {
            queryWrapper.eq(Task::getStatus, status);
        }
        
        // 添加优先级过滤条件
        if (priority != null) {
            queryWrapper.eq(Task::getPriority, priority);
        }
        
        // 添加项目ID过滤条件
        if (projectId != null) {
            queryWrapper.eq(Task::getProjectId, projectId);
        }
        
        // 排序（按优先级降序，更新时间降序）
        queryWrapper.orderByDesc(Task::getPriority)
                  .orderByDesc(Task::getUpdateTime);
        
        // 执行分页查询
        Page<Task> pageInfo = new Page<>(page, pageSize);
        Page<Task> taskPage = taskMapper.selectPage(pageInfo, queryWrapper);
        
        // 构建返回结果
        List<TaskDTO> taskDTOs = new ArrayList<>();
        for (Task task : taskPage.getRecords()) {
            TaskDTO taskDTO = new TaskDTO();
            BeanUtils.copyProperties(task, taskDTO);
            
            // 查询任务标签ID
            List<Long> tagIds = taskTagService.getTagIdsByTaskId(task.getId());
            taskDTO.setTagIds(tagIds);
            
            // 查询任务成员
            List<String> members = getTaskMembers(task.getId());
            taskDTO.setMembers(members);
            
            taskDTOs.add(taskDTO);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", taskPage.getTotal());
        result.put("items", taskDTOs);
        
        return result;
    }

    /**
     * 获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    @Override
    public TaskDTO getTaskDetail(Long id) {
        log.info("获取任务详情: id={}", id);
        
        // 1. 查询任务基本信息
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 2. 转换为DTO
        TaskDTO taskDTO = new TaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        
        // 3. 查询任务标签ID
        List<Long> tagIds = taskTagService.getTagIdsByTaskId(id);
        taskDTO.setTagIds(tagIds);
        
        // 4. 查询任务成员
        List<String> members = getTaskMembers(id);
        taskDTO.setMembers(members);
        
        return taskDTO;
    }

    /**
     * 更新任务
     * @param id 任务ID
     * @param taskDTO 任务数据
     * @return 更新后的任务
     */
    @Override
    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查任务是否存在
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 检查用户是否有权限更新任务 - 简单检查，用户需要是任务创建者或任务成员
        checkUserCanUpdateTask(userId, task);
        
        // 保存任务原始状态，用于后续通知
        Integer oldStatus = task.getStatus();
        
        // 更新任务基本信息
        BeanUtils.copyProperties(taskDTO, task, "id", "createTime", "createUser", "commentCount");
        
        // 添加更新者信息
        task.setUpdateUser(userId);
        task.setUpdateTime(LocalDateTime.now());
        
        // 如果状态变为已完成，设置完成时间
        if (task.getStatus() != null && task.getStatus() == 2) {
            task.setCompletedTime(LocalDateTime.now());
        }
        
        // 更新任务
        taskMapper.updateById(task);
        
        // 如果任务状态发生变化，发送通知给任务成员
        if (oldStatus != null && task.getStatus() != null && !oldStatus.equals(task.getStatus())) {
            sendTaskStatusUpdateNotifications(task, oldStatus);
        }
        
        // 查询更新后的完整任务信息
        return getTaskDetail(task.getId());
    }
    
    /**
     * 检查用户是否有权限更新任务
     * @param userId 用户ID
     * @param task 任务对象
     */
    private void checkUserCanUpdateTask(Long userId, Task task) {
        // 任务创建者可以更新
        if (task.getCreateUser().equals(userId)) {
            return;
        }
        
        // 任务成员也可以更新
        LambdaQueryWrapper<TaskMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskMember::getTaskId, task.getId())
                   .eq(TaskMember::getUserId, userId);
        Long count = taskMemberMapper.selectCount(queryWrapper);
        
        if (count == 0) {
            throw new BusinessException("您没有权限更新此任务");
        }
    }

    /**
     * 发送任务状态更新通知
     * @param task 任务对象
     * @param oldStatus 原状态
     */
    private void sendTaskStatusUpdateNotifications(Task task, Integer oldStatus) {
        // 从数据库获取最新的完整任务信息，确保有所有字段
        Task freshTask = taskMapper.selectById(task.getId());
        if (freshTask == null) {
            log.error("无法获取任务信息，任务ID: {}", task.getId());
            return;
        }
        
        // 添加更详细的日志
        log.info("获取到的任务信息: id={}, name={}, 原始任务对象: {}", freshTask.getId(), freshTask.getName(), freshTask);
        
        // 获取当前用户信息
        Long currentUserId = BaseContext.getCurrentId();
        User currentUser = userMapper.selectById(currentUserId);
        String currentUsername = currentUser != null ? currentUser.getUsername() : "用户" + currentUserId;
        
        // 获取任务新状态描述
        String newStatusDesc = getStatusDescription(freshTask.getStatus());
        
        // 确保任务名称不为空 - 使用已更新的任务对象的name属性
        // 先尝试使用传入的task对象的name
        String taskName = task.getName();
        log.info("从原始任务对象获取的名称: {}", taskName);
        
        // 如果为空，再尝试使用freshTask的name
        if (taskName == null || taskName.trim().isEmpty()) {
            taskName = freshTask.getName();
            log.info("从fresh任务对象获取的名称: {}", taskName);
        }
        
        // 如果还是为空，使用默认名称
        if (taskName == null || taskName.trim().isEmpty() || "null".equals(taskName) || "undefined".equals(taskName)) {
            taskName = "未命名任务 #" + freshTask.getId();
            log.info("任务名称为空或无效，使用默认名称: {}", taskName);
        }
        
        // 组装通知内容
        String content = currentUsername + " 将任务 [" + taskName + "] 的状态更新为 " + newStatusDesc;
        log.info("生成的通知内容: {}", content);
        
        // 获取任务成员，并发送通知
        List<TaskMember> members = taskMemberMapper.selectList(
                new LambdaQueryWrapper<TaskMember>()
                        .eq(TaskMember::getTaskId, freshTask.getId()));
        
        for (TaskMember member : members) {
            if (!member.getUserId().equals(currentUserId)) { // 不通知自己
                notificationService.createTaskUpdateNotification(freshTask.getId(), content, member.getUserId());
                log.info("已发送任务状态更新通知给用户ID: {}", member.getUserId());
            }
        }
    }
    
    /**
     * 获取任务状态描述
     * @param status 状态代码
     * @return 状态描述
     */
    private String getStatusDescription(Integer status) {
        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "进行中";
            case 2:
                return "已完成";
            case 3:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    /**
     * 删除任务
     * @param id 任务ID
     */
    @Override
    @Transactional
    public void deleteTask(Long id) {
        log.info("删除任务: id={}", id);
        
        // 1. 检查任务是否存在
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 2. 检查是否有权限删除
        Long currentUserId = BaseContext.getCurrentId();
        if (!task.getCreateUser().equals(currentUserId)) {
            throw new BusinessException("没有权限删除该任务");
        }
        
        // 3. 删除任务标签关联
        taskTagService.removeTaskTags(id);
        
        // 4. 删除任务
        taskMapper.deleteById(id);
    }

    /**
     * 获取任务统计信息
     * @param projectId 项目ID (可选)
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getTaskStats(Long projectId) {
        log.info("获取任务统计信息: projectId={}", projectId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 使用自定义Mapper方法获取总任务数
        Long total = taskMapper.countTasks(projectId, null);
        stats.put("total", total);
        
        // 查询待处理任务数 (状态0)
        Long pending = taskMapper.countTasks(projectId, 0);
        stats.put("pending", pending);
        
        // 查询进行中任务数 (状态1)
        Long inProgress = taskMapper.countTasks(projectId, 1);
        stats.put("inProgress", inProgress);
        
        // 查询已完成任务数 (状态2)
        Long completed = taskMapper.countTasks(projectId, 2);
        stats.put("completed", completed);
        
        // 查询已取消任务数 (状态3)
        Long canceled = taskMapper.countTasks(projectId, 3);
        stats.put("canceled", canceled);
        
        return stats;
    }

    /**
     * 获取项目任务列表
     * @param projectId 项目ID
     * @param keyword 关键词
     * @param status 状态
     * @param priority 优先级
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数（仅包含标签ID，不包含颜色等额外信息）
     */
    @Override
    public Map<String, Object> getProjectTasks(Long projectId, String keyword, Integer status,
                                             Integer priority, Integer page, Integer pageSize) {
        log.info("获取项目任务列表: projectId={}, keyword={}, status={}, priority={}, page={}, pageSize={}",
                projectId, keyword, status, priority, page, pageSize);
        
        // 检查项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 使用LambdaQuery构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        
        // 项目ID是必须条件
        queryWrapper.eq(Task::getProjectId, projectId);
        
        // 添加关键词查询条件
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.and(wrapper -> 
                wrapper.like(Task::getName, keyword)
                    .or()
                    .like(Task::getDescription, keyword)
            );
        }
        
        // 添加状态过滤条件
        if (status != null) {
            queryWrapper.eq(Task::getStatus, status);
        }
        
        // 添加优先级过滤条件
        if (priority != null) {
            queryWrapper.eq(Task::getPriority, priority);
        }
        
        // 排序（按优先级降序，更新时间降序）
        queryWrapper.orderByDesc(Task::getPriority)
                  .orderByDesc(Task::getUpdateTime);
        
        // 执行分页查询
        Page<Task> pageInfo = new Page<>(page, pageSize);
        Page<Task> taskPage = taskMapper.selectPage(pageInfo, queryWrapper);
        
        // 构建返回结果
        List<TaskDTO> taskDTOs = new ArrayList<>();
        for (Task task : taskPage.getRecords()) {
            TaskDTO taskDTO = new TaskDTO();
            BeanUtils.copyProperties(task, taskDTO);
            
            // 仅查询任务标签ID（标签颜色统一为浅蓝灰色#E0E7F1，由前端渲染）
            List<Long> tagIds = taskTagService.getTagIdsByTaskId(task.getId());
            taskDTO.setTagIds(tagIds);
            
            taskDTOs.add(taskDTO);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", taskPage.getTotal());
        result.put("items", taskDTOs);
        
        return result;
    }
    
    /**
     * 批量上传任务附件
     * @param taskId 任务ID
     * @param files 文件列表
     * @param userId 用户ID
     * @return 文件URL列表
     */
    @Override
    @Transactional
    public List<String> batchUploadTaskAttachments(Long taskId, List<MultipartFile> files, Long userId) {
        log.info("批量上传任务附件: taskId={}, fileCount={}, userId={}", taskId, files.size(), userId);
        
        // 1. 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 2. 使用FileService批量上传文件，重复利用已有逻辑
        List<String> fileUrls = fileService.uploadTaskFiles(files, taskId, userId);
        
        // 不再更新Task的attachmentCount，改为使用动态计算
        
        return fileUrls;
    }
    
    /**
     * 上传任务附件 (无需用户ID的重载方法，自动获取当前用户)
     * @param taskId 任务ID
     * @param file 文件
     * @return 附件信息
     */
    @Override
    @Transactional
    public TaskAttachmentDTO uploadTaskAttachment(Long taskId, Object file) {
        log.info("上传任务附件: taskId={}, file={}", taskId, file != null ? "已上传" : "为空");
        
        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        
        // 转换为MultipartFile类型
        if (!(file instanceof MultipartFile)) {
            throw new BusinessException("文件格式不正确");
        }
        
        MultipartFile multipartFile = (MultipartFile) file;
        
        // 使用FileService上传文件，重复利用已有逻辑
        String fileUrl = fileService.uploadTaskFile(multipartFile, taskId, userId);
        
        // 查询刚上传的附件信息
        LambdaQueryWrapper<TaskAttachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskAttachment::getTaskId, taskId)
                 .eq(TaskAttachment::getFilePath, fileUrl)
                 .orderByDesc(TaskAttachment::getCreateTime)
                 .last("LIMIT 1");
        
        TaskAttachment attachment = taskAttachmentMapper.selectOne(queryWrapper);
        
        // 构建返回结果
        TaskAttachmentDTO dto = new TaskAttachmentDTO();
        if (attachment != null) {
            dto.setId(attachment.getId());
            dto.setTaskId(attachment.getTaskId());
            dto.setFileName(attachment.getFileName());
            dto.setFilePath(attachment.getFilePath());
            dto.setFileSize(attachment.getFileSize());
            dto.setFileType(attachment.getFileType());
            dto.setCreateTime(attachment.getCreateTime());
            dto.setCreateUser(attachment.getCreateUser());
            
            // 获取上传者信息
            User uploader = userMapper.selectById(userId);
            if (uploader != null) {
                // 将上传者信息放入其他字段可以考虑扩展DTO
            }
        } else {
            dto.setFilePath(fileUrl);
            dto.setFileName(multipartFile.getOriginalFilename());
            dto.setFileSize(multipartFile.getSize());
        }
        
        return dto;
    }
    
    /**
     * 批量上传任务附件 (无需用户ID的重载方法，自动获取当前用户)
     * @param taskId 任务ID
     * @param files 文件列表
     * @return 附件信息列表
     */
    @Override
    @Transactional
    public List<TaskAttachmentDTO> batchUploadTaskAttachments(Long taskId, List<Object> files) {
        log.info("批量上传任务附件: taskId={}, filesCount={}", taskId, files != null ? files.size() : 0);
        
        if (files == null || files.isEmpty()) {
            throw new BusinessException("没有选择文件");
        }
        
        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        
        List<TaskAttachmentDTO> results = new ArrayList<>();
        
        // 逐个处理文件
        for (Object file : files) {
            try {
                // 调用单个文件上传方法
                TaskAttachmentDTO dto = uploadTaskAttachment(taskId, file);
                results.add(dto);
            } catch (Exception e) {
                log.error("文件上传失败: {}", e.getMessage(), e);
                // 继续处理下一个文件
            }
        }
        
        if (results.isEmpty()) {
            throw new BusinessException("所有文件上传失败");
        }
        
        return results;
    }
    
    /**
     * 删除任务附件
     * @param taskId 任务ID
     * @param attachmentId 附件ID
     */
    @Override
    @Transactional
    public void deleteTaskAttachment(Long taskId, Long attachmentId) {
        log.info("删除任务附件: taskId={}, attachmentId={}", taskId, attachmentId);
        
        // 1. 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 2. 检查附件是否存在
        TaskAttachment attachment = taskAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }
        
        // 3. 检查附件是否属于该任务
        if (!attachment.getTaskId().equals(taskId)) {
            throw new BusinessException("附件不属于该任务");
        }
        
        // 4. 获取当前用户ID
        Long currentUserId = BaseContext.getCurrentId();
        
        // 5. 检查是否有权限删除（任务创建者或附件上传者可删除）
        if (!task.getCreateUser().equals(currentUserId) && !attachment.getCreateUser().equals(currentUserId)) {
            throw new BusinessException("没有权限删除该附件");
        }
        
        // 6. 删除附件记录
        taskAttachmentMapper.deleteById(attachmentId);
        
        // 不再更新任务附件数量，改为动态计算
    }

    /**
     * 获取任务附件列表
     * @param taskId 任务ID
     * @return 附件列表
     */
    @Override
    public List<TaskAttachmentDTO> getTaskAttachments(Long taskId) {
        log.info("获取任务附件列表: taskId={}", taskId);
        
        // 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 查询附件列表
        LambdaQueryWrapper<TaskAttachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskAttachment::getTaskId, taskId);
        queryWrapper.orderByDesc(TaskAttachment::getCreateTime);
        
        List<TaskAttachment> attachments = taskAttachmentMapper.selectList(queryWrapper);
        
        // 转换为DTO对象
        return attachments.stream().map(attachment -> {
            TaskAttachmentDTO dto = new TaskAttachmentDTO();
            dto.setId(attachment.getId());
            dto.setTaskId(attachment.getTaskId());
            dto.setFileName(attachment.getFileName());
            dto.setFilePath(attachment.getFilePath());
            dto.setFileSize(attachment.getFileSize());
            dto.setFileType(attachment.getFileType());
            dto.setCreateTime(attachment.getCreateTime());
            dto.setCreateUser(attachment.getCreateUser());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取任务成员列表
     * @param taskId 任务ID
     * @return 成员用户名列表
     */
    @Override
    public List<String> getTaskMembers(Long taskId) {
        log.info("获取任务成员列表: taskId={}", taskId);
        
        // 查询任务成员关系
        LambdaQueryWrapper<TaskMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskMember::getTaskId, taskId);
        List<TaskMember> taskMembers = taskMemberMapper.selectList(queryWrapper);
        
        // 提取成员用户ID
        List<Long> userIds = taskMembers.stream()
                .map(TaskMember::getUserId)
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询用户信息
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(User::getId, userIds);
        List<User> users = userMapper.selectList(userQueryWrapper);
        
        // 提取用户名
        return users.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }
    
    /**
     * 添加任务成员
     * @param taskId 任务ID
     * @param username 用户名
     */
    @Override
    @Transactional
    public void addTaskMember(Long taskId, String username) {
        log.info("添加任务成员: taskId={}, username={}", taskId, username);
        
        // 查询用户信息
        User user = getUserByUsername(username);
        
        // 添加任务成员
        addTaskMember(taskId, user.getId());
    }
    
    /**
     * 批量添加任务成员
     * @param taskId 任务ID
     * @param usernames 用户名列表
     * @return 添加成功的数量
     */
    @Override
    @Transactional
    public int batchAddTaskMembers(Long taskId, List<String> usernames) {
        log.info("批量添加任务成员: taskId={}, usernames={}", taskId, usernames);
        
        int successCount = 0;
        for (String username : usernames) {
            try {
                addTaskMember(taskId, username);
                successCount++;
            } catch (Exception e) {
                log.error("添加任务成员失败: taskId={}, username={}", taskId, username, e);
            }
        }
        
        return successCount;
    }
    
    /**
     * 根据用户ID添加任务成员
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    private void addTaskMember(Long taskId, Long userId) {
        // 检查是否已经是成员
        LambdaQueryWrapper<TaskMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskMember::getTaskId, taskId)
                .eq(TaskMember::getUserId, userId);
        
        if (taskMemberMapper.selectCount(queryWrapper) > 0) {
            // 已经是成员，不需要添加
            return;
        }
        
        // 添加成员关系
        TaskMember taskMember = new TaskMember();
        taskMember.setTaskId(taskId);
        taskMember.setUserId(userId);
        taskMember.setCreateTime(LocalDateTime.now());
        
        taskMemberMapper.insert(taskMember);
    }
    
    /**
     * 移除任务成员
     * @param taskId 任务ID
     * @param username 用户名
     */
    @Override
    @Transactional
    public void removeTaskMember(Long taskId, String username) {
        log.info("移除任务成员: taskId={}, username={}", taskId, username);
        
        // 查询用户信息
        User user = getUserByUsername(username);
        
        // 查询任务成员关系
        LambdaQueryWrapper<TaskMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskMember::getTaskId, taskId)
                .eq(TaskMember::getUserId, user.getId());
        
        // 删除成员关系
        taskMemberMapper.delete(queryWrapper);
    }
    
    /**
     * 根据成员搜索任务
     * @param memberUsername 成员用户名
     * @param page 页码
     * @param pageSize 每页大小
     * @return 任务列表和总数
     */
    @Override
    public Map<String, Object> getTasksByMember(String memberUsername, Integer page, Integer pageSize) {
        log.info("根据成员搜索任务: memberUsername={}, page={}, pageSize={}", memberUsername, page, pageSize);
        
        // 查询用户信息
        User user = getUserByUsername(memberUsername);
        
        // 查询用户参与的任务ID
        List<Long> taskIds = taskMemberMapper.getTaskIdsByUserId(user.getId());
        
        if (taskIds.isEmpty()) {
            // 如果用户没有参与任何任务，返回空结果
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("total", 0);
            emptyResult.put("items", new ArrayList<>());
            return emptyResult;
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Task::getId, taskIds);
        
        // 排序（按优先级降序，更新时间降序）
        queryWrapper.orderByDesc(Task::getPriority)
                  .orderByDesc(Task::getUpdateTime);
        
        // 执行分页查询
        Page<Task> pageInfo = new Page<>(page, pageSize);
        Page<Task> taskPage = taskMapper.selectPage(pageInfo, queryWrapper);
        
        // 构建返回结果
        List<TaskDTO> taskDTOs = new ArrayList<>();
        for (Task task : taskPage.getRecords()) {
            TaskDTO taskDTO = new TaskDTO();
            BeanUtils.copyProperties(task, taskDTO);
            
            // 查询任务标签ID
            List<Long> tagIds = taskTagService.getTagIdsByTaskId(task.getId());
            taskDTO.setTagIds(tagIds);
            
            // 查询任务成员
            List<String> members = getTaskMembers(task.getId());
            taskDTO.setMembers(members);
            
            taskDTOs.add(taskDTO);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", taskPage.getTotal());
        result.put("items", taskDTOs);
        
        return result;
    }
    
    /**
     * 根据用户名查询用户
     */
    private User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("用户不存在: " + username);
        }
        
        return user;
    }

    /**
     * 根据项目ID获取任务VO列表
     * @param projectId 项目ID
     * @return 任务VO列表
     */
    @Override
    public List<TaskVO> getTaskVOListByProjectId(Long projectId) {
        log.info("根据项目ID获取任务VO列表: projectId={}", projectId);
        
        // 检查项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 获取项目任务列表（不分页，获取所有任务）
        Map<String, Object> result = getProjectTasks(projectId, null, null, null, 1, 1000);
        
        List<TaskVO> taskVOList = new ArrayList<>();
        if (result.containsKey("items")) {
            @SuppressWarnings("unchecked")
            List<TaskDTO> items = (List<TaskDTO>) result.get("items");
            
            for (TaskDTO taskDTO : items) {
                TaskVO taskVO = convertTaskDTOToVO(taskDTO);
                if (taskVO != null) {
                    taskVOList.add(taskVO);
                }
            }
        }
        
        return taskVOList;
    }
    
    /**
     * 将TaskDTO转换为TaskVO对象
     * @param taskDTO 任务DTO
     * @return TaskVO对象
     */
    private TaskVO convertTaskDTOToVO(TaskDTO taskDTO) {
        if (taskDTO == null) {
            return null;
        }
        
        TaskVO taskVO = new TaskVO();
        
        // 复制基本属性
        taskVO.setId(taskDTO.getId());
        taskVO.setName(taskDTO.getName());
        taskVO.setDescription(taskDTO.getDescription());
        taskVO.setProjectId(taskDTO.getProjectId());
        taskVO.setStatus(taskDTO.getStatus());
        taskVO.setPriority(taskDTO.getPriority());
        taskVO.setStartTime(taskDTO.getStartTime());
        taskVO.setDeadline(taskDTO.getDeadline());
        
        // 设置时间相关字段
        taskVO.setCreateTime(taskDTO.getCreateTime());
        taskVO.setUpdateTime(taskDTO.getUpdateTime());
        
        // 设置完成时间，注意TaskDTO中是completeTime，而TaskVO中是completedTime
        taskVO.setCompleteTime(taskDTO.getCompleteTime());
        
        // 设置评论数
        if (taskDTO.getCommentCount() != null) {
            taskVO.setCommentCount(taskDTO.getCommentCount());
        } else {
            taskVO.setCommentCount(0); // 设置默认值
        }
        
        // 其他复杂字段（creator, members, tags, comments, attachments）初始化为空或默认值
        taskVO.setCreator(null);  // 不设置创建者信息
        taskVO.setMembers(new ArrayList<>());
        taskVO.setTags(new ArrayList<>());
        taskVO.setComments(new ArrayList<>());
        taskVO.setAttachments(new ArrayList<>());
        
        // 如果有成员信息，添加到VO中
        if (taskDTO.getMembers() != null && !taskDTO.getMembers().isEmpty()) {
            List<UserVO> memberVOs = new ArrayList<>();
            for (String username : taskDTO.getMembers()) {
                try {
                    User member = getUserByUsername(username);
                    if (member != null) {
                        UserVO memberVO = new UserVO();
                        memberVO.setId(member.getId());
                        memberVO.setUsername(member.getUsername());
                        memberVOs.add(memberVO);
                    }
                } catch (Exception e) {
                    log.warn("无法找到用户: {}", username);
                }
            }
            taskVO.setMembers(memberVOs);
        }
        
        return taskVO;
    }

    /**
     * 获取用户任务统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getUserTaskStats(Long userId) {
        log.info("获取用户任务统计信息: userId={}", userId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 查询待处理任务数 (状态0)
        Long pending = countUserTasks(userId, 0);
        stats.put("pending", pending);
        
        // 查询进行中任务数 (状态1)
        Long inProgress = countUserTasks(userId, 1);
        stats.put("inProgress", inProgress);
        
        // 查询已完成任务数 (状态2)
        Long completed = countUserTasks(userId, 2);
        stats.put("completed", completed);
        
        // 查询已取消任务数 (状态3)
        Long canceled = countUserTasks(userId, 3);
        stats.put("canceled", canceled);
        
        // 查询今日到期的任务数
        Long todayExpired = countTodayExpiredTasks(userId);
        stats.put("todayExpired", todayExpired);
        
        // 查询总任务数
        Long total = countUserTasks(userId, null);
        stats.put("total", total);
        
        return stats;
    }

    /**
     * 获取用户任务列表根据状态
     * @param userId 用户ID
     * @param status 任务状态，null表示所有状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数
     */
    @Override
    public Map<String, Object> getUserTasksByStatus(Long userId, Integer status, Integer page, Integer pageSize) {
        log.info("获取用户任务列表根据状态: userId={}, status={}, page={}, pageSize={}", userId, status, page, pageSize);
        
        // 查询用户参与的任务ID
        List<Long> taskIds = taskMemberMapper.getTaskIdsByUserId(userId);
        
        // 也包括用户创建的任务
        LambdaQueryWrapper<Task> createdTasksQuery = new LambdaQueryWrapper<>();
        createdTasksQuery.eq(Task::getCreateUser, userId);
        List<Task> createdTasks = taskMapper.selectList(createdTasksQuery);
        
        List<Long> createdTaskIds = createdTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        
        // 合并任务ID并去重
        taskIds.addAll(createdTaskIds);
        taskIds = taskIds.stream().distinct().collect(Collectors.toList());
        
        if (taskIds.isEmpty()) {
            // 如果用户没有参与任何任务，返回空结果
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("total", 0);
            emptyResult.put("items", new ArrayList<>());
            return emptyResult;
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Task::getId, taskIds);
        
        // 添加状态过滤条件
        if (status != null) {
            queryWrapper.eq(Task::getStatus, status);
        }
        
        // 排序（按优先级降序，更新时间降序）
        queryWrapper.orderByDesc(Task::getPriority)
                  .orderByDesc(Task::getUpdateTime);
        
        // 执行分页查询
        Page<Task> pageInfo = new Page<>(page, pageSize);
        Page<Task> taskPage = taskMapper.selectPage(pageInfo, queryWrapper);
        
        // 构建返回结果
        List<TaskDTO> taskDTOs = new ArrayList<>();
        for (Task task : taskPage.getRecords()) {
            TaskDTO taskDTO = new TaskDTO();
            BeanUtils.copyProperties(task, taskDTO);
            
            // 查询任务标签ID
            List<Long> tagIds = taskTagService.getTagIdsByTaskId(task.getId());
            taskDTO.setTagIds(tagIds);
            
            // 查询任务成员
            List<String> members = getTaskMembers(task.getId());
            taskDTO.setMembers(members);
            
            taskDTOs.add(taskDTO);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", taskPage.getTotal());
        result.put("items", taskDTOs);
        
        return result;
    }

    /**
     * 获取用户今日到期的任务列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数
     */
    @Override
    public Map<String, Object> getUserTodayExpiredTasks(Long userId, Integer page, Integer pageSize) {
        log.info("获取用户今日到期的任务列表: userId={}, page={}, pageSize={}", userId, page, pageSize);
        
        // 查询用户参与的任务ID
        List<Long> taskIds = taskMemberMapper.getTaskIdsByUserId(userId);
        
        // 也包括用户创建的任务
        LambdaQueryWrapper<Task> createdTasksQuery = new LambdaQueryWrapper<>();
        createdTasksQuery.eq(Task::getCreateUser, userId);
        List<Task> createdTasks = taskMapper.selectList(createdTasksQuery);
        
        List<Long> createdTaskIds = createdTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        
        // 合并任务ID并去重
        taskIds.addAll(createdTaskIds);
        taskIds = taskIds.stream().distinct().collect(Collectors.toList());
        
        if (taskIds.isEmpty()) {
            // 如果用户没有参与任何任务，返回空结果
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("total", 0);
            emptyResult.put("items", new ArrayList<>());
            return emptyResult;
        }
        
        // 获取今天的日期范围
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);
        
        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Task::getId, taskIds)
                   .le(Task::getDeadline, todayEnd)  // 截止日期小于等于今天结束
                   .ge(Task::getDeadline, todayStart) // 截止日期大于等于今天开始
                   .and(i -> i.ne(Task::getStatus, 2).or().isNull(Task::getStatus)); // 排除已完成的任务
        
        // 排序（按优先级降序，截止日期升序）
        queryWrapper.orderByAsc(Task::getDeadline)
                  .orderByDesc(Task::getPriority);
        
        // 执行分页查询
        Page<Task> pageInfo = new Page<>(page, pageSize);
        Page<Task> taskPage = taskMapper.selectPage(pageInfo, queryWrapper);
        
        // 构建返回结果
        List<TaskDTO> taskDTOs = new ArrayList<>();
        for (Task task : taskPage.getRecords()) {
            TaskDTO taskDTO = new TaskDTO();
            BeanUtils.copyProperties(task, taskDTO);
            
            // 查询任务标签ID
            List<Long> tagIds = taskTagService.getTagIdsByTaskId(task.getId());
            taskDTO.setTagIds(tagIds);
            
            // 查询任务成员
            List<String> members = getTaskMembers(task.getId());
            taskDTO.setMembers(members);
            
            taskDTOs.add(taskDTO);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", taskPage.getTotal());
        result.put("items", taskDTOs);
        
        return result;
    }

    /**
     * 计算用户任务数量
     * @param userId 用户ID
     * @param status 任务状态，null表示所有状态
     * @return 任务数量
     */
    private Long countUserTasks(Long userId, Integer status) {
        // 查询用户参与的任务ID
        List<Long> taskIds = taskMemberMapper.getTaskIdsByUserId(userId);
        
        // 也包括用户创建的任务
        LambdaQueryWrapper<Task> createdTasksQuery = new LambdaQueryWrapper<>();
        createdTasksQuery.eq(Task::getCreateUser, userId);
        List<Task> createdTasks = taskMapper.selectList(createdTasksQuery);
        
        List<Long> createdTaskIds = createdTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        
        // 合并任务ID并去重
        taskIds.addAll(createdTaskIds);
        taskIds = taskIds.stream().distinct().collect(Collectors.toList());
        
        if (taskIds.isEmpty()) {
            return 0L;
        }
        
        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Task::getId, taskIds);
        
        // 添加状态过滤条件
        if (status != null) {
            queryWrapper.eq(Task::getStatus, status);
        }
        
        return taskMapper.selectCount(queryWrapper);
    }

    /**
     * 计算用户今日到期的任务数量
     * @param userId 用户ID
     * @return 任务数量
     */
    private Long countTodayExpiredTasks(Long userId) {
        // 查询用户参与的任务ID
        List<Long> taskIds = taskMemberMapper.getTaskIdsByUserId(userId);
        
        // 也包括用户创建的任务
        LambdaQueryWrapper<Task> createdTasksQuery = new LambdaQueryWrapper<>();
        createdTasksQuery.eq(Task::getCreateUser, userId);
        List<Task> createdTasks = taskMapper.selectList(createdTasksQuery);
        
        List<Long> createdTaskIds = createdTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        
        // 合并任务ID并去重
        taskIds.addAll(createdTaskIds);
        taskIds = taskIds.stream().distinct().collect(Collectors.toList());
        
        if (taskIds.isEmpty()) {
            return 0L;
        }
        
        // 获取今天的日期范围
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);
        
        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Task::getId, taskIds)
                   .le(Task::getDeadline, todayEnd)  // 截止日期小于等于今天结束
                   .ge(Task::getDeadline, todayStart) // 截止日期大于等于今天开始
                   .and(i -> i.ne(Task::getStatus, 2).or().isNull(Task::getStatus)); // 排除已完成的任务
        
        return taskMapper.selectCount(queryWrapper);
    }
} 