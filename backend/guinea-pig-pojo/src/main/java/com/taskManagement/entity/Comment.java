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
} 