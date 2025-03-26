package com.taskManagement.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskVO {
    private Long id;
    private String name;
    private String description;
    private Long projectId;
    private Integer status;
    private Integer priority;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private LocalDateTime completedTime;
    private Double estimatedHours;
    private Double actualHours;
    private Integer commentCount;
    
    private UserVO creator;
    private List<UserVO> members;
    private List<TagVO> tags;
    private List<CommentVO> comments;
    private List<AttachmentVO> attachments;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 获取附件数量
     * @return 附件数量
     */
    public Integer getAttachmentCount() {
        return attachments != null ? attachments.size() : 0;
    }
} 