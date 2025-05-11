package com.taskManagement.controller;

import com.taskManagement.dto.CommentDTO;
import com.taskManagement.result.Result;
import com.taskManagement.service.CommentService;
import com.taskManagement.vo.CommentVO;
import com.taskManagement.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@Slf4j
@Api(tags = "评论相关接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 根据评论ID获取评论详情
     * @param commentId 评论ID
     * @return 评论详情
     */
    @GetMapping("/{commentId}")
    @ApiOperation("根据评论ID获取评论详情")
    public Result<CommentVO> getCommentById(@PathVariable Long commentId) {
        log.info("根据评论ID获取评论详情: commentId={}", commentId);
        
        // 调用service获取评论详情
        CommentDTO commentDTO = commentService.getCommentById(commentId);
        
        // 将DTO转换为VO
        CommentVO commentVO = convertToCommentVO(commentDTO);
        
        return Result.success(commentVO);
    }
    
    /**
     * 将单个CommentDTO转换为CommentVO
     * @param commentDTO CommentDTO对象
     * @return CommentVO对象
     */
    private CommentVO convertToCommentVO(CommentDTO commentDTO) {
        if (commentDTO == null) {
            return null;
        }
        
        CommentVO commentVO = new CommentVO();
        commentVO.setId(commentDTO.getId());
        commentVO.setContent(commentDTO.getContent());
        commentVO.setTaskId(commentDTO.getTaskId());
        commentVO.setParentId(commentDTO.getParentId());
        commentVO.setCreateTime(commentDTO.getCreateTime());
        
        // 设置创建用户信息
        UserVO creatorVO = new UserVO();
        creatorVO.setId(commentDTO.getCreateUser());
        creatorVO.setUsername(commentDTO.getCreateUserName());
        creatorVO.setAvatar(commentDTO.getCreateUserAvatar());
        commentVO.setCreator(creatorVO);
        
        return commentVO;
    }
} 