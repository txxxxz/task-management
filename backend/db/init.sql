
-- auto-generated definition
create table tb_comment
(
    id          bigint auto_increment comment 'Primary key ID'
        primary key,
    task_id     bigint                             not null comment 'Task ID',
    content     text                               not null comment 'Comment Content',
    parent_id   bigint                             null comment 'Parent comment ID (NULL indicates top-level comment)',
    create_time datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    create_user bigint                             null comment 'Create User ID',
    constraint tb_comment_ibfk_1
        foreign key (task_id) references tb_task (id)
            on delete cascade,
    constraint tb_comment_ibfk_2
        foreign key (parent_id) references tb_comment (id)
            on delete cascade
)
    comment 'Comment Table';

create index idx_parent_id
    on tb_comment (parent_id);

create index idx_task_id
    on tb_comment (task_id);
```

```
-- auto-generated definition
create table tb_notification
(
    id          bigint auto_increment comment 'Primary Key ID'
        primary key,
    type        varchar(50)                          not null comment 'Notification Type：task_update-Update Task, comment_mention-Comment@',
    content     varchar(500)                         not null comment 'Notification Content',
    user_id     bigint                               not null comment 'ID for Receiving Notification User',
    is_read     tinyint(1) default 0                 not null comment 'Read or not: 0- unread, 1- Read',
    related_id  bigint                               null comment 'Associated ID (task ID & comment ID)',
    create_time datetime   default CURRENT_TIMESTAMP not null comment 'Create Time',
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    constraint tb_notification_ibfk_1
        foreign key (user_id) references tb_user (id)
            on delete cascade
)
    comment 'Notification Table';

create index idx_create_time
    on tb_notification (create_time);

create index idx_is_read
    on tb_notification (is_read);

create index idx_user_id
    on tb_notification (user_id);
```

```
-- auto-generated definition
create table tb_project
(
    id          bigint auto_increment comment 'Primary Key ID'
        primary key,
    name        varchar(100)                       not null comment 'Project Name',
    description text                               null comment 'Project Description',
    status      tinyint  default 0                 not null comment 'Project status: 0- in preparation, 1- in progress, 2- completed, 3- achived',
    start_time  datetime                           null comment 'Start Time',
    end_time    datetime                           null comment 'End Time',
    priority    tinyint  default 1                 not null comment 'Project priority: 1- low, 2- medium, 3- High, 4- Crtical',
    create_time datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    create_user bigint                             null comment 'Create User ID',
    update_user bigint                             null comment 'Update User ID'
)
    comment 'Project Table';
```

```
-- auto-generated definition
create table tb_project_attachment
(
    id          bigint auto_increment comment 'Primary Key ID'
        primary key,
    project_id  bigint                             not null comment 'Project ID',
    file_name   varchar(200)                       not null comment 'File Name',
    file_path   varchar(500)                       not null comment 'File Path',
    file_size   bigint                             not null comment 'File Size (Byte)',
    file_type   varchar(100)                       null comment 'File Type',
    create_time datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    create_user bigint                             null comment 'Create User ID',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    update_user bigint                             null comment 'Update User ID',
    constraint tb_project_attachment_ibfk_1
        foreign key (project_id) references tb_project (id)
            on delete cascade
)
    comment 'Project Attachment Table';

create index idx_project_id
    on tb_project_attachment (project_id);
```

```
-- auto-generated definition
create table tb_project_member
(
    id          bigint auto_increment comment 'Primary Key ID'
        primary key,
    project_id  bigint   not null comment 'Project ID',
    user_id     bigint   not null comment 'User ID',
    create_time datetime not null comment 'Create Time',
    constraint uk_project_user
        unique (project_id, user_id)
)
    comment 'Project Member Table';

create index idx_project_id
    on tb_project_member (project_id);

create index idx_user_id
    on tb_project_member (user_id);
```

```
-- auto-generated definition
create table tb_tag
(
    id          bigint auto_increment comment '主键'
        primary key,
    name        varchar(32)                        not null comment 'Tag Name',
    description varchar(255)                       null comment 'Tag Description',
    create_time datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    create_user bigint                             null comment 'Create User',
    update_user bigint                             null comment 'Update User',
    color       varchar(50)                        null comment 'Tag Color',
    constraint uk_name
        unique (name) comment '标签名称唯一'
)
    comment 'Tag  Table';
