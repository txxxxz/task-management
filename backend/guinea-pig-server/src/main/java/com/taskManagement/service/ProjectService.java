package com.taskManagement.service;

import com.taskManagement.dto.ProjectAttachmentDTO;
import com.taskManagement.dto.ProjectDTO;
import com.taskManagement.vo.PageResult;
import com.taskManagement.vo.ProjectVO;
import com.taskManagement.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    /**
     * 上传项目附件
     * @param projectId 项目ID
     * @param file 文件
     * @param userId 用户ID
     * @return 文件URL
     */
    String uploadProjectAttachment(Long projectId, MultipartFile file, Long userId);
    
    /**
     * 批量上传项目附件
     * @param projectId 项目ID
     * @param files 文件列表
     * @param userId 用户ID
     * @return 文件URL列表
     */
    List<String> batchUploadProjectAttachments(Long projectId, List<MultipartFile> files, Long userId);
    
    /**
     * 获取项目附件列表
     * @param projectId 项目ID
     * @return 附件列表
     */
    List<ProjectAttachmentDTO> getProjectAttachments(Long projectId);
} 