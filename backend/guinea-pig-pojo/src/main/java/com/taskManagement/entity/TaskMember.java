package com.taskManagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_member")
public class TaskMember extends BaseEntity {
    private Long taskId;
    private Long userId;
    private Integer role;    // 0-参与者，1-负责人
    private Integer status;  // 0-待接受，1-已接受，2-已拒绝
} 