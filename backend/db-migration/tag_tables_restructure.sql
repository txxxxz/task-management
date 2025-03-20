-- 标签表和任务标签关联表重构脚本
-- 注意：执行此脚本前请备份数据库

-- 步骤1：检查是否有需要迁移的表
SET @tag_table_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'tb_tag');
SET @task_tag_exists = (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'tb_task_tag');

-- 步骤2：如果存在tb_tag表或tb_task_tag表需要重构，则进行迁移
DROP PROCEDURE IF EXISTS migrate_tag_data;
DELIMITER //
CREATE PROCEDURE migrate_tag_data()
BEGIN
    -- 先备份现有tb_task_tag表（如果存在）
    IF @task_tag_exists > 0 THEN
        CREATE TABLE IF NOT EXISTS `tb_task_tag_backup` LIKE `tb_task_tag`;
        INSERT INTO `tb_task_tag_backup` SELECT * FROM `tb_task_tag`;
        
        -- 检查tb_task_tag表是否有project_id字段
        SET @has_project_id = (SELECT COUNT(*) FROM information_schema.columns 
                              WHERE table_schema = DATABASE() 
                              AND table_name = 'tb_task_tag' 
                              AND column_name = 'project_id');
                              
        -- 如果有project_id字段，需要重构表结构
        IF @has_project_id > 0 THEN
            -- 创建临时表存储数据
            CREATE TEMPORARY TABLE `temp_task_tag` (
                `id` bigint NOT NULL,
                `name` varchar(32) NOT NULL,
                `description` varchar(255) DEFAULT NULL,
                `create_time` datetime NOT NULL,
                `update_time` datetime NOT NULL,
                `create_user` bigint NOT NULL,
                `update_user` bigint NOT NULL
            );
            
            -- 复制数据到临时表（不包含project_id）
            INSERT INTO `temp_task_tag` 
            SELECT `id`, `name`, `description`, `create_time`, `update_time`, `create_user`, `update_user` 
            FROM `tb_task_tag`;
            
            -- 删除旧表
            DROP TABLE `tb_task_tag`;
            
            -- 创建新表结构（不包含project_id）
            CREATE TABLE `tb_task_tag` (
                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                `name` varchar(32) NOT NULL COMMENT '标签名称',
                `description` varchar(255) DEFAULT NULL COMMENT '标签描述',
                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                `create_user` bigint NOT NULL COMMENT '创建人',
                `update_user` bigint NOT NULL COMMENT '修改人',
                PRIMARY KEY (`id`),
                UNIQUE KEY `uk_name` (`name`) COMMENT '标签名称唯一'
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
            
            -- 从临时表恢复数据
            INSERT INTO `tb_task_tag` 
            SELECT * FROM `temp_task_tag`;
            
            -- 删除临时表
            DROP TEMPORARY TABLE `temp_task_tag`;
        END IF;
    ELSE
        -- 如果tb_task_tag表不存在，直接创建新表
        CREATE TABLE `tb_task_tag` (
            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
            `name` varchar(32) NOT NULL COMMENT '标签名称',
            `description` varchar(255) DEFAULT NULL COMMENT '标签描述',
            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
            `create_user` bigint NOT NULL COMMENT '创建人',
            `update_user` bigint NOT NULL COMMENT '修改人',
            PRIMARY KEY (`id`),
            UNIQUE KEY `uk_name` (`name`) COMMENT '标签名称唯一'
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
    END IF;
    
    -- 如果存在tb_tag表，将数据迁移到tb_task_tag表
    IF @tag_table_exists > 0 THEN
        -- 迁移tb_tag表数据到tb_task_tag表（不包含project_id）
        INSERT INTO tb_task_tag (id, name, description, create_time, update_time, create_user, update_user)
        SELECT id, name, description, create_time, update_time, create_user, update_user
        FROM tb_tag;
        
        -- 删除旧的tb_tag表
        DROP TABLE tb_tag;
    END IF;
END //
DELIMITER ;

-- 执行迁移过程
CALL migrate_tag_data();
DROP PROCEDURE migrate_tag_data;

-- 步骤3：确保任务标签关联表结构正确
CREATE TABLE IF NOT EXISTS `tb_task_tag_rel` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `tag_id` bigint NOT NULL COMMENT '标签ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_tag` (`task_id`, `tag_id`) COMMENT '任务标签关联唯一',
    KEY `idx_task_id` (`task_id`) COMMENT '任务ID索引',
    KEY `idx_tag_id` (`tag_id`) COMMENT '标签ID索引',
    CONSTRAINT `fk_task_tag_rel_task` FOREIGN KEY (`task_id`) REFERENCES `tb_task` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_task_tag_rel_tag` FOREIGN KEY (`tag_id`) REFERENCES `tb_task_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务标签关联表'; 