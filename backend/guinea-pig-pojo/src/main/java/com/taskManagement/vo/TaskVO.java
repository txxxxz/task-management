package com.taskManagement.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;
    
    private Double estimatedHours;
    private Double actualHours;
    private Integer commentCount;
    
    private UserVO creator;
    private List<UserVO> members;
    private List<TagVO> tags;
    private List<CommentVO> comments;
    private List<AttachmentVO> attachments;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 获取附件数量
     * @return 附件数量
     */
    public Integer getAttachmentCount() {
        return attachments != null ? attachments.size() : 0;
    }
} 