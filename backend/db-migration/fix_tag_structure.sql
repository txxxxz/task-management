-- 修复标签表结构问题

-- 步骤1：备份现有数据（如果需要）
CREATE TABLE IF NOT EXISTS `tb_task_tag_backup` LIKE `tb_task_tag`;
INSERT INTO `tb_task_tag_backup` SELECT * FROM `tb_task_tag`;

-- 步骤2：删除任务标签关联表的外键约束
ALTER TABLE `tb_task_tag_rel` DROP FOREIGN KEY `fk_task_tag_rel_tag`;

-- 步骤3：删除旧的标签表
DROP TABLE IF EXISTS `tb_task_tag`;

-- 步骤4：创建新的标签表（不包含task_id字段）
CREATE TABLE `tb_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(32) NOT NULL COMMENT '标签名称',
    `description` varchar(255) DEFAULT NULL COMMENT '标签描述',
    `color` varchar(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint DEFAULT NULL COMMENT '创建人',
    `update_user` bigint DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`) COMMENT '标签名称唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

-- 步骤5：从备份表恢复数据到新表（如果有备份且结构兼容）
-- INSERT INTO `tb_tag` (id, name, description, color, create_time, update_time, create_user, update_user)
-- SELECT id, name, description, color, create_time, create_time, create_user, create_user FROM `tb_task_tag_backup`;

-- 步骤6：更新任务标签关联表结构
DROP TABLE IF EXISTS `tb_task_tag_rel`;
CREATE TABLE `tb_task_tag_rel` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `tag_id` bigint NOT NULL COMMENT '标签ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_tag` (`task_id`, `tag_id`) COMMENT '任务标签关联唯一',
    KEY `idx_task_id` (`task_id`) COMMENT '任务ID索引',
    KEY `idx_tag_id` (`tag_id`) COMMENT '标签ID索引',
    CONSTRAINT `fk_task_tag_rel_task` FOREIGN KEY (`task_id`) REFERENCES `tb_task` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_task_tag_rel_tag` FOREIGN KEY (`tag_id`) REFERENCES `tb_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务标签关联表';

-- 注释: 如果需要从旧表恢复数据关系，可以在执行完上述步骤后添加适当的数据迁移语句 