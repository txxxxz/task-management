package com.taskManagement.mapper;

import com.taskManagement.dto.TaskStatusStatsDTO;
import com.taskManagement.dto.TaskDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskStatsMapper {
    /**
     * 获取指定日期的任务状态统计
     */
    TaskStatusStatsDTO getTaskStatusStatsByDate(@Param("date") LocalDateTime date, @Param("weekOffset") Integer weekOffset);

    /**
     * 获取任务统计信息
     */
    Map<String, Object> getTaskStats(@Param("projectId") Long projectId);

    /**
     * 获取用户任务统计信息
     */
    Map<String, Object> getUserTaskStats(@Param("userId") Long userId);

    /**
     * 获取用户任务总数
     */
    Integer getUserTasksCount(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 获取用户任务列表
     */
    List<TaskDTO> getUserTasksByStatus(@Param("userId") Long userId, @Param("status") Integer status,
                                     @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /**
     * 获取用户今日到期任务总数
     */
    Integer getUserTodayExpiredTasksCount(@Param("userId") Long userId);

    /**
     * 获取用户今日到期任务列表
     */
    List<TaskDTO> getUserTodayExpiredTasks(@Param("userId") Long userId,
                                         @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /**
     * 获取用户任务优先级分布
     */
    Map<String, Object> getTaskPriorityDistribution(@Param("userId") Long userId);
} 