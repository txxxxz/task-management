package com.taskManagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskManagement.context.BaseContext;
import com.taskManagement.dto.ProjectDTO;
import com.taskManagement.entity.Project;
import com.taskManagement.entity.ProjectMember;
import com.taskManagement.entity.User;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.mapper.ProjectMapper;
import com.taskManagement.mapper.ProjectMemberMapper;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.service.ProjectService;
import com.taskManagement.vo.PageResult;
import com.taskManagement.vo.ProjectVO;
import com.taskManagement.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskManagement.entity.Project;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目管理服务实现
 */
@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;
    
    @Autowired
    public ProjectServiceImpl(ProjectMapper projectMapper, ProjectMemberMapper projectMemberMapper, UserMapper userMapper) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.userMapper = userMapper;
    }

    /**
     * 获取项目列表
     */
    @Override
    public PageResult<ProjectVO> getProjectList(String keyword, Integer status, Integer page, Integer pageSize) {
        log.info("查询项目列表参数: keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索 - 同时匹配项目名称或描述
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(Project::getName, keyword)
                    .or()
                    .like(Project::getDescription, keyword));
        }
        
        // 状态过滤
        if (status != null) {
            queryWrapper.eq(Project::getStatus, status);
        }
        
        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            log.warn("获取当前用户ID为空，使用默认值1");
            userId = 1L;
        }
        
        // 查询当前用户参与的项目ID
        LambdaQueryWrapper<ProjectMember> memberQueryWrapper = new LambdaQueryWrapper<>();
        memberQueryWrapper.eq(ProjectMember::getUserId, userId);
        List<ProjectMember> projectMembers = projectMemberMapper.selectList(memberQueryWrapper);
        List<Long> projectIds = projectMembers.stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toList());
        
        // 如果用户没有参与任何项目，返回空列表
        if (projectIds.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        
        // 只查询用户参与的项目
        queryWrapper.in(Project::getId, projectIds);
        
        // 按优先级和创建时间排序（优先级高的在前，同优先级按创建时间降序）
        queryWrapper.orderByDesc(Project::getPriority)
                   .orderByDesc(Project::getCreateTime);
        
        // 分页参数处理 - MyBatis-Plus页码从0开始，前端从1开始，需要减1
        int pageIndex = Math.max(0, page - 1);
        Page<Project> pageParam = new Page<>(pageIndex, pageSize);
        
        // 执行分页查询
        Page<Project> pageResult = projectMapper.selectPage(pageParam, queryWrapper);
        
        // 转换为VO列表并填充关联数据
        List<ProjectVO> projectVOList = new ArrayList<>();
        for (Project project : pageResult.getRecords()) {
            ProjectVO vo = convertToVO(project);
            
            // 获取项目成员数量
            LambdaQueryWrapper<ProjectMember> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(ProjectMember::getProjectId, project.getId());
            long memberCount = projectMemberMapper.selectCount(countWrapper);
            vo.setMemberCount((int)memberCount);
            
            // 设置当前用户是否是创建者
            vo.setIsCreator(userId.equals(project.getCreateUser()));
            
            projectVOList.add(vo);
        }
        
        // 返回分页结果
        return new PageResult<>(projectVOList, pageResult.getTotal());
    }

    /**
     * 获取项目详情
     */
    @Override
    public ProjectVO getProjectDetail(Long id) {
        // 查询项目信息
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 转换为VO并返回
        return convertToVO(project);
    }

    /**
     * 创建项目
     */
    @Override
    @Transactional
    public ProjectVO createProject(ProjectDTO projectDTO) {
        // 创建项目实体
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        
        // 处理日期转换
        if (projectDTO.getStartTime() != null) {
            // 将LocalDate转换为LocalDateTime
            LocalDate startDate = projectDTO.getStartTime();
            project.setStartTime(startDate.atStartOfDay());
        }
        if (projectDTO.getEndTime() != null) {
            // 将LocalDate转换为LocalDateTime
            LocalDate endDate = projectDTO.getEndTime();
            project.setEndTime(endDate.atStartOfDay());
        }
        
        // 设置创建者信息
        Long userId = BaseContext.getCurrentId();
        // 如果无法获取当前用户ID，则使用默认值1
        if (userId == null) {
            log.warn("无法获取当前用户ID，使用默认值1");
            userId = 1L;
        }
        project.setCreateUser(userId);
        project.setUpdateUser(userId);
        
        // 如果状态为空，默认为筹备中
        if (project.getStatus() == null) {
            project.setStatus(0); // 0-筹备中
        }
        
        // 如果优先级为空，默认为中
        if (project.getPriority() == null) {
            project.setPriority(2); // 2-中
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        project.setCreateTime(now);
        project.setUpdateTime(now);
        
        // 保存项目
        projectMapper.insert(project);
        
        // 添加创建者为项目成员
        addProjectMember(project.getId(), userId);
        
        // 如果有指定成员，添加成员
        if (projectDTO.getMembers() != null && !projectDTO.getMembers().isEmpty()) {
            for (String username : projectDTO.getMembers()) {
                if (StringUtils.isNotBlank(username)) {
                    try {
                        addProjectMember(project.getId(), username);
                    } catch (Exception e) {
                        log.error("添加项目成员失败: {}", username, e);
                    }
                }
            }
        }
        
        // 返回项目VO
        return convertToVO(project);
    }

    /**
     * 更新项目
     */
    @Override
    @Transactional
    public ProjectVO updateProject(ProjectDTO projectDTO) {
        // 查询项目是否存在
        Long id = projectDTO.getId();
        Project existingProject = projectMapper.selectById(id);
        if (existingProject == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 更新项目信息
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        
        // 处理日期转换
        if (projectDTO.getStartTime() != null) {
            // 将LocalDate转换为LocalDateTime
            LocalDate startDate = projectDTO.getStartTime();
            project.setStartTime(startDate.atStartOfDay());
        }
        if (projectDTO.getEndTime() != null) {
            // 将LocalDate转换为LocalDateTime
            LocalDate endDate = projectDTO.getEndTime();
            project.setEndTime(endDate.atStartOfDay());
        }
        
        // 设置更新者
        Long userId = BaseContext.getCurrentId();
        project.setUpdateUser(userId);
        project.setUpdateTime(LocalDateTime.now());
        
        // 保存更新
        projectMapper.updateById(project);
        
        // 如果有指定成员，更新成员
        if (projectDTO.getMembers() != null && !projectDTO.getMembers().isEmpty()) {
            // 获取当前成员
            List<String> currentMembers = getProjectMembers(id);
            
            // 计算需要添加的成员
            List<String> membersToAdd = projectDTO.getMembers().stream()
                    .filter(username -> !currentMembers.contains(username))
                    .collect(Collectors.toList());
            
            // 计算需要移除的成员
            List<String> membersToRemove = currentMembers.stream()
                    .filter(username -> !projectDTO.getMembers().contains(username))
                    .collect(Collectors.toList());
            
            // 添加新成员
            for (String username : membersToAdd) {
                if (StringUtils.isNotBlank(username)) {
                    try {
                        addProjectMember(id, username);
                    } catch (Exception e) {
                        log.error("添加项目成员失败: {}", username, e);
                    }
                }
            }
            
            // 移除旧成员
            for (String username : membersToRemove) {
                if (StringUtils.isNotBlank(username)) {
                    try {
                        removeProjectMember(id, username);
                    } catch (Exception e) {
                        log.error("移除项目成员失败: {}", username, e);
                    }
                }
            }
        }
        
        // 查询更新后的项目
        Project updatedProject = projectMapper.selectById(id);
        
        // 返回项目VO
        return convertToVO(updatedProject);
    }

    /**
     * 删除项目
     */
    @Override
    @Transactional
    public void deleteProject(Long id) {
        // 查询项目是否存在
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 删除项目成员关系
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, id);
        projectMemberMapper.delete(queryWrapper);
        
        // 删除项目
        projectMapper.deleteById(id);
    }

    /**
     * 获取项目成员
     */
    @Override
    public List<String> getProjectMembers(Long id) {
        // 查询项目成员关系
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, id);
        List<ProjectMember> projectMembers = projectMemberMapper.selectList(queryWrapper);
        
        // 提取成员用户ID
        List<Long> userIds = projectMembers.stream()
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询用户信息
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(User::getId, userIds);
        List<User> users = userMapper.selectList(userQueryWrapper);
        
        // 提取用户名
        return users.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

    /**
     * 搜索项目成员
     */
    @Override
    public PageResult<UserVO> searchProjectMembers(Long id, String keyword, Integer page, Integer pageSize) {
        log.info("搜索项目成员，id={}，keyword={}，page={}，pageSize={}", id, keyword, page, pageSize);
        
        // 1. 检查项目是否存在
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        // 2. 查询项目成员关系
        LambdaQueryWrapper<ProjectMember> memberQueryWrapper = new LambdaQueryWrapper<>();
        memberQueryWrapper.eq(ProjectMember::getProjectId, id);
        List<ProjectMember> projectMembers = projectMemberMapper.selectList(memberQueryWrapper);
        
        if (projectMembers.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0);
        }
        
        // 3. 提取成员用户ID
        List<Long> userIds = projectMembers.stream()
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());
        
        // 4. 构建用户查询条件
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(User::getId, userIds);
        
        // 5. 添加关键字搜索条件
        if (StringUtils.isNotBlank(keyword)) {
            userQueryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        
        // 6. 计算分页参数
        int pageIndex = Math.max(0, page - 1);
        Page<User> pageParam = new Page<>(pageIndex, pageSize);
        
        // 7. 执行分页查询
        Page<User> userPage = userMapper.selectPage(pageParam, userQueryWrapper);
        
        // 8. 转换为VO对象
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(user -> {
                    UserVO vo = new UserVO();
                    vo.setId(user.getId());
                    vo.setUsername(user.getUsername());
                    vo.setEmail(user.getEmail());
                    vo.setAvatar(user.getAvatar());
                    vo.setStatus(user.getStatus());
                    vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null);
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 9. 返回分页结果
        return new PageResult<>(userVOList, userPage.getTotal());
    }

    /**
     * 添加项目成员
     */
    @Override
    @Transactional
    public void addProjectMember(Long id, String username) {
        // 查询用户信息
        User user = getUserByUsername(username);
        
        // 添加项目成员
        addProjectMember(id, user.getId());
    }
    
    /**
     * 根据用户ID添加项目成员
     */
    private void addProjectMember(Long projectId, Long userId) {
        // 如果userId为null，则不添加成员
        if (userId == null) {
            log.warn("尝试添加项目成员时userId为null，projectId={}", projectId);
            return;
        }
        
        // 检查是否已经是成员
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId);
        
        if (projectMemberMapper.selectCount(queryWrapper) > 0) {
            // 已经是成员，不需要添加
            return;
        }
        
        // 添加成员关系
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(projectId);
        projectMember.setUserId(userId);
        projectMember.setCreateTime(LocalDateTime.now());
        
        projectMemberMapper.insert(projectMember);
    }

    /**
     * 移除项目成员
     */
    @Override
    @Transactional
    public void removeProjectMember(Long id, String username) {
        // 查询用户信息
        User user = getUserByUsername(username);
        
        // 查询项目成员关系
        LambdaQueryWrapper<ProjectMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMember::getProjectId, id)
                .eq(ProjectMember::getUserId, user.getId());
        
        // 删除成员关系
        projectMemberMapper.delete(queryWrapper);
    }
    
    /**
     * 根据用户名查询用户
     */
    private User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("用户不存在: " + username);
        }
        
        return user;
    }
    
    /**
     * 将项目实体转换为VO
     */
    private ProjectVO convertToVO(Project project) {
        if (project == null) {
            return null;
        }
        
        // 创建VO对象
        ProjectVO projectVO = new ProjectVO();
        BeanUtils.copyProperties(project, projectVO);
        
        // 获取创建者信息
        User creator = userMapper.selectById(project.getCreateUser());
        if (creator != null) {
            UserVO creatorVO = new UserVO();
            BeanUtils.copyProperties(creator, creatorVO);
            projectVO.setCreator(creatorVO);
        }
        
        // 判断当前用户是否为创建者
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null && project.getCreateUser() != null && 
            currentUserId.equals(project.getCreateUser())) {
            projectVO.setIsCreator(true);
        } else {
            projectVO.setIsCreator(false);
        }
        
        // 获取项目成员
        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getProjectId, project.getId());
        List<ProjectMember> projectMembers = projectMemberMapper.selectList(memberWrapper);
        
        // 设置成员数量
        projectVO.setMemberCount(projectMembers.size());
        
        if (projectMembers != null && !projectMembers.isEmpty()) {
            List<Long> memberIds = projectMembers.stream()
                    .map(ProjectMember::getUserId)
                    .collect(Collectors.toList());
            
            // 查询用户信息
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(User::getId, memberIds);
            List<User> users = userMapper.selectList(userWrapper);
            
            // 提取用户名列表
            List<String> memberUsernames = users.stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList());
            
            projectVO.setMembers(memberUsernames);
        } else {
            // 没有成员时设置空列表和0
            projectVO.setMembers(new ArrayList<>());
            projectVO.setMemberCount(0);
        }
        
        return projectVO;
    }
} 