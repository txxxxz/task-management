package com.taskManagement.service;

import com.taskManagement.dto.CommentDTO;
import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 根据任务ID获取评论列表
     * @param taskId 任务ID
     * @return 评论列表
     */
    List<CommentDTO> getCommentsByTaskId(Long taskId);
    
    /**
     * 创建评论
     * @param commentDTO 评论信息
     * @return 创建后的评论
     */
    CommentDTO createComment(CommentDTO commentDTO);
    
    /**
     * 删除评论
     * @param taskId 任务ID
     * @param commentId 评论ID
     */
    void deleteComment(Long taskId, Long commentId);
    
    /**
     * 根据评论ID获取评论详情
     * @param commentId 评论ID
     * @return 评论详情
     */
    CommentDTO getCommentById(Long commentId);
}
