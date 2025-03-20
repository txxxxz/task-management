package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.TaskTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务标签关联Mapper接口
 */
@Mapper
public interface TaskTagRelMapper extends BaseMapper<TaskTag> {
    
    /**
     * 根据任务ID查询标签关联
     * @param taskId 任务ID
     * @return 标签关联列表
     */
    @Select("SELECT * FROM tb_task_tag_rel WHERE task_id = #{taskId}")
    List<TaskTag> selectByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根据标签ID查询任务关联
     * @param tagId 标签ID
     * @return 任务关联列表
     */
    @Select("SELECT * FROM tb_task_tag_rel WHERE tag_id = #{tagId}")
    List<TaskTag> selectByTagId(@Param("tagId") Long tagId);
    
    /**
     * 删除任务的所有标签关联
     * @param taskId 任务ID
     * @return 影响行数
     */
    @Delete("DELETE FROM tb_task_tag_rel WHERE task_id = #{taskId}")
    int deleteByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 删除标签的所有任务关联
     * @param tagId 标签ID
     * @return 影响行数
     */
    @Delete("DELETE FROM tb_task_tag_rel WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);
    
    /**
     * 批量插入任务标签关联
     * @param taskId 任务ID
     * @param tagIds 标签ID列表
     * @return 影响行数
     */
    default int batchInsert(Long taskId, List<Long> tagIds) {
        int count = 0;
        for (Long tagId : tagIds) {
            TaskTag taskTag = new TaskTag();
            taskTag.setTaskId(taskId);
            taskTag.setTagId(tagId);
            count += insert(taskTag);
        }
        return count;
    }
} 