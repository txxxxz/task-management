package com.taskManagement.service;

import com.taskManagement.dto.TaskStatusStatsDTO;
import java.util.List;

/**
 * 任务统计服务接口
 */
public interface TaskStatsService {
    /**
     * 获取每日任务状态统计数据
     * @return 任务状态统计数据列表
     */
    List<TaskStatusStatsDTO> getTaskStatusStatsByDay();
} 