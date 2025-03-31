package com.taskManagement.controller;

import com.taskManagement.dto.TaskStatusStatsDTO;
import com.taskManagement.service.TaskStatsService;
import com.taskManagement.result.Result;
import com.taskManagement.context.BaseContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 任务统计控制器
 */
@RestController
@RequestMapping("/tasks/stats")
@Slf4j
@Api(tags = "任务统计接口")
public class TaskStatsController {

    @Autowired
    private TaskStatsService taskStatsService;

    

    /**
     * 获取每日任务状态统计数据
     * @return 任务状态统计数据列表
     */
    @GetMapping("/statusStatsByDay")
    @ApiOperation("获取每日任务状态统计数据")
    public Result<List<TaskStatusStatsDTO>> getTaskStatusStatsByDay(
            @RequestParam(defaultValue = "0") Integer weekOffset) {
        log.info("获取每日任务状态统计数据: weekOffset={}", weekOffset);
        return Result.success(taskStatsService.getTaskStatusStatsByDay(weekOffset));
    }

    /**
     * 获取任务统计信息
     * @param projectId 项目ID (可选)
     * @return 统计信息
     */
    @GetMapping("/stats")
    @ApiOperation("获取任务统计信息")
    public Result<Map<String, Object>> getTaskStats(@RequestParam(required = false) Long projectId) {
        log.info("获取任务统计信息: projectId={}", projectId);
        return Result.success(taskStatsService.getTaskStats(projectId));
    }

    /**
     * 获取当前用户任务统计信息
     * @return 统计信息
     */
    @GetMapping("/user/stats")
    @ApiOperation("获取当前用户任务统计信息")
    public Result<Map<String, Object>> getCurrentUserTaskStats() {
        log.info("获取当前用户任务统计信息");
        Long userId = BaseContext.getCurrentId();
        return Result.success(taskStatsService.getUserTaskStats(userId));
    }

    /**
     * 获取当前用户任务列表根据状态
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    @GetMapping("/user/status/{status}")
    @ApiOperation("获取当前用户任务列表根据状态")
    public Result<Map<String, Object>> getUserTasksByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取当前用户任务列表根据状态: status={}, page={}, pageSize={}", status, page, pageSize);
        Long userId = BaseContext.getCurrentId();
        return Result.success(taskStatsService.getUserTasksByStatus(userId, status, page, pageSize));
    }

    /**
     * 获取当前用户所有任务列表
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    @GetMapping("/user/all")
    @ApiOperation("获取当前用户所有任务列表")
    public Result<Map<String, Object>> getAllUserTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取当前用户所有任务列表: page={}, pageSize={}", page, pageSize);
        Long userId = BaseContext.getCurrentId();
        return Result.success(taskStatsService.getUserTasksByStatus(userId, null, page, pageSize));
    }

    /**
     * 获取当前用户今日到期的任务列表
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    @GetMapping("/user/today-expired")
    @ApiOperation("获取当前用户今日到期的任务列表")
    public Result<Map<String, Object>> getUserTodayExpiredTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取当前用户今日到期的任务列表: page={}, pageSize={}", page, pageSize);
        Long userId = BaseContext.getCurrentId();
        return Result.success(taskStatsService.getUserTodayExpiredTasks(userId, page, pageSize));
    }

    /**
     * 获取当前用户任务优先级分布
     * @return 优先级分布统计
     */
    @GetMapping("/priorityDistribution")
    @ApiOperation("获取当前用户任务优先级分布")
    public Result<Map<String, Object>> getTaskPriorityDistribution() {
        log.info("获取当前用户任务优先级分布");
        Long userId = BaseContext.getCurrentId();
        return Result.success(taskStatsService.getTaskPriorityDistribution(userId));
    }
}