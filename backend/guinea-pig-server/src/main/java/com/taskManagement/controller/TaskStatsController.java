package com.taskManagement.controller;

import com.taskManagement.dto.TaskStatusStatsDTO;
import com.taskManagement.service.TaskStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 任务统计控制器
 */
@RestController
@RequestMapping("/api/task")
public class TaskStatsController {

    @Autowired
    private TaskStatsService taskStatsService;

    /**
     * 获取每日任务状态统计数据
     * @return 任务状态统计数据列表
     */
    @GetMapping("/statusStatsByDay")
    public List<TaskStatusStatsDTO> getTaskStatusStatsByDay() {
        return taskStatsService.getTaskStatusStatsByDay();
    }
} 