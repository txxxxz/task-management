package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务Mapper接口
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    
} 