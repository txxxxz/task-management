package com.taskManagement.controller;

import com.taskManagement.result.Result;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Slf4j
@Api(tags = "任务管理接口")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 创建任务
     * @param taskDTO 任务数据
     * @return 创建后的任务
     */
    @PostMapping
    @ApiOperation("创建任务")
    public Result<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        log.info("创建任务：{}", taskDTO);
        // 这里暂时返回模拟数据，实际应该调用service
        taskDTO.setId(1L);
        return Result.success(taskDTO);
    }

    /**
     * 获取任务列表
     * @param keyword 关键词
     * @param status 状态
     * @param priority 优先级
     * @param projectId 项目ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    @GetMapping
    @ApiOperation("获取任务列表")
    public Result<Map<String, Object>> getTaskList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取任务列表: keyword={}, status={}, priority={}, projectId={}, page={}, pageSize={}",
                keyword, status, priority, projectId, page, pageSize);
        
        // 这里返回模拟数据，实际项目应该调用service
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("items", new Object[0]);
        return Result.success(result);
    }

    /**
     * 获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取任务详情")
    public Result<TaskDTO> getTaskDetail(@PathVariable Long id) {
        log.info("获取任务详情: id={}", id);
        // 这里返回模拟数据，实际项目应该调用service
        TaskDTO task = new TaskDTO();
        task.setId(id);
        task.setTitle("示例任务");
        task.setDescription("这是一个示例任务");
        task.setStatus(0);
        task.setPriority(1);
        return Result.success(task);
    }

    /**
     * 更新任务
     * @param id 任务ID
     * @param taskDTO 任务数据
     * @return 更新后的任务
     */
    @PutMapping("/{id}")
    @ApiOperation("更新任务")
    public Result<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        log.info("更新任务: id={}, task={}", id, taskDTO);
        taskDTO.setId(id);
        // 这里返回模拟数据，实际项目应该调用service
        return Result.success(taskDTO);
    }

    /**
     * 删除任务
     * @param id 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除任务")
    public Result<String> deleteTask(@PathVariable Long id) {
        log.info("删除任务: id={}", id);
        // 这里返回模拟成功，实际项目应该调用service
        return Result.success("删除成功");
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
        
        // 这里返回模拟数据，实际项目应该调用service
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 10);
        stats.put("pending", 3);
        stats.put("inProgress", 4);
        stats.put("completed", 3);
        
        return Result.success(stats);
    }

    /**
     * 获取项目任务列表
     * @param projectId 项目ID
     * @param keyword 关键词
     * @param status 状态
     * @param priority 优先级
     * @param page 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    @GetMapping("/project/{projectId}")
    @ApiOperation("获取项目任务列表")
    public Result<Map<String, Object>> getProjectTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取项目任务列表: projectId={}, keyword={}, status={}, priority={}, page={}, pageSize={}",
                projectId, keyword, status, priority, page, pageSize);
        
        // 这里返回模拟数据，实际项目应该调用service
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("items", new Object[0]);
        return Result.success(result);
    }
} 