# 数据库结构修改说明

本目录包含数据库结构修改脚本，用于确保数据库结构与实体类和DTO设计保持一致。

## 标签表结构重构（tag_tables_restructure.sql）

此脚本用于重构标签相关的表结构，使其符合当前的实体类和DTO设计。

### 变更内容

1. 如果存在`tb_tag`表，将其数据迁移到`tb_task_tag`表，然后删除`tb_tag`表。
2. 确保`tb_task_tag`表具有正确的结构（包含id、name、description、create_time、update_time、create_user、update_user字段）。
3. 确保`tb_task_tag_rel`表具有正确的结构（包含id、task_id、tag_id、create_time、update_time字段），用于存储标签和任务的多对多关系。

### 执行步骤

**重要：执行前请务必备份数据库！**

1. 连接到MySQL数据库：
```bash
mysql -u username -p your_database_name
```

2. 执行SQL脚本：
```sql
source /path/to/tag_tables_restructure.sql
```

或者使用MySQL客户端工具（如MySQL Workbench、Navicat等）执行脚本。

### 注意事项

1. 脚本包含数据迁移逻辑，会自动处理从旧表结构到新表结构的迁移。
2. 执行脚本前请确保数据库中没有与脚本冲突的触发器、存储过程或外键约束。
3. 如果执行过程中出现错误，请使用之前的备份恢复数据库，并联系开发团队。

## 变更后的表结构

### tb_task_tag（标签表）
- `id`: 标签ID
- `name`: 标签名称
- `description`: 标签描述
- `create_time`: 创建时间
- `update_time`: 更新时间
- `create_user`: 创建人
- `update_user`: 修改人

### tb_task_tag_rel（任务标签关联表）
- `id`: 关联ID
- `task_id`: 任务ID
- `tag_id`: 标签ID
- `create_time`: 创建时间
- `update_time`: 更新时间 