```

```
-- auto-generated definition
create table tb_task
(
    id               bigint auto_increment comment 'Primary Key ID'
        primary key,
    name             varchar(200)                       not null comment 'Task Name',
    description      text                               null comment 'Task Decription',
    project_id       bigint                             not null comment 'Belonging Project ID',
    status           tinyint  default 0                 not null comment 'Task status: 0- Pending, 1- in progress, 2- Completed, 3- Cancelled',
    priority         tinyint  default 1                 not null comment 'Priority: 1- Low, 2- Medium, 3- High, 4- Critical',
    start_time       datetime                           null comment 'Start Time',
    deadline         datetime                           null comment 'Due Time',
    completed_time   datetime                           null comment 'Completed Time',
    comment_count    int      default 0                 not null comment 'Comment Count',
    attachment_count int      default 0                 not null comment 'Attachment_Count',
    create_time      datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    update_time      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    create_user      bigint                             null comment 'Create User',
    update_user      bigint                             null comment 'Update User',
    constraint tb_task_ibfk_1
        foreign key (project_id) references tb_project (id)
)
    comment 'Task Table';

create index project_id
    on tb_task (project_id);
```

```
-- auto-generated definition
create table tb_task_attachment
(
    id          bigint auto_increment comment 'Primary Key ID'
        primary key,
    task_id     bigint                             not null comment 'Task ID',
    file_name   varchar(200)                       not null comment 'File Name',
    file_path   varchar(500)                       not null comment 'File Path',
    file_size   bigint                             not null comment 'File Size (Byte)',
    file_type   varchar(100)                       null comment 'File Type',
    create_time datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    create_user bigint                             null comment 'Create User',
    update_time datetime default (now())           null comment 'Update Time',
    update_user bigint                             null comment 'Update User',
    constraint tb_task_attachment_ibfk_1
        foreign key (task_id) references tb_task (id)
            on delete cascade
)
    comment 'Task Attachment Table';

create index idx_task_id
    on tb_task_attachment (task_id);
```

```
-- auto-generated definition
create table tb_task_member
(
    id          bigint auto_increment comment 'Primary Key ID'
        primary key,
    task_id     bigint                             not null comment 'Task ID',
    user_id     bigint                             not null comment 'User ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    constraint uk_task_user
        unique (task_id, user_id),
    constraint tb_task_member_ibfk_1
        foreign key (task_id) references tb_task (id)
            on delete cascade,
    constraint tb_task_member_ibfk_2
        foreign key (user_id) references tb_user (id)
            on delete cascade
)
    comment 'Task Member Table';

create index idx_task_id
    on tb_task_member (task_id);

create index idx_user_id
    on tb_task_member (user_id);


-- auto-generated definition
create table tb_task_tag_rel
(
    id          bigint auto_increment comment 'Primary Key ID'
        primary key,
    task_id     bigint                             not null comment 'Task ID',
    tag_id      bigint                             not null comment 'Tag ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    constraint uk_task_tag
        unique (task_id, tag_id) comment '任务标签关联唯一',
    constraint fk_task_tag_rel_tag
        foreign key (tag_id) references tb_tag (id)
            on delete cascade,
    constraint fk_task_tag_rel_task
        foreign key (task_id) references tb_task (id)
            on delete cascade
)
    comment 'Task Tag Relation Table';

create index idx_tag_id
    on tb_task_tag_rel (tag_id)
    comment '标签ID索引';

create index idx_task_id
    on tb_task_tag_rel (task_id)
    comment '任务ID索引';



-- auto-generated definition
create table tb_user
(
    id          bigint auto_increment comment 'Primary Key ID'
        primary key,
    username    varchar(50)                        not null comment 'Username',
    password    varchar(100)                       not null comment 'Password',
    email       varchar(100)                       null comment 'Email',
    phone       varchar(20)                        null comment 'Phone',
    avatar      varchar(255)                       null comment 'Avatar URL',
    status      tinyint  default 1                 not null comment 'User status: 0-Disabled, 1-Normal',
    role        tinyint  default 0                 not null comment 'Roles: 0- Member, 1-leader, 2-admin',
    create_time datetime default CURRENT_TIMESTAMP not null comment 'Create Time',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    create_user bigint                             null comment 'Create User',
    update_user bigint                             null comment 'Update User',
    constraint username
        unique (username)
)
    comment 'User Table';

