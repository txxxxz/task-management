package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签Mapper接口
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    
    /**
     * 根据项目ID查询标签列表
     * 注意：Tag与Project没有直接关系，通过Task间接关联
     * @param projectId 项目ID
     * @return 标签列表
     */
    @Select("SELECT DISTINCT t.* FROM tb_task_tag t " +
            "JOIN tb_task_tag_rel r ON t.id = r.tag_id " +
            "JOIN tb_task task ON r.task_id = task.id " +
            "WHERE task.project_id = #{projectId}")
    List<Tag> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 根据关键词和项目ID查询标签列表
     * 注意：Tag与Project没有直接关系，通过Task间接关联
     * @param keyword 关键词
     * @param projectId 项目ID
     * @return 标签列表
     */
    @Select("SELECT DISTINCT t.* FROM tb_task_tag t " +
            "JOIN tb_task_tag_rel r ON t.id = r.tag_id " +
            "JOIN tb_task task ON r.task_id = task.id " +
            "WHERE t.name LIKE CONCAT('%', #{keyword}, '%') " +
            "AND task.project_id = #{projectId}")
    List<Tag> selectByKeywordAndProjectId(@Param("keyword") String keyword, @Param("projectId") Long projectId);
    
    /**
     * 根据关键词查询标签列表
     * @param keyword 关键词
     * @return 标签列表
     */
    @Select("SELECT * FROM tb_task_tag WHERE name LIKE CONCAT('%', #{keyword}, '%')")
    List<Tag> selectByKeyword(@Param("keyword") String keyword);
    
    /**
     * 根据任务ID查询标签列表
     * @param taskId 任务ID
     * @return 标签列表
     */
    @Select("SELECT t.* FROM tb_task_tag t " +
            "JOIN tb_task_tag_rel r ON t.id = r.tag_id " +
            "WHERE r.task_id = #{taskId}")
    List<Tag> selectByTaskId(@Param("taskId") Long taskId);
} 