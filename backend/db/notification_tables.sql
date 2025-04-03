-- 通知表
CREATE TABLE IF NOT EXISTS tb_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型：task_update-任务更新, comment_mention-评论@',
    content VARCHAR(500) NOT NULL COMMENT '通知内容',
    user_id BIGINT NOT NULL COMMENT '接收通知的用户ID',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    related_id BIGINT DEFAULT NULL COMMENT '关联ID（如任务ID或评论ID）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_is_read (is_read),
    KEY idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES tb_user (id) ON DELETE CASCADE
) COMMENT='通知表'; 