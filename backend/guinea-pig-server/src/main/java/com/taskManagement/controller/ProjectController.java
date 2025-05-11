package com.taskManagement.controller;

import com.taskManagement.dto.ProjectAttachmentDTO;
import com.taskManagement.dto.ProjectDTO;
import com.taskManagement.entity.Project;
import com.taskManagement.result.Result;
import com.taskManagement.service.ProjectService;
import com.taskManagement.service.TaskService;
import com.taskManagement.vo.PageResult;
import com.taskManagement.vo.ProjectVO;
import com.taskManagement.vo.TaskVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.context.BaseContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 项目管理相关接口
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    /**
     * 获取项目列表
     * @param keyword 关键字搜索
     * @param status 项目状态
     * @param page 页码
     * @param pageSize 每页大小
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dueStartTime 任务开始时间
     * @param dueEndTime 任务结束时间
     * @return 项目列表
     */
    @GetMapping
    public Result<PageResult<ProjectVO>> getProjectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) LocalDate startTime,
            @RequestParam(required = false) LocalDate endTime,
            @RequestParam(required = false) LocalDate dueStartTime,
            @RequestParam(required = false) LocalDate dueEndTime) {
        log.info("获取项目列表，keyword={}, status={}, page={}, pageSize={}, startTime={}, endTime={}, dueStartTime={}, dueEndTime={}", 
                keyword, status, page, pageSize, startTime, endTime, dueStartTime, dueEndTime);
        
        PageResult<ProjectVO> result = projectService.getProjectList(keyword, status, page, pageSize, startTime, endTime, dueStartTime, dueEndTime);
        return Result.success(result);
    }

    /**
     * 获取项目详情
     * @param id 项目ID
     * @return 项目详情
     */
    @GetMapping("/{id}")
    public Result<ProjectVO> getProjectDetail(@PathVariable Long id) {
        log.info("获取项目详情，id={}", id);
        
        ProjectVO projectVO = projectService.getProjectDetail(id);
        return Result.success(projectVO);
    }

    /**
     * 创建项目
     * @param projectDTO 项目信息
     * @return 创建后的项目信息
     */
    @PostMapping
    public Result<ProjectVO> createProject(@RequestBody @Valid ProjectDTO projectDTO) {
        log.info("创建项目，projectDTO={}", projectDTO);
        
        ProjectVO projectVO = projectService.createProject(projectDTO);
        return Result.success(projectVO);
    }

    /**
     * 更新项目
     * @param id 项目ID
     * @param projectDTO 项目信息
     * @return 更新后的项目信息
     */
    @PutMapping("/{id}")
    public Result<ProjectVO> updateProject(@PathVariable Long id, @RequestBody @Valid ProjectDTO projectDTO) {
        log.info("更新项目，id={}, projectDTO={}", id, projectDTO);
        
        projectDTO.setId(id);
        ProjectVO projectVO = projectService.updateProject(projectDTO);
        return Result.success(projectVO);
    }

    /**
     * 删除项目
     * @param id 项目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        log.info("删除项目，id={}", id);
        
        projectService.deleteProject(id);
        return Result.success();
    }

    /**
     * 获取项目成员
     * @param id 项目ID
     * @return 项目成员列表
     */
    @GetMapping("/{id}/members")
    public Result<List<String>> getProjectMembers(@PathVariable Long id) {
        log.info("获取项目成员，id={}", id);
        
        List<String> members = projectService.getProjectMembers(id);
        return Result.success(members);
    }

    /**
     * 搜索项目成员
     * @param id 项目ID
     * @param keyword 搜索关键字
     * @param page 页码
     * @param pageSize 每页大小
     * @return 项目成员列表
     */
    @GetMapping("/{id}/members/search")
    public Result<PageResult<UserVO>> searchProjectMembers(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("搜索项目成员，id={}，keyword={}，page={}，pageSize={}", id, keyword, page, pageSize);
        
        PageResult<UserVO> members = projectService.searchProjectMembers(id, keyword, page, pageSize);
        return Result.success(members);
    }

    /**
     * 添加项目成员
     * @param id 项目ID
     * @param username 用户名
     * @return 操作结果
     */
    @PostMapping("/{id}/members")
    public Result<Void> addProjectMember(
            @PathVariable Long id, 
            @RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        log.info("添加项目成员，id={}, username={}", id, username);
        
        projectService.addProjectMember(id, username);
        return Result.success();
    }

    /**
     * 移除项目成员
     * @param id 项目ID
     * @param username 用户名
     * @return 操作结果
     */
    @DeleteMapping("/{id}/members/{username}")
    public Result<Void> removeProjectMember(@PathVariable Long id, @PathVariable String username) {
        log.info("移除项目成员，id={}, username={}", id, username);
        
        projectService.removeProjectMember(id, username);
        return Result.success();
    }

    /**
     * 获取项目附件列表
     * @param projectId 项目ID
     * @return 附件列表
     */
    @GetMapping("/{projectId}/attachments")
    public Result<List<ProjectAttachmentDTO>> getProjectAttachments(@PathVariable Long projectId) {
        log.info("获取项目附件列表: projectId={}", projectId);
        
        // 调用service获取附件列表
        List<ProjectAttachmentDTO> attachments = projectService.getProjectAttachments(projectId);
        return Result.success(attachments);
    }

    /**
     * 获取项目下的任务列表
     * @param projectId 项目ID
     * @return 任务列表
     */
    @GetMapping("/{projectId}/tasks")
    public Result<List<TaskVO>> getProjectTasks(@PathVariable Long projectId) {
        log.info("获取项目任务列表，projectId={}", projectId);
        
        List<TaskVO> taskVOList = taskService.getTaskVOListByProjectId(projectId);
        return Result.success(taskVOList);
    }
    
    /**
     * 上传项目附件
     * @param projectId 项目ID
     * @param file 文件
     * @return 文件URL
     */
    @PostMapping("/{projectId}/attachments")
    public Result<String> uploadProjectAttachment(
            @PathVariable Long projectId, 
            @RequestParam("file") MultipartFile file) {
        log.info("上传项目附件，projectId={}, fileName={}", projectId, file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                userId = 1L; // 默认用户ID
            }
            
            // 调用service上传文件
            String fileUrl = projectService.uploadProjectAttachment(projectId, file, userId);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("项目附件上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传项目附件
     * @param projectId 项目ID
     * @param files 文件列表
     * @return 文件URL列表
     */
    @PostMapping("/{projectId}/attachments/batch")
    public Result<List<String>> batchUploadProjectAttachments(
            @PathVariable Long projectId, 
            @RequestParam("files") List<MultipartFile> files) {
        log.info("批量上传项目附件，projectId={}, fileCount={}", projectId, files.size());
        
        if (files.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                userId = 1L; // 默认用户ID
            }
            
            // 调用service批量上传文件
            List<String> fileUrls = projectService.batchUploadProjectAttachments(projectId, files, userId);
            return Result.success(fileUrls);
        } catch (Exception e) {
            log.error("批量上传项目附件失败", e);
            return Result.error("批量上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除项目附件
     * @param projectId 项目ID
     * @param attachmentId 附件ID
     * @return 操作结果
     */
    @DeleteMapping("/{projectId}/attachments/{attachmentId}")
    public Result<Void> deleteProjectAttachment(
            @PathVariable Long projectId, 
            @PathVariable Long attachmentId) {
        log.info("删除项目附件，projectId={}, attachmentId={}", projectId, attachmentId);
        
        // Todo: 实现附件删除逻辑
        return Result.success();
    }

    /**
     * 获取所有项目（管理员专用）
     * @param keyword 关键字搜索
     * @param status 项目状态
     * @param page 页码
     * @param pageSize 每页大小
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dueStartTime 任务开始时间
     * @param dueEndTime 任务结束时间
     * @return 所有项目列表
     */
    @GetMapping("/all")
    public Result<PageResult<ProjectVO>> getAllProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) LocalDate startTime,
            @RequestParam(required = false) LocalDate endTime,
            @RequestParam(required = false) LocalDate dueStartTime,
            @RequestParam(required = false) LocalDate dueEndTime) {
        log.info("管理员获取所有项目列表，keyword={}, status={}, page={}, pageSize={}, startTime={}, endTime={}, dueStartTime={}, dueEndTime={}", 
                keyword, status, page, pageSize, startTime, endTime, dueStartTime, dueEndTime);
        
        // 检查当前用户是否为管理员
        Long userId = BaseContext.getCurrentId();
        PageResult<ProjectVO> result = projectService.getAllProjects(userId, keyword, status, page, pageSize, startTime, endTime, dueStartTime, dueEndTime);
        return Result.success(result);
    }
} 