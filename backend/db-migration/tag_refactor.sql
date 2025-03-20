-- 创建迁移脚本：将标签表从tb_tag迁移到tb_task_tag

-- 步骤1：备份现有数据（如果有）
CREATE TABLE IF NOT EXISTS `tb_tag_backup` LIKE `tb_tag`;
INSERT INTO `tb_tag_backup` SELECT * FROM `tb_tag`;

-- 步骤2：创建迁移数据到新表的过程
-- 如果tb_task_tag表已存在，确保它有正确的结构
DROP TABLE IF EXISTS `tb_task_tag`;
CREATE TABLE `tb_task_tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(50) NOT NULL COMMENT '标签名称',
    `color` varchar(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `project_id` bigint(20) DEFAULT NULL COMMENT '所属项目ID（NULL表示全局标签）',
    `description` varchar(255) DEFAULT NULL COMMENT '标签描述',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint(20) DEFAULT NULL COMMENT '创建者ID',
    `update_user` bigint(20) DEFAULT NULL COMMENT '更新者ID',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    FOREIGN KEY (`project_id`) REFERENCES `tb_project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务标签表';

-- 步骤3：确保tb_task_tag_rel表存在
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

-- 步骤4：迁移数据从tb_tag到tb_task_tag
INSERT INTO `tb_task_tag` (`name`, `color`, `project_id`, `description`, `create_time`, `update_time`, `create_user`, `update_user`)
SELECT `name`, `color`, `project_id`, `description`, `create_time`, `update_time`, `create_user`, `update_user` 
FROM `tb_tag`;

-- 步骤5：迁移关联数据到tb_task_tag_rel（如果有）
-- 假设有一个关系表tb_task_tag保存了任务和标签的关系
INSERT INTO `tb_task_tag_rel` (`task_id`, `tag_id`, `create_time`)
SELECT `task_id`, `tag_id`, NOW() 
FROM `tb_task_tag` 
WHERE EXISTS (SELECT 1 FROM `tb_tag` WHERE `tb_tag`.`id` = `tb_task_tag`.`tag_id`);

-- 完成迁移后不要立即删除旧表，等确认一切正常后再删除
-- DROP TABLE `tb_tag`; 