package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_comment")
public class Comment extends BaseEntity {
    private String content;
    private Long taskId;
    private Long parentId;
    private Integer type;        // 0-普通评论，1-状态变更，2-任务更新
    private Integer hasAttachment; // 0-否，1-是
} 