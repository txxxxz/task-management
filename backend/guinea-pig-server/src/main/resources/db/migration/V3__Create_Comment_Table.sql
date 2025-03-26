-- 创建评论表
CREATE TABLE IF NOT EXISTS t_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    reply_to BIGINT COMMENT '回复的评论ID，如果是顶级评论则为NULL',
    INDEX idx_task_id (task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_reply_to (reply_to)
) COMMENT='任务评论表';

-- 添加外键约束
ALTER TABLE t_comment 
ADD CONSTRAINT fk_comment_task 
FOREIGN KEY (task_id) REFERENCES t_task(id) 
ON DELETE CASCADE;

ALTER TABLE t_comment 
ADD CONSTRAINT fk_comment_user 
FOREIGN KEY (user_id) REFERENCES t_user(id);

ALTER TABLE t_comment 
ADD CONSTRAINT fk_comment_reply 
FOREIGN KEY (reply_to) REFERENCES t_comment(id) 
ON DELETE SET NULL; 