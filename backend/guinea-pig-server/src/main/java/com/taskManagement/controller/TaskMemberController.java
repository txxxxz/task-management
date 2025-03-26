package com.taskManagement.controller;

import com.taskManagement.result.Result;
import com.taskManagement.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务成员控制器
 */
@RestController
@RequestMapping("/tasks/members")
@Slf4j
public class TaskMemberController {

    @Autowired
    private TaskService taskService;

    /**
     * 获取任务成员列表
     * @param taskId 任务ID
     * @return 成员列表
     */
    @GetMapping("/{taskId}")
    public Result<List<String>> getTaskMembers(@PathVariable Long taskId) {
        log.info("获取任务成员列表: taskId={}", taskId);
        List<String> members = taskService.getTaskMembers(taskId);
        return Result.success(members);
    }

    /**
     * 添加任务成员
     * @param taskId 任务ID
     * @param username 用户名
     * @return 操作结果
     */
    @PostMapping("/{taskId}")
    public Result<String> addTaskMember(@PathVariable Long taskId, @RequestParam String username) {
        log.info("添加任务成员: taskId={}, username={}", taskId, username);
        taskService.addTaskMember(taskId, username);
        return Result.success("Member added successfully");
    }

    /**
     * 批量添加任务成员
     * @param taskId 任务ID
     * @param usernames 用户名列表
     * @return 操作结果
     */
    @PostMapping("/{taskId}/batch")
    public Result<String> batchAddTaskMembers(@PathVariable Long taskId, @RequestBody List<String> usernames) {
        log.info("批量添加任务成员: taskId={}, usernames={}", taskId, usernames);
        int count = taskService.batchAddTaskMembers(taskId, usernames);
        return Result.success("Added " + count + " members successfully");
    }

    /**
     * 移除任务成员
     * @param taskId 任务ID
     * @param username 用户名
     * @return 操作结果
     */
    @DeleteMapping("/{taskId}")
    public Result<String> removeTaskMember(@PathVariable Long taskId, @RequestParam String username) {
        log.info("移除任务成员: taskId={}, username={}", taskId, username);
        taskService.removeTaskMember(taskId, username);
        return Result.success("Member removed successfully");
    }

    /**
     * 根据成员搜索任务
     * @param username 成员用户名
     * @param page 页码
     * @param pageSize 每页大小
     * @return 任务列表
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> getTasksByMember(
            @RequestParam String username,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("根据成员搜索任务: username={}, page={}, pageSize={}", username, page, pageSize);
        Map<String, Object> result = taskService.getTasksByMember(username, page, pageSize);
        return Result.success(result);
    }
} 