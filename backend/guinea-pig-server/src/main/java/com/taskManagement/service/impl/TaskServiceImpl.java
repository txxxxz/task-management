package com.taskManagement.service.impl;

import com.taskManagement.context.BaseContext;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.entity.Project;
import com.taskManagement.entity.Task;
import com.taskManagement.mapper.ProjectMapper;
import com.taskManagement.mapper.TaskMapper;
import com.taskManagement.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // taskMapper.insert(task);
        
        // 模拟ID生成
        task.setId(1L);
        
        // 5. 返回DTO
        TaskDTO resultDTO = new TaskDTO();
        BeanUtils.copyProperties(task, resultDTO);
        
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
     * @return 任务列表和总数
     */
    @Override
    public Map<String, Object> getTaskList(String keyword, Integer status, Integer priority,
                                          Long projectId, Integer page, Integer pageSize) {
        log.info("获取任务列表: keyword={}, status={}, priority={}, projectId={}, page={}, pageSize={}",
                keyword, status, priority, projectId, page, pageSize);
        
        // 这里暂时返回模拟数据
        // 实际项目中应该使用MyBatis-Plus的Page对象和条件构造器进行查询
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("items", new ArrayList<>());
        
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
        
        // 这里暂时返回模拟数据
        // 实际项目中应该从数据库查询任务，并转换为DTO
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(id);
        taskDTO.setTitle("示例任务");
        taskDTO.setDescription("这是一个示例任务，ID为" + id);
        taskDTO.setStatus(0);
        taskDTO.setPriority(1);
        taskDTO.setProjectId(1L);
        
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
        // Task task = taskMapper.selectById(id);
        // if (task == null) {
        //     throw new BusinessException("任务不存在");
        // }
        
        // 2. 检查是否有权限修改
        // Long currentUserId = BaseContext.getCurrentId();
        // if (!task.getCreateUser().equals(currentUserId)) {
        //     throw new BusinessException("没有权限修改该任务");
        // }
        
        // 3. 更新任务
        taskDTO.setId(id);
        // Task updateTask = new Task();
        // BeanUtils.copyProperties(taskDTO, updateTask);
        // updateTask.setUpdateUser(currentUserId);
        // updateTask.setUpdateTime(LocalDateTime.now());
        
        // 4. 持久化更新
        // taskMapper.updateById(updateTask);
        
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
        // Task task = taskMapper.selectById(id);
        // if (task == null) {
        //     throw new BusinessException("任务不存在");
        // }
        
        // 2. 检查是否有权限删除
        // Long currentUserId = BaseContext.getCurrentId();
        // if (!task.getCreateUser().equals(currentUserId)) {
        //     throw new BusinessException("没有权限删除该任务");
        // }
        
        // 3. 删除任务
        // taskMapper.deleteById(id);
    }

    /**
     * 获取任务统计信息
     * @param projectId 项目ID (可选)
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getTaskStats(Long projectId) {
        log.info("获取任务统计信息: projectId={}", projectId);
        
        // 这里暂时返回模拟数据
        // 实际项目中应该基于projectId从数据库统计各状态任务数量
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 10);
        stats.put("pending", 3);
        stats.put("inProgress", 4);
        stats.put("completed", 3);
        
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
     * @return 任务列表和总数
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
        
        // 这里暂时返回模拟数据
        // 实际项目中应该使用MyBatis-Plus的Page对象和条件构造器进行查询
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("items", new ArrayList<>());
        
        return result;
    }
} 