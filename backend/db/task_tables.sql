-- 任务表
CREATE TABLE IF NOT EXISTS `tb_task` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(200) NOT NULL COMMENT '任务标题',
    `description` text COMMENT '任务描述',
    `project_id` bigint(20) NOT NULL COMMENT '所属项目ID',
    `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '任务状态：0-待处理，1-进行中，2-已完成，3-已取消',
    `priority` tinyint(4) NOT NULL DEFAULT 1 COMMENT '优先级：1-低，2-中，3-高，4-紧急',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `deadline` datetime DEFAULT NULL COMMENT '截止时间',
    `completed_time` datetime DEFAULT NULL COMMENT '实际完成时间',
    `estimated_hours` double DEFAULT NULL COMMENT '预计工时（小时）',
    `actual_hours` double DEFAULT NULL COMMENT '实际工时（小时）',
    `comment_count` int(11) NOT NULL DEFAULT 0 COMMENT '评论数量',
    `attachment_count` int(11) NOT NULL DEFAULT 0 COMMENT '附件数量',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint(20) DEFAULT NULL COMMENT '创建者ID',
    `update_user` bigint(20) DEFAULT NULL COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_create_time` (`create_time`),
    FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 任务成员表
CREATE TABLE IF NOT EXISTS `tb_task_member` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` bigint(20) NOT NULL COMMENT '任务ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_user` (`task_id`,`user_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_user_id` (`user_id`),
    FOREIGN KEY (`task_id`) REFERENCES `tb_task` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务成员关系表';

-- 任务标签表
CREATE TABLE IF NOT EXISTS `tb_task_tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(50) NOT NULL COMMENT '标签名称',
    `color` varchar(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `project_id` bigint(20) DEFAULT NULL COMMENT '所属项目ID（NULL表示全局标签）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user` bigint(20) DEFAULT NULL COMMENT '创建者ID',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务标签表';

-- 任务-标签关系表
CREATE TABLE IF NOT EXISTS `tb_task_tag_rel` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` bigint(20) NOT NULL COMMENT '任务ID',
    `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_tag` (`task_id`,`tag_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_tag_id` (`tag_id`),
    FOREIGN KEY (`task_id`) REFERENCES `tb_task` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tb_task_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务-标签关系表';

-- 任务附件表
CREATE TABLE IF NOT EXISTS `tb_task_attachment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` bigint(20) NOT NULL COMMENT '任务ID',
    `file_name` varchar(200) NOT NULL COMMENT '文件名',
    `file_path` varchar(500) NOT NULL COMMENT '文件路径',
    `file_size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
    `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user` bigint(20) DEFAULT NULL COMMENT '创建者ID',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    FOREIGN KEY (`task_id`) REFERENCES `tb_task` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务附件表';

-- 任务评论表
CREATE TABLE IF NOT EXISTS `tb_task_comment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` bigint(20) NOT NULL COMMENT '任务ID',
    `content` text NOT NULL COMMENT '评论内容',
    `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID（NULL表示顶级评论）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user` bigint(20) DEFAULT NULL COMMENT '创建者ID',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_parent_id` (`parent_id`),
    FOREIGN KEY (`task_id`) REFERENCES `tb_task` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `tb_task_comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务评论表'; 