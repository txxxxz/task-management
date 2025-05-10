package com.taskManagement.service.impl;

import com.taskManagement.dto.TaskStatusStatsDTO;
import com.taskManagement.service.TaskStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.taskManagement.mapper.TaskStatsMapper;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.service.TaskService;
import com.taskManagement.service.TaskTagService;
/**
 * 任务统计服务实现类
 */
@Service
public class TaskStatsServiceImpl implements TaskStatsService {

    @Autowired
    private TaskStatsMapper taskStatsMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskTagService taskTagService;

    /**
     * 获取指定周的任务状态统计数据
     * @param weekOffset 周偏移量（0表示当前周，正数表示未来周，负数表示过去周）
     * @return 任务状态统计数据列表
     */
    @Override
    public List<TaskStatusStatsDTO> getTaskStatusStatsByDay(Integer weekOffset) {
        List<TaskStatusStatsDTO> result = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) { // 从6到0，确保按时间顺序从早到晚
            LocalDateTime date = now.minusDays(i);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            
            // 从数据库获取当天的统计数据
            TaskStatusStatsDTO stats = taskStatsMapper.getTaskStatusStatsByDate(date, weekOffset);
            
            // 如果没有数据，创建空的统计对象
            if (stats == null) {
                stats = new TaskStatusStatsDTO();
                stats.setTotal(0);
                stats.setPending(0);
                stats.setInProgress(0);
                stats.setCompleted(0);
                stats.setCancelled(0);
                stats.setTodayExpired(0);
            } else {
                // 确保所有字段都不为null
                if (stats.getTotal() == null) stats.setTotal(0);
                if (stats.getPending() == null) stats.setPending(0);
                if (stats.getInProgress() == null) stats.setInProgress(0);
                if (stats.getCompleted() == null) stats.setCompleted(0);
                if (stats.getCancelled() == null) stats.setCancelled(0);
                if (stats.getTodayExpired() == null) stats.setTodayExpired(0);
            }
            
            stats.setDay(dayOfWeek.toString());
            result.add(stats);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getTaskStats(Long projectId) {
        // 从数据库获取项目任务统计数据
        Map<String, Object> stats = taskStatsMapper.getTaskStats(projectId);
        
        // 如果没有数据，返回默认值
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total", 0);
            stats.put("pending", 0);
            stats.put("inProgress", 0);
            stats.put("completed", 0);
            stats.put("cancelled", 0);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getUserTaskStats(Long userId) {
        // 从数据库获取用户任务统计数据
        Map<String, Object> stats = taskStatsMapper.getUserTaskStats(userId);
        
        // 如果没有数据，返回默认值
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total", 0);
            stats.put("pending", 0);
            stats.put("inProgress", 0);
            stats.put("completed", 0);
            stats.put("cancelled", 0);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getUserTasksByStatus(Long userId, Integer status, Integer page, Integer pageSize) {
        // 参数校验和默认值设置
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        
        int offset = (page - 1) * pageSize;
        Map<String, Object> result = new HashMap<>();
        
        // 获取总数和分页数据
        Integer total = taskStatsMapper.getUserTasksCount(userId, status);
        List<TaskDTO> tasks = taskStatsMapper.getUserTasksByStatus(userId, status, offset, pageSize);
        
        // 为每个任务补充成员信息
        if (tasks != null && !tasks.isEmpty()) {
            for (TaskDTO task : tasks) {
                // 查询任务成员
                List<String> members = taskService.getTaskMembers(task.getId());
                task.setMembers(members);
                
                // 查询任务标签ID
                List<Long> tagIds = taskTagService.getTagIdsByTaskId(task.getId());
                task.setTagIds(tagIds);
            }
        }
        
        // 构建返回结果
        result.put("total", total != null ? total : 0);
        result.put("items", tasks != null ? tasks : new ArrayList<>());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", total == null ? 0 : (total + pageSize - 1) / pageSize);
        
        return result;
    }

    @Override
    public Map<String, Object> getUserTodayExpiredTasks(Long userId, Integer page, Integer pageSize) {
        // 参数校验和默认值设置
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        
        int offset = (page - 1) * pageSize;
        Map<String, Object> result = new HashMap<>();
        
        // 获取总数和分页数据
        Integer total = taskStatsMapper.getUserTodayExpiredTasksCount(userId);
        List<TaskDTO> tasks = taskStatsMapper.getUserTodayExpiredTasks(userId, offset, pageSize);
        
        // 为每个任务补充成员信息
        if (tasks != null && !tasks.isEmpty()) {
            for (TaskDTO task : tasks) {
                // 查询任务成员
                List<String> members = taskService.getTaskMembers(task.getId());
                task.setMembers(members);
                
                // 查询任务标签ID
                List<Long> tagIds = taskTagService.getTagIdsByTaskId(task.getId());
                task.setTagIds(tagIds);
            }
        }
        
        // 构建返回结果
        result.put("total", total != null ? total : 0);
        result.put("items", tasks != null ? tasks : new ArrayList<>());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", total == null ? 0 : (total + pageSize - 1) / pageSize);
        
        return result;
    }

    @Override
    public Map<String, Object> getTaskPriorityDistribution(Long userId) {
        // 从数据库获取用户任务优先级分布
        Map<String, Object> distribution = taskStatsMapper.getTaskPriorityDistribution(userId);
        
        // 如果没有数据，返回默认值
        if (distribution == null) {
            distribution = new HashMap<>();
            distribution.put("low", 0);
            distribution.put("medium", 0);
            distribution.put("high", 0);
            distribution.put("critical", 0);
        }
        
        return distribution;
    }
} 