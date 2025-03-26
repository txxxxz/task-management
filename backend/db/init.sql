-- 创建数据库
CREATE DATABASE IF NOT EXISTS task_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE task_management;

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-普通成员，1-leader，2-admin',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT COMMENT '创建者ID',
    update_user BIGINT COMMENT '更新者ID'
) COMMENT '用户表';

-- 项目表
CREATE TABLE IF NOT EXISTS tb_project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '项目状态：0-筹备中，1-进行中，2-已完成，3-已归档',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    priority TINYINT NOT NULL DEFAULT 1 COMMENT '项目优先级：1-低，2-中，3-高，4-紧急',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT COMMENT '创建者ID',
    update_user BIGINT COMMENT '更新者ID'
) COMMENT '项目表';

-- 任务表
CREATE TABLE IF NOT EXISTS tb_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '任务状态：0-待处理，1-进行中，2-已完成，3-已取消',
    priority TINYINT NOT NULL DEFAULT 1 COMMENT '优先级：1-低，2-中，3-高，4-紧急',
    start_time DATETIME COMMENT '开始时间',
    deadline DATETIME COMMENT '截止时间',
    completed_time DATETIME COMMENT '实际完成时间',
    estimated_hours DOUBLE COMMENT '预计工时（小时）',
    actual_hours DOUBLE COMMENT '实际工时（小时）',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数量',
    attachment_count INT NOT NULL DEFAULT 0 COMMENT '附件数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT COMMENT '创建者ID',
    update_user BIGINT COMMENT '更新者ID',
    FOREIGN KEY (project_id) REFERENCES tb_project(id)
) COMMENT '任务表';

-- 任务成员表
CREATE TABLE IF NOT EXISTS tb_task_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-参与者，1-负责人',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '成员状态：0-待接受，1-已接受，2-已拒绝',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT COMMENT '创建者ID',
    update_user BIGINT COMMENT '更新者ID',
    FOREIGN KEY (task_id) REFERENCES tb_task(id),
    FOREIGN KEY (user_id) REFERENCES tb_user(id),
    UNIQUE KEY uk_task_user (task_id, user_id)
) COMMENT '任务成员表';

-- 标签表
CREATE TABLE IF NOT EXISTS tb_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    color VARCHAR(20) NOT NULL COMMENT '标签颜色（十六进制颜色码）',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    description VARCHAR(255) COMMENT '标签描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT COMMENT '创建者ID',
    update_user BIGINT COMMENT '更新者ID',
    FOREIGN KEY (project_id) REFERENCES tb_project(id)
) COMMENT '标签表';

-- 任务标签关联表
CREATE TABLE IF NOT EXISTS tb_task_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT COMMENT '创建者ID',
    update_user BIGINT COMMENT '更新者ID',
    FOREIGN KEY (task_id) REFERENCES tb_task(id),
    FOREIGN KEY (tag_id) REFERENCES tb_tag(id),
    UNIQUE KEY uk_task_tag (task_id, tag_id)
) COMMENT '任务标签关联表';

-- 评论表已经转移到task_tables.sql文件中

-- 附件表
CREATE TABLE IF NOT EXISTS tb_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_path VARCHAR(255) NOT NULL COMMENT '文件路径',
    task_id BIGINT NOT NULL COMMENT '所属任务ID',
    md5 VARCHAR(32) NOT NULL COMMENT '文件MD5值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT COMMENT '创建者ID',
    update_user BIGINT COMMENT '更新者ID',
    FOREIGN KEY (task_id) REFERENCES tb_task(id)
) COMMENT '附件表'; 