package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.TaskAttachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务附件Mapper接口
 */
@Mapper
public interface TaskAttachmentMapper extends BaseMapper<TaskAttachment> {
} 