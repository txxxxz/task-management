package com.taskManagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskManagement.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据任务ID查询评论
     * @param taskId 任务ID
     * @return 评论列表
     */
    @Select("SELECT id, task_id, content, parent_id, create_time, create_user FROM tb_comment WHERE task_id = #{taskId}")
    List<Comment> selectByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根据任务ID查询顶级评论（没有父评论的评论）
     * @param taskId 任务ID
     * @return 评论列表
     */
    @Select("SELECT * FROM tb_comment WHERE task_id = #{taskId} AND parent_id IS NULL ORDER BY create_time DESC")
    List<Comment> selectTopLevelByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根据父评论ID查询子评论
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    @Select("SELECT id, task_id, content, parent_id, create_time, create_user FROM tb_comment WHERE parent_id = #{parentId}")
    List<Comment> selectChildrenByParentId(@Param("parentId") Long parentId);
    
    /**
     * 删除评论及其子评论
     * @param commentId 评论ID
     */
    @Delete("DELETE FROM tb_comment WHERE id = #{commentId} OR parent_id = #{commentId}")
    void deleteWithChildren(@Param("commentId") Long commentId);
    
    /**
     * 检查评论是否属于任务
     * @param commentId 评论ID
     * @param taskId 任务ID
     * @return 计数结果
     */
    @Select("SELECT COUNT(*) FROM tb_comment WHERE id = #{commentId} AND task_id = #{taskId}")
    int checkCommentBelongsToTask(@Param("commentId") Long commentId, @Param("taskId") Long taskId);

    /**
     * 查询评论详情不包含update_time和update_user字段
     * @param commentId 评论ID
     * @return 评论对象
     */
    @Select("SELECT id, task_id, content, parent_id, create_time, create_user FROM tb_comment WHERE id = #{commentId}")
    Comment selectCommentWithoutUpdateFields(@Param("commentId") Long commentId);
} 