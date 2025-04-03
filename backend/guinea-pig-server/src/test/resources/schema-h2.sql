-- 首先删除所有引用了其他表的表（先删除子表）
DROP TABLE IF EXISTS tb_notification;
DROP TABLE IF EXISTS tb_comment;
DROP TABLE IF EXISTS tb_task_tag_rel;
DROP TABLE IF EXISTS tb_task_attachment;
DROP TABLE IF EXISTS tb_task_member;
DROP TABLE IF EXISTS tb_project_attachment;
DROP TABLE IF EXISTS tb_project_member;
DROP TABLE IF EXISTS tb_file;
DROP TABLE IF EXISTS tb_task;
DROP TABLE IF EXISTS tb_project;
DROP TABLE IF EXISTS tb_tag;
DROP TABLE IF EXISTS tb_user;

-- 创建用户表
CREATE TABLE `tb_user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    username VARCHAR(50) NOT NULL COMMENT 'Username',
    password VARCHAR(100) NOT NULL COMMENT 'Password',
    email VARCHAR(100) NULL COMMENT 'Email',
    phone VARCHAR(20) NULL COMMENT 'Phone',
    avatar VARCHAR(255) NULL COMMENT 'Avatar URL',
    status TINYINT DEFAULT 1 NOT NULL COMMENT 'User status: 0-Disabled, 1-Normal',
    role TINYINT DEFAULT 0 NOT NULL COMMENT 'Roles: 0- Member, 1-leader, 2-admin',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Update Time',
    create_user BIGINT NULL COMMENT 'Create User',
    update_user BIGINT NULL COMMENT 'Update User',
    CONSTRAINT username UNIQUE (username)
);

-- 测试不需要预设数据，移除预设数据以避免主键冲突

DROP TABLE IF EXISTS tb_project;

CREATE TABLE tb_project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    name VARCHAR(100) NOT NULL COMMENT 'Project Name',
    description TEXT NULL COMMENT 'Project Description',
    status TINYINT DEFAULT 0 NOT NULL COMMENT 'Project status: 0- in preparation, 1- in progress, 2- completed, 3- achived',
    start_time DATETIME NULL COMMENT 'Start Time',
    end_time DATETIME NULL COMMENT 'End Time',
    priority TINYINT DEFAULT 1 NOT NULL COMMENT 'Project priority: 1- low, 2- medium, 3- High, 4- Crtical',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Update Time',
    create_user BIGINT NULL COMMENT 'Create User ID',
    update_user BIGINT NULL COMMENT 'Update User ID'
);

DROP TABLE IF EXISTS tb_task;

CREATE TABLE tb_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    name VARCHAR(200) NOT NULL COMMENT 'Task Name',
    description TEXT NULL COMMENT 'Task Decription',
    project_id BIGINT NOT NULL COMMENT 'Belonging Project ID',
    status TINYINT DEFAULT 0 NOT NULL COMMENT 'Task status: 0- Pending, 1- in progress, 2- Completed, 3- Cancelled',
    priority TINYINT DEFAULT 1 NOT NULL COMMENT 'Priority: 1- Low, 2- Medium, 3- High, 4- Critical',
    start_time DATETIME NULL COMMENT 'Start Time',
    deadline DATETIME NULL COMMENT 'Due Time',
    completed_time DATETIME NULL COMMENT 'Completed Time',
    comment_count INT DEFAULT 0 NOT NULL COMMENT 'Comment Count',
    attachment_count INT DEFAULT 0 NOT NULL COMMENT 'Attachment_Count',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Update Time',
    create_user BIGINT NULL COMMENT 'Create User',
    update_user BIGINT NULL COMMENT 'Update User'
);
CREATE INDEX idx_task_project_id ON tb_task (project_id);

DROP TABLE IF EXISTS tb_tag;

CREATE TABLE tb_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(32) NOT NULL COMMENT 'Tag Name',
    description VARCHAR(255) NULL COMMENT 'Tag Description',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Update Time',
    create_user BIGINT NULL COMMENT 'Create User',
    update_user BIGINT NULL COMMENT 'Update User',
    color VARCHAR(50) NULL COMMENT 'Tag Color',
    CONSTRAINT uk_name UNIQUE (name)
);

DROP TABLE IF EXISTS tb_file;

CREATE TABLE tb_file (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  original_name VARCHAR(255) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  file_size BIGINT,
  file_type VARCHAR(32),
  task_id BIGINT,
  create_time DATETIME,
  update_time DATETIME,
  create_user BIGINT,
  update_user BIGINT
);

-- 项目成员表
DROP TABLE IF EXISTS tb_project_member;
CREATE TABLE tb_project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    project_id BIGINT NOT NULL COMMENT 'Project ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    create_time DATETIME NOT NULL COMMENT 'Create Time',
    CONSTRAINT uk_project_user UNIQUE (project_id, user_id)
);
CREATE INDEX idx_project_member_project_id ON tb_project_member (project_id);
CREATE INDEX idx_project_member_user_id ON tb_project_member (user_id);

-- 项目附件表
DROP TABLE IF EXISTS tb_project_attachment;
CREATE TABLE tb_project_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    project_id BIGINT NOT NULL COMMENT 'Project ID',
    file_name VARCHAR(200) NOT NULL COMMENT 'File Name',
    file_path VARCHAR(500) NOT NULL COMMENT 'File Path',
    file_size BIGINT NOT NULL COMMENT 'File Size (Byte)',
    file_type VARCHAR(100) NULL COMMENT 'File Type',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    create_user BIGINT NULL COMMENT 'Create User ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Update Time',
    update_user BIGINT NULL COMMENT 'Update User ID'
);
CREATE INDEX idx_project_attachment_project_id ON tb_project_attachment (project_id);

-- 任务成员表
DROP TABLE IF EXISTS tb_task_member;
CREATE TABLE tb_task_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    task_id BIGINT NOT NULL COMMENT 'Task ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    CONSTRAINT uk_task_user UNIQUE (task_id, user_id)
);
CREATE INDEX idx_task_member_task_id ON tb_task_member (task_id);
CREATE INDEX idx_task_member_user_id ON tb_task_member (user_id);

-- 任务附件表
DROP TABLE IF EXISTS tb_task_attachment;
CREATE TABLE tb_task_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    task_id BIGINT NOT NULL COMMENT 'Task ID',
    file_name VARCHAR(200) NOT NULL COMMENT 'File Name',
    file_path VARCHAR(500) NOT NULL COMMENT 'File Path',
    file_size BIGINT NOT NULL COMMENT 'File Size (Byte)',
    file_type VARCHAR(100) NULL COMMENT 'File Type',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    create_user BIGINT NULL COMMENT 'Create User',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT 'Update Time',
    update_user BIGINT NULL COMMENT 'Update User'
);
CREATE INDEX idx_task_attachment_task_id ON tb_task_attachment (task_id);

-- 任务标签关联表
DROP TABLE IF EXISTS tb_task_tag_rel;
CREATE TABLE tb_task_tag_rel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    task_id BIGINT NOT NULL COMMENT 'Task ID',
    tag_id BIGINT NOT NULL COMMENT 'Tag ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    CONSTRAINT uk_task_tag UNIQUE (task_id, tag_id)
);
CREATE INDEX idx_task_tag_rel_task_id ON tb_task_tag_rel (task_id);
CREATE INDEX idx_task_tag_rel_tag_id ON tb_task_tag_rel (tag_id);

-- 评论表
DROP TABLE IF EXISTS tb_comment;
CREATE TABLE tb_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    task_id BIGINT NOT NULL COMMENT 'Task ID',
    content TEXT NOT NULL COMMENT 'Comment Content',
    parent_id BIGINT NULL COMMENT 'Parent comment ID (NULL indicates top-level comment)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    create_user BIGINT NULL COMMENT 'Create User ID'
);
CREATE INDEX idx_comment_task_id ON tb_comment (task_id);
CREATE INDEX idx_comment_parent_id ON tb_comment (parent_id);

-- 通知表
DROP TABLE IF EXISTS tb_notification;
CREATE TABLE tb_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key ID',
    type VARCHAR(50) NOT NULL COMMENT 'Notification Type：task_update-Update Task, comment_mention-Comment@',
    content VARCHAR(500) NOT NULL COMMENT 'Notification Content',
    user_id BIGINT NOT NULL COMMENT 'ID for Receiving Notification User',
    is_read BOOLEAN DEFAULT FALSE NOT NULL COMMENT 'Read or not: 0- unread, 1- Read',
    related_id BIGINT NULL COMMENT 'Associated ID (task ID & comment ID)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Create Time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Update Time'
);
CREATE INDEX idx_notification_user_id ON tb_notification (user_id);
CREATE INDEX idx_notification_is_read ON tb_notification (is_read);
CREATE INDEX idx_notification_create_time ON tb_notification (create_time);

-- 在H2中外键关系需要在所有表创建后添加，以避免顺序问题
ALTER TABLE tb_project_member ADD CONSTRAINT fk_project_member_project 
    FOREIGN KEY (project_id) REFERENCES tb_project(id) ON DELETE CASCADE;
ALTER TABLE tb_project_member ADD CONSTRAINT fk_project_member_user
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE;

ALTER TABLE tb_project_attachment ADD CONSTRAINT fk_project_attachment_project
    FOREIGN KEY (project_id) REFERENCES tb_project(id) ON DELETE CASCADE;

ALTER TABLE tb_task ADD CONSTRAINT fk_task_project
    FOREIGN KEY (project_id) REFERENCES tb_project(id);

ALTER TABLE tb_task_member ADD CONSTRAINT fk_task_member_task
    FOREIGN KEY (task_id) REFERENCES tb_task(id) ON DELETE CASCADE;
ALTER TABLE tb_task_member ADD CONSTRAINT fk_task_member_user
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE;

ALTER TABLE tb_task_attachment ADD CONSTRAINT fk_task_attachment_task
    FOREIGN KEY (task_id) REFERENCES tb_task(id) ON DELETE CASCADE;

ALTER TABLE tb_task_tag_rel ADD CONSTRAINT fk_task_tag_rel_task
    FOREIGN KEY (task_id) REFERENCES tb_task(id) ON DELETE CASCADE;
ALTER TABLE tb_task_tag_rel ADD CONSTRAINT fk_task_tag_rel_tag
    FOREIGN KEY (tag_id) REFERENCES tb_tag(id) ON DELETE CASCADE;

ALTER TABLE tb_comment ADD CONSTRAINT fk_comment_task
    FOREIGN KEY (task_id) REFERENCES tb_task(id) ON DELETE CASCADE;
ALTER TABLE tb_comment ADD CONSTRAINT fk_comment_parent
    FOREIGN KEY (parent_id) REFERENCES tb_comment(id) ON DELETE CASCADE;

ALTER TABLE tb_notification ADD CONSTRAINT fk_notification_user
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE; 