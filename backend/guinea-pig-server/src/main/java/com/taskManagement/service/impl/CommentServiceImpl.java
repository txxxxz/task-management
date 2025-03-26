package com.taskManagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskManagement.dto.CommentDTO;
import com.taskManagement.entity.Comment;
import com.taskManagement.entity.Task;
import com.taskManagement.entity.User;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.mapper.CommentMapper;
import com.taskManagement.mapper.TaskMapper;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.service.CommentService;
import com.taskManagement.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据任务ID获取评论列表，并构建评论树结构
     * @param taskId 任务ID
     * @return 评论列表
     */
    @Override
    public List<CommentDTO> getCommentsByTaskId(Long taskId) {
        log.info("获取任务评论列表，taskId: {}", taskId);
        
        // 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 获取所有顶级评论
        List<Comment> topLevelComments = commentMapper.selectTopLevelByTaskId(taskId);
        
        // 构建评论树
        List<CommentDTO> result = new ArrayList<>();
        for (Comment comment : topLevelComments) {
            CommentDTO commentDTO = convertToDTO(comment);
            // 递归获取子评论
            commentDTO.setChildren(getChildComments(comment.getId()));
            result.add(commentDTO);
        }
        
        return result;
    }
    
    /**
     * 递归获取子评论
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    private List<CommentDTO> getChildComments(Long parentId) {
        List<Comment> childComments = commentMapper.selectChildrenByParentId(parentId);
        List<CommentDTO> result = new ArrayList<>();
        
        for (Comment comment : childComments) {
            CommentDTO commentDTO = convertToDTO(comment);
            // 递归获取下一级子评论
            commentDTO.setChildren(getChildComments(comment.getId()));
            result.add(commentDTO);
        }
        
        return result;
    }
    
    /**
     * 将评论实体转换为DTO
     * @param comment 评论实体
     * @return 评论DTO
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        BeanUtils.copyProperties(comment, commentDTO);
        
        // 设置创建用户信息
        if (comment.getCreateUser() != null) {
            User user = userMapper.selectById(comment.getCreateUser());
            if (user != null) {
                commentDTO.setCreateUserName(user.getUsername());
                commentDTO.setCreateUserAvatar(user.getAvatar());
            }
        }
        
        return commentDTO;
    }

    /**
     * 创建评论
     * @param commentDTO 评论信息
     * @return 创建后的评论
     */
    @Override
    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO) {
        log.info("创建评论: {}", commentDTO);
        
        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查任务是否存在
        Task task = taskMapper.selectById(commentDTO.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 如果有父评论ID，检查父评论是否存在且属于同一任务
        if (commentDTO.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(commentDTO.getParentId());
            if (parentComment == null) {
                throw new BusinessException("父评论不存在");
            }
            
            if (!parentComment.getTaskId().equals(commentDTO.getTaskId())) {
                throw new BusinessException("不能跨任务评论");
            }
        }
        
        // 创建评论实体并设置属性
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setTaskId(commentDTO.getTaskId());
        comment.setParentId(commentDTO.getParentId());
        comment.setCreateUser(userId);
        comment.setCreateTime(LocalDateTime.now());
        
        // 保存评论
        commentMapper.insert(comment);
        
        // 更新任务评论数量
        task.setCommentCount(task.getCommentCount() + 1);
        taskMapper.updateById(task);
        
        // 转换为DTO并返回
        return convertToDTO(comment);
    }

    /**
     * 删除评论
     * @param taskId 任务ID
     * @param commentId 评论ID
     */
    @Override
    @Transactional
    public void deleteComment(Long taskId, Long commentId) {
        log.info("删除评论，taskId: {}, commentId: {}", taskId, commentId);
        
        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 检查评论是否存在且属于该任务
        int count = commentMapper.checkCommentBelongsToTask(commentId, taskId);
        if (count == 0) {
            throw new BusinessException("评论不存在或不属于该任务");
        }
        
        // 获取评论
        Comment comment = commentMapper.selectById(commentId);
        
        // 检查用户是否有权限删除（只有评论创建者可以删除）
        if (!comment.getCreateUser().equals(userId)) {
            throw new BusinessException("无权删除该评论");
        }
        
        // 获取将被删除的评论数量（包括子评论）
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        getChildCommentsRecursively(commentId, comments);
        int deleteCount = comments.size();
        
        // 删除评论及其所有子评论
        commentMapper.deleteWithChildren(commentId);
        
        // 更新任务评论数量
        task.setCommentCount(Math.max(0, task.getCommentCount() - deleteCount));
        taskMapper.updateById(task);
    }
    
    /**
     * 递归获取所有子评论
     * @param parentId 父评论ID
     * @param comments 评论集合
     */
    private void getChildCommentsRecursively(Long parentId, List<Comment> comments) {
        List<Comment> childComments = commentMapper.selectChildrenByParentId(parentId);
        if (!childComments.isEmpty()) {
            for (Comment childComment : childComments) {
                comments.add(childComment);
                getChildCommentsRecursively(childComment.getId(), comments);
            }
        }
    }
}