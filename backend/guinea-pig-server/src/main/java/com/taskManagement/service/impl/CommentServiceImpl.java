package com.taskManagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskManagement.dto.CommentDTO;
import com.taskManagement.entity.Comment;
import com.taskManagement.entity.Task;
import com.taskManagement.entity.User;
import com.taskManagement.entity.TaskMember;
import com.taskManagement.exception.BusinessException;
import com.taskManagement.mapper.CommentMapper;
import com.taskManagement.mapper.TaskMapper;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.mapper.TaskMemberMapper;
import com.taskManagement.service.CommentService;
import com.taskManagement.service.NotificationService;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private TaskMemberMapper taskMemberMapper;

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
        
        // 直接获取所有评论并返回扁平列表，前端负责构建树结构
        List<Comment> comments = commentMapper.selectByTaskId(taskId);
        List<CommentDTO> result = comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 打印前5条评论数据查看格式
        if (!result.isEmpty()) {
            int count = Math.min(result.size(), 2);
            for (int i = 0; i < count; i++) {
                try {
                    CommentDTO dto = result.get(i);
                    log.info("评论数据示例[{}]: id={}, createUserName={}, createUserAvatar={}",
                            i, dto.getId(), dto.getCreateUserName(), dto.getCreateUserAvatar());
                } catch (Exception e) {
                    log.error("打印评论数据异常", e);
                }
            }
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
                log.debug("评论ID:{} 设置用户信息 - 用户:{}, 头像:{}", 
                         comment.getId(), user.getUsername(), user.getAvatar());
            } else {
                log.warn("评论ID:{} 找不到用户ID:{}", comment.getId(), comment.getCreateUser());
                commentDTO.setCreateUserName("用户" + comment.getCreateUser());
                commentDTO.setCreateUserAvatar("");
            }
        } else {
            commentDTO.setCreateUserName("未知用户");
            commentDTO.setCreateUserAvatar("");
        }
        
        // 确保children字段初始化
        if (commentDTO.getChildren() == null) {
            commentDTO.setChildren(new ArrayList<>());
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
        
        // 1. 检查任务是否存在
        Task task = taskMapper.selectById(commentDTO.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 2. 创建评论实体
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);
        
        // 3. 补全创建信息
        Long currentUserId = BaseContext.getCurrentId();
        comment.setCreateUser(currentUserId);
        comment.setCreateTime(LocalDateTime.now());
        
        // 4. 保存评论
        commentMapper.insert(comment);
        
        // 5. 更新任务评论数
        task.setCommentCount(task.getCommentCount() + 1);
        taskMapper.updateById(task);
        
        // 6. 处理评论中的@用户通知
        processCommentMentions(comment);
        
        // 7. 通知任务所有成员有新评论
        notifyTaskMembers(comment, task);
        
        // 8. 转换为DTO并返回
        CommentDTO resultDTO = convertToDTO(comment);
        log.info("返回评论DTO: {}", resultDTO);
        return resultDTO;
    }

    /**
     * 处理评论中的@用户并发送通知
     * @param comment 评论
     */
    private void processCommentMentions(Comment comment) {
        // 从评论内容中提取@的用户名
        List<String> mentionedUsernames = notificationService.extractMentionedUsers(comment.getContent());
        if (mentionedUsernames.isEmpty()) {
            return;
        }
        
        // 获取当前用户信息
        Long currentUserId = comment.getCreateUser();
        User currentUser = userMapper.selectById(currentUserId);
        String currentUsername = currentUser != null ? currentUser.getUsername() : "User" + currentUserId;
        
        // 获取任务信息
        Task task = taskMapper.selectById(comment.getTaskId());
        String taskName = task != null ? task.getName() : "Unknown Task";
        
        // 查找被@用户的ID并发送通知
        for (String username : mentionedUsernames) {
            User user = userMapper.getByUsername(username);
            if (user != null && !user.getId().equals(currentUserId)) { // 不通知自己
                String content = currentUsername + " mentioned you in task [" + taskName + "]";
                notificationService.createCommentMentionNotification(comment.getId(), content, user.getId());
                log.info("Comment mention notification sent to user {}", username);
            }
        }
    }

    /**
     * 通知任务所有成员有新评论
     * @param comment 评论
     * @param task 任务
     */
    private void notifyTaskMembers(Comment comment, Task task) {
        // 获取当前用户信息
        Long currentUserId = comment.getCreateUser();
        User currentUser = userMapper.selectById(currentUserId);
        String currentUsername = currentUser != null ? currentUser.getUsername() : "User" + currentUserId;
        
        // 获取任务名称
        String taskName = task.getName();
        
        // 获取任务所有成员，包括创建者
        List<Long> memberIds = new ArrayList<>();
        
        // 添加任务创建者
        if (task.getCreateUser() != null && !task.getCreateUser().equals(currentUserId)) {
            memberIds.add(task.getCreateUser());
        }
        
        // 查询任务成员
        List<TaskMember> members = taskMemberMapper.selectList(
                new LambdaQueryWrapper<TaskMember>()
                        .eq(TaskMember::getTaskId, task.getId()));
        
        // 添加任务成员
        for (TaskMember member : members) {
            if (!member.getUserId().equals(currentUserId) && !memberIds.contains(member.getUserId())) {
                memberIds.add(member.getUserId());
            }
        }
        
        // 发送通知给所有成员
        String content = currentUsername + " commented on task [" + taskName + "]";
        for (Long userId : memberIds) {
            notificationService.createCommentMentionNotification(comment.getId(), content, userId);
            log.info("已发送评论通知给任务成员: {}", userId);
        }
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
        
        // 获取评论 - 使用自定义查询方法避免使用update_time和update_user
        Comment comment = commentMapper.selectCommentWithoutUpdateFields(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 检查用户是否有权限删除（只有评论创建者或任务拥有者可以删除）
        if (!comment.getCreateUser().equals(userId) && !task.getCreateUser().equals(userId)) {
            throw new BusinessException("无权删除该评论");
        }
        
        // 获取将被删除的评论数量（包括子评论）
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        getChildCommentsRecursively(commentId, comments);
        int deleteCount = comments.size();
        
        // 删除评论及其所有子评论（使用优化的递归SQL）
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