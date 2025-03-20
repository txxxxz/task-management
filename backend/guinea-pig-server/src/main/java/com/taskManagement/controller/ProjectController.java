package com.taskManagement.controller;

import com.taskManagement.dto.ProjectDTO;
import com.taskManagement.entity.Project;
import com.taskManagement.result.Result;
import com.taskManagement.service.ProjectService;
import com.taskManagement.vo.PageResult;
import com.taskManagement.vo.ProjectVO;
import com.taskManagement.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    /**
     * 获取项目列表
     * @param keyword 关键字搜索
     * @param status 项目状态
     * @param page 页码
     * @param pageSize 每页大小
     * @return 项目列表
     */
    @GetMapping
    public Result<PageResult<ProjectVO>> getProjectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取项目列表，keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);
        
        PageResult<ProjectVO> result = projectService.getProjectList(keyword, status, page, pageSize);
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
} 