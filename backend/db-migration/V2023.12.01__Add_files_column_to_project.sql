-- 在tb_project表中添加files字段用于存储阿里云OSS文件链接
ALTER TABLE tb_project ADD COLUMN files TEXT COMMENT '项目文件，阿里云OSS链接，多个以逗号分隔'; 