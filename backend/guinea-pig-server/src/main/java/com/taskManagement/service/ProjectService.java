package com.taskManagement.service;

import com.taskManagement.dto.ProjectDTO;
import com.taskManagement.vo.PageResult;
import com.taskManagement.vo.ProjectVO;
import com.taskManagement.vo.UserVO;

import java.util.List;

/**
 * 项目管理服务接口
 */
public interface ProjectService {

    /**
     * 获取项目列表
     * @param keyword 关键字
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ProjectVO> getProjectList(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 获取项目详情
     * @param id 项目ID
     * @return 项目详情
     */
    ProjectVO getProjectDetail(Long id);

    /**
     * 创建项目
     * @param projectDTO 项目信息
     * @return 创建后的项目信息
     */
    ProjectVO createProject(ProjectDTO projectDTO);

    /**
     * 更新项目
     * @param projectDTO 项目信息
     * @return 更新后的项目信息
     */
    ProjectVO updateProject(ProjectDTO projectDTO);

    /**
     * 删除项目
     * @param id 项目ID
     */
    void deleteProject(Long id);

    /**
     * 获取项目成员
     * @param id 项目ID
     * @return 成员用户名列表
     */
    List<String> getProjectMembers(Long id);
    
    /**
     * 搜索项目成员
     * @param id 项目ID
     * @param keyword 搜索关键字
     * @param page 页码
     * @param pageSize 每页大小
     * @return 成员详细信息列表
     */
    PageResult<UserVO> searchProjectMembers(Long id, String keyword, Integer page, Integer pageSize);

    /**
     * 添加项目成员
     * @param id 项目ID
     * @param username 用户名
     */
    void addProjectMember(Long id, String username);

    /**
     * 移除项目成员
     * @param id 项目ID
     * @param username 用户名
     */
    void removeProjectMember(Long id, String username);
} 