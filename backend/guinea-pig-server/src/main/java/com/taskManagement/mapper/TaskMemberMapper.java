package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.TaskMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务成员关系Mapper接口
 */
@Mapper
public interface TaskMemberMapper extends BaseMapper<TaskMember> {

    /**
     * 根据用户ID查询关联的任务ID列表
     * @param userId 用户ID
     * @return 任务ID列表
     */
    @Select("SELECT task_id FROM tb_task_member WHERE user_id = #{userId}")
    List<Long> getTaskIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据任务ID查询关联的用户ID列表
     * @param taskId 任务ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM tb_task_member WHERE task_id = #{taskId}")
    List<Long> getUserIdsByTaskId(@Param("taskId") Long taskId);
} 