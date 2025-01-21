package com.taskmanagement.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务视图对象
 */
@Data
public class TaskVO {
    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 所属项目信息
     */
    private ProjectVO project;
    
    /**
     * 任务状态：0-待处理，1-进行中，2-已完成，3-已取消
     */
    private Integer status;
    
    /**
     * 优先级：1-低，2-中，3-高，4-紧急
     */
    private Integer priority;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 实际完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * 预计工时（小时）
     */
    private Double estimatedHours;
    
    /**
     * 实际工时（小时）
     */
    private Double actualHours;
    
    /**
     * 评论数量
     */
    private Integer commentCount;
    
    /**
     * 附件数量
     */
    private Integer attachmentCount;
    
    /**
     * 标签列表
     */
    private List<TagVO> tags;
    
    /**
     * 负责人信息
     */
    private UserVO assignee;
    
    /**
     * 参与者列表
     */
    private List<UserVO> participants;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建者信息
     */
    private UserVO creator;
} 