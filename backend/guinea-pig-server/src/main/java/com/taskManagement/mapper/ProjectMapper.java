package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目Mapper
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
} 