package com.taskManagement.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private String content;
    private Long taskId;
    private Long parentId;
    private Integer type;
    private Integer hasAttachment;
    
    private UserVO creator;
    private List<AttachmentVO> attachments;
    private List<CommentVO> replies;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 