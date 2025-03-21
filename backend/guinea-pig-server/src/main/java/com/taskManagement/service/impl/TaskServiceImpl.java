package com.taskManagement.service.impl;

import com.taskManagement.context.BaseContext;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.entity.Project;
import com.taskManagement.entity.Task;
import com.taskManagement.entity.TaskAttachment;
import com.taskManagement.mapper.ProjectMapper;
import com.taskManagement.mapper.TaskMapper;
import com.taskManagement.mapper.TaskAttachmentMapper;
import com.taskManagement.service.TaskService;
import com.taskManagement.service.TaskTagService;
import com.taskManagement.service.FileService;
import com.taskManagement.vo.TaskVO;
import com.taskManagement.vo.TagVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
    private FileService fileService;

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
        task.setAttachmentCount(0);
        
        // 4. 持久化任务
        taskMapper.insert(task);
        log.info("任务创建成功，ID: {}", task.getId());
        
        // 5. 处理标签关联
        if (taskDTO.getTagIds() != null && !taskDTO.getTagIds().isEmpty()) {
            log.info("开始处理任务标签关联，任务ID: {}, 标签IDs: {}", task.getId(), taskDTO.getTagIds());
            int addedCount = taskTagService.batchAddTaskTags(task.getId(), taskDTO.getTagIds());
            log.info("成功添加 {} 个标签关联到任务 {}", addedCount, task.getId());
        }
        
        // 6. 返回DTO
        TaskDTO resultDTO = new TaskDTO();
        BeanUtils.copyProperties(task, resultDTO);
        
        // 设置标签ID列表到返回DTO
        resultDTO.setTagIds(taskDTO.getTagIds());
        
        return resultDTO;
    }

    /**
     * 获取任务列表
     * @param keyword 关键词
     * @param status 状态
     * @param priority 优先级
     * @param projectId 项目ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表和总数（仅包含标签ID，不包含颜色等额外信息）
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
     * 获取任务详情
     * @param id 任务ID
     * @return 任务详情（仅包含标签ID，不包含颜色等额外信息）
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
        log.info("更新任务: id={}, task={}", id, taskDTO);
        
        // 1. 检查任务是否存在
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 2. 检查是否有权限修改
        Long currentUserId = BaseContext.getCurrentId();
        if (!task.getCreateUser().equals(currentUserId)) {
            throw new BusinessException("没有权限修改该任务");
        }
        
        // 3. 更新任务
        taskDTO.setId(id);
        Task updateTask = new Task();
        BeanUtils.copyProperties(taskDTO, updateTask);
        
        updateTask.setUpdateUser(currentUserId);
        updateTask.setUpdateTime(LocalDateTime.now());
        
        // 4. 持久化更新
        taskMapper.updateById(updateTask);
        
        // 5. 处理标签关联
        if (taskDTO.getTagIds() != null) {
            taskTagService.updateTaskTags(id, taskDTO.getTagIds());
        }
        
        return taskDTO;
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
     * 上传任务附件
     * @param taskId 任务ID
     * @param file 文件
     * @param userId 用户ID
     * @return 文件URL
     */
    @Override
    @Transactional
    public String uploadTaskAttachment(Long taskId, MultipartFile file, Long userId) {
        log.info("上传任务附件: taskId={}, fileName={}, userId={}", taskId, file.getOriginalFilename(), userId);
        
        // 1. 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 2. 上传文件并保存到任务附件表
        String fileUrl = fileService.uploadTaskFile(file, taskId, userId);
        
        // 3. 更新任务附件数量
        task.setAttachmentCount(task.getAttachmentCount() + 1);
        taskMapper.updateById(task);
        
        return fileUrl;
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
        
        // 2. 批量上传文件并保存到任务附件表
        List<String> fileUrls = fileService.uploadTaskFiles(files, taskId, userId);
        
        // 3. 更新任务附件数量
        task.setAttachmentCount(task.getAttachmentCount() + files.size());
        taskMapper.updateById(task);
        
        return fileUrls;
    }
    
    /**
     * 获取任务附件列表
     * @param taskId 任务ID
     * @return 附件列表
     */
    @Override
    public List<Map<String, Object>> getTaskAttachments(Long taskId) {
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
        
        // 转换为前端需要的格式
        return attachments.stream().map(attachment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", attachment.getId());
            map.put("fileName", attachment.getFileName());
            map.put("filePath", attachment.getFilePath());
            map.put("fileSize", attachment.getFileSize());
            map.put("fileType", attachment.getFileType());
            map.put("createTime", attachment.getCreateTime());
            map.put("createUser", attachment.getCreateUser());
            return map;
        }).collect(Collectors.toList());
    }
} 