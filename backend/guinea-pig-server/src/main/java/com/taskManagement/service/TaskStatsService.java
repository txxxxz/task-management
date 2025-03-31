package com.taskManagement.service;

import com.taskManagement.dto.TaskStatusStatsDTO;
import java.util.List;
import java.util.Map;

/**
 * 任务统计服务接口
 */
public interface TaskStatsService {
    /**
     * 获取每日任务状态统计数据
     * @param weekOffset 周偏移量（0表示当前周，正数表示未来周，负数表示过去周）
     * @return 任务状态统计数据列表
     */
    List<TaskStatusStatsDTO> getTaskStatusStatsByDay(Integer weekOffset);

    /**
     * 获取任务统计信息
     * @param projectId 项目ID (可选)
     * @return 统计信息
     */
    Map<String, Object> getTaskStats(Long projectId);

    /**
     * 获取用户任务统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getUserTaskStats(Long userId);

    /**
     * 获取用户任务列表根据状态
     * @param userId 用户ID
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    Map<String, Object> getUserTasksByStatus(Long userId, Integer status, Integer page, Integer pageSize);

    /**
     * 获取用户今日到期的任务列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    Map<String, Object> getUserTodayExpiredTasks(Long userId, Integer page, Integer pageSize);

    /**
     * 获取用户任务优先级分布
     * @param userId 用户ID
     * @return 优先级分布统计
     */
    Map<String, Object> getTaskPriorityDistribution(Long userId);
}