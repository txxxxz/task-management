package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目成员关系Mapper
 */
@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
} 