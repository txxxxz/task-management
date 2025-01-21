package com.taskmanagement.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目视图对象
 */
@Data
public class ProjectVO {
    /**
     * 项目ID
     */
    private Long id;
    
    /**
     * 项目名称
     */
    private String name;
    
    /**
     * 项目描述
     */
    private String description;
    
    /**
     * 项目状态：0-筹备中，1-进行中，2-已完成，3-已归档
     */
    private Integer status;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 项目优先级：1-低，2-中，3-高，4-紧急
     */
    private Integer priority;
    
    /**
     * 任务总数
     */
    private Integer taskCount;
    
    /**
     * 已完成任务数
     */
    private Integer completedTaskCount;
    
    /**
     * 项目标签列表
     */
    private List<TagVO> tags;
    
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