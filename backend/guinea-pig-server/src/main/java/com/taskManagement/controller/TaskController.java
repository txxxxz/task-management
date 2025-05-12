package com.taskManagement.controller;

import com.taskManagement.result.Result;
import com.taskManagement.dto.TaskDTO;
import com.taskManagement.dto.CommentDTO;
import com.taskManagement.vo.CommentVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.service.CommentService;
import com.taskManagement.service.TaskService;
import com.taskManagement.dto.TaskAttachmentDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.taskManagement.context.BaseContext;
import com.taskManagement.service.FileService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/tasks")
@Slf4j
@Api(tags = "task management interface")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FileService fileService;

    /**
     * create task
     * @param taskDTO task data
     * @return created task
     */
    @PostMapping
    @ApiOperation("create task")
    public Result<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        log.info("create task: {}", taskDTO);
        
        // call service to create task
        TaskDTO createdTask = taskService.createTask(taskDTO);
        return Result.success(createdTask);
    }

    /**
     * 获取任务列表
     * @param keyword 关键词
     * @param status 状态
     * @param priority 优先级
     * @param projectId 项目ID
     * @param member 成员
     * @param tags 标签
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dueStartTime 截止开始时间
     * @param dueEndTime 截止结束时间
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
            @RequestParam(required = false) String member,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) LocalDate startTime,
            @RequestParam(required = false) LocalDate endTime,
            @RequestParam(required = false) LocalDate dueStartTime,
            @RequestParam(required = false) LocalDate dueEndTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取任务列表: keyword={}, status={}, priority={}, projectId={}, member={}, tags={}, startTime={}, endTime={}, dueStartTime={}, dueEndTime={}, page={}, pageSize={}",
                keyword, status, priority, projectId, member, tags, startTime, endTime, dueStartTime, dueEndTime, page, pageSize);
        
        Map<String, Object> result;
        
        // 如果指定了成员查询，则使用特定的方法
        if (StringUtils.isNotBlank(member)) {
            result = taskService.getTasksByMember(member, page, pageSize);
        } else {
            result = taskService.getTaskList(keyword, status, priority, projectId, tags, startTime, endTime, dueStartTime, dueEndTime, page, pageSize);
        }

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
        // 调用service
        TaskDTO taskDetail = taskService.getTaskDetail(id);
        return Result.success(taskDetail);
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
        // 调用service
        TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
        return Result.success(updatedTask);
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
        // 调用service
        taskService.deleteTask(id);
        return Result.success("删除成功");
    }

    /**
     * 获取项目任务列表
     * @param projectId 项目ID
     * @param keyword 关键词
     * @param status 状态
     * @param priority 优先级
     * @param tags 标签
     * @param startTime 创建开始日期
     * @param endTime 创建结束日期
     * @param dueStartTime 截止开始日期
     * @param dueEndTime 截止结束日期
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
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) LocalDate startTime,
            @RequestParam(required = false) LocalDate endTime,
            @RequestParam(required = false) LocalDate dueStartTime,
            @RequestParam(required = false) LocalDate dueEndTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取项目任务列表: projectId={}, keyword={}, status={}, priority={}, tags={}, startTime={}, endTime={}, dueStartTime={}, dueEndTime={}, page={}, pageSize={}",
                projectId, keyword, status, priority, tags, startTime, endTime, dueStartTime, dueEndTime, page, pageSize);
        
        // 调用service
        Map<String, Object> projectTasks = taskService.getProjectTasks(projectId, keyword, status, priority, tags, startTime, endTime, dueStartTime, dueEndTime, page, pageSize);
        return Result.success(projectTasks);
    }
    
    /**
     * 获取任务评论列表
     * @param taskId 任务ID
     * @return 评论列表
     */
    @GetMapping("/{taskId}/comments")
    @ApiOperation("获取任务评论列表")
    public Result<List<CommentVO>> getTaskComments(@PathVariable Long taskId) {
        log.info("获取任务评论列表: taskId={}", taskId);
        
        // 调用service获取评论列表
        List<CommentDTO> commentDTOs = commentService.getCommentsByTaskId(taskId);
        
        // 将DTO转换为VO
        List<CommentVO> commentVOs = convertToCommentVOList(commentDTOs);
        
        return Result.success(commentVOs);
    }
    
    /**
     * 将CommentDTO列表转换为CommentVO列表
     * @param commentDTOs CommentDTO列表
     * @return CommentVO列表
     */
    private List<CommentVO> convertToCommentVOList(List<CommentDTO> commentDTOs) {
        if (commentDTOs == null) {
            return null;
        }
        
        List<CommentVO> commentVOs = new ArrayList<>();
        for (CommentDTO commentDTO : commentDTOs) {
            CommentVO commentVO = convertToCommentVO(commentDTO);
            commentVOs.add(commentVO);
        }
        
        return commentVOs;
    }
    
    /**
     * 将单个CommentDTO转换为CommentVO
     * @param commentDTO CommentDTO对象
     * @return CommentVO对象
     */
    private CommentVO convertToCommentVO(CommentDTO commentDTO) {
        if (commentDTO == null) {
            return null;
        }
        
        CommentVO commentVO = new CommentVO();
        commentVO.setId(commentDTO.getId());
        commentVO.setContent(commentDTO.getContent());
        commentVO.setTaskId(commentDTO.getTaskId());
        commentVO.setParentId(commentDTO.getParentId());
        commentVO.setCreateTime(commentDTO.getCreateTime());
        
        // 设置创建用户信息
        UserVO creatorVO = new UserVO();
        creatorVO.setId(commentDTO.getCreateUser());
        creatorVO.setUsername(commentDTO.getCreateUserName());
        creatorVO.setAvatar(commentDTO.getCreateUserAvatar());
        commentVO.setCreator(creatorVO);
        
        // 递归转换子评论
        if (commentDTO.getChildren() != null && !commentDTO.getChildren().isEmpty()) {
            commentVO.setChildren(convertToCommentVOList(commentDTO.getChildren()));
        }
        
        return commentVO;
    }

    /**
     * 添加任务评论
     * @param taskId 任务ID
     * @param commentDTO 评论数据
     * @return 创建后的评论
     */
    @PostMapping("/{taskId}/comments")
    @ApiOperation("添加任务评论")
    public Result<CommentVO> createComment(
            @PathVariable Long taskId,
            @RequestBody CommentDTO commentDTO) {
        log.info("添加任务评论: taskId={}, comment={}", taskId, commentDTO);
        
        // 设置任务ID
        commentDTO.setTaskId(taskId);
        
        // 调用service添加评论
        CommentDTO createdComment = commentService.createComment(commentDTO);
        
        // 将DTO转换为VO
        CommentVO commentVO = convertToCommentVO(createdComment);
        
        return Result.success(commentVO);
    }
    
    /**
     * 删除任务评论
     * @param taskId 任务ID
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/{taskId}/comments/{commentId}")
    @ApiOperation("删除任务评论")
    public Result<String> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId) {
        log.info("删除任务评论: taskId={}, commentId={}", taskId, commentId);
        
        // 调用service删除评论
        commentService.deleteComment(taskId, commentId);
        return Result.success("删除评论成功");
    }
    
    /**
     * 获取任务附件列表
     * @param taskId 任务ID
     * @return 附件列表
     */
    @GetMapping("/{taskId}/attachments")
    @ApiOperation("获取任务附件列表")
    public Result<List<TaskAttachmentDTO>> getTaskAttachments(@PathVariable Long taskId) {
        log.info("获取任务附件列表: taskId={}", taskId);
        
        // 调用service获取附件列表
        List<TaskAttachmentDTO> attachments = taskService.getTaskAttachments(taskId);
        return Result.success(attachments);
    }
    
    /**
     * 上传任务附件
     * @param taskId 任务ID
     * @param file 文件
     * @return 上传后的附件信息
     */
    @PostMapping("/{taskId}/attachments")
    @ApiOperation("上传任务附件")
    public Result<TaskAttachmentDTO> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) {
        log.info("上传任务附件: taskId={}, fileName={}", taskId, file != null ? file.getOriginalFilename() : "文件为空");
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                userId = 1L; // 默认用户ID
            }
            
            // 使用FileService来处理文件上传
            String fileUrl = fileService.uploadTaskFile(file, taskId, userId);
            
            // 构建返回结果
            TaskAttachmentDTO dto = new TaskAttachmentDTO();
            dto.setTaskId(taskId);
            dto.setFileName(file.getOriginalFilename());
            dto.setFileSize(file.getSize());
            dto.setFileType(file.getContentType());
            dto.setFilePath(fileUrl);
            dto.setCreateUser(userId);
            
            return Result.success(dto);
        } catch (Exception e) {
            log.error("任务附件上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传任务附件
     * @param taskId 任务ID
     * @param files 文件列表
     * @return 上传后的附件信息
     */
    @PostMapping("/{taskId}/attachments/batch")
    @ApiOperation("批量上传任务附件")
    public Result<List<String>> batchUploadTaskAttachments(
            @PathVariable Long taskId,
            @RequestParam("files") List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        Long userId = BaseContext.getCurrentId();
        if (userId == null) userId = 1L;

        List<String> urls = taskService.batchUploadTaskAttachments(taskId, files, userId);
        return Result.success(urls);
    }

    
    /**
     * 删除任务附件
     * @param taskId 任务ID
     * @param attachmentId 附件ID
     * @return 删除结果
     */
    @DeleteMapping("/{taskId}/attachments/{attachmentId}")
    @ApiOperation("删除任务附件")
    public Result<String> deleteAttachment(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId) {
        log.info("删除任务附件: taskId={}, attachmentId={}", taskId, attachmentId);
        
        // 调用service删除附件
        taskService.deleteTaskAttachment(taskId, attachmentId);
        return Result.success("删除附件成功");
    }
}