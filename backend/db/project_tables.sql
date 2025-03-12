-- 项目表
CREATE TABLE IF NOT EXISTS `tb_project` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(100) NOT NULL COMMENT '项目名称',
    `description` text COMMENT '项目描述',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '项目状态：0-筹备中，1-进行中，2-已完成，3-已归档',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `priority` tinyint(4) NOT NULL DEFAULT '2' COMMENT '优先级：1-低，2-中，3-高，4-紧急',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    `create_user` bigint(20) NOT NULL COMMENT '创建人ID',
    `update_user` bigint(20) NOT NULL COMMENT '修改人ID',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 项目成员关系表
CREATE TABLE IF NOT EXISTS `tb_project_member` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `project_id` bigint(20) NOT NULL COMMENT '项目ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员关系表'; 