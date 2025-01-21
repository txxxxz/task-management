package com.taskmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务成员实体类
 * 记录任务的参与者和他们的角色
 * 
 * @author taskmanagement
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_member")
public class TaskMember extends BaseEntity {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色：0-参与者，1-负责人
     */
    private Integer role;
    
    /**
     * 成员状态：0-待接受，1-已接受，2-已拒绝
     */
    private Integer status;
} 