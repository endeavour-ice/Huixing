SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_record
-- ----------------------------
DROP TABLE IF EXISTS `chat_record`;
CREATE TABLE `chat_record`
(
    `id`          varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci        NOT NULL,
    `user_id`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL COMMENT '用户id\r\n',
    `accept_id`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL COMMENT '好友id',
    `message`     varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '消息',
    `has_read`    int(1)                                                         NULL     DEFAULT 0 COMMENT '是否已读 0 未读',
    `send_time`   datetime(0)                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送的时间',
    `create_time` datetime(0)                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_delete`   tinyint(4)                                                     NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `one` (`user_id`, `accept_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '聊天记录表'
  ROW_FORMAT = Dynamic;

ALTER TABLE `user_center`.`chat_record`
    ADD INDEX `delete_index` (`is_delete`);
ALTER TABLE `user_center`.`chat_record`
    ADD INDEX `idx_user_id` (`user_id`);
ALTER TABLE `user_center`.`chat_record`
    ADD INDEX `idx_friend_id` (`accept_id`);
ALTER TABLE `user_center`.`chat_record`
    ADD INDEX `idx_send_time` (`send_time`);

ALTER TABLE `user_center`.`chat_record`
    ADD INDEX `chat_index` (`user_id`, `accept_id`, `send_time`);
DROP TABLE IF EXISTS `chat_hal_record`;
CREATE TABLE `chat_hal_record`
(
    `id`        varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci        NOT NULL,
    `user_id`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL COMMENT '用户id\r\n',
    `message`   varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '消息',
    `has_read`  int(1)                                                         NULL     DEFAULT 0 COMMENT '是否已读 0 未读',
    `send_time` datetime(0)                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送的时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '大厅聊天记录表'
  ROW_FORMAT = Dynamic;
-- ----------------------------
-- Table structure for message_mq
-- ----------------------------
DROP TABLE IF EXISTS `message_mq`;
CREATE TABLE `message_mq`
(
    `message_id`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci         NOT NULL COMMENT 'id',
    `message_body` varchar(1204) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '评论内容',
    `error`        varchar(1204) CHARACTER SET utf8 COLLATE utf8_general_ci       NULL     DEFAULT NULL,
    `create_time`  datetime(0)                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime(0)                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    PRIMARY KEY (`message_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`
(
    `id`             bigint(20)                                              NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`        bigint(20)                                              NOT NULL COMMENT '创建用户 id',
    `content`        text CHARACTER SET utf8 COLLATE utf8_general_ci         NULL COMMENT '内容',
    `tags`           varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '标签id',
    `review_status`  int(20)                                                 NOT NULL DEFAULT 0 COMMENT '状态（0-待审核, 1-通过, 2-拒绝）',
    `review_message` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '审核信息',
    `view_num`       int(20)                                                 NOT NULL DEFAULT 0 COMMENT '浏览数',
    `collect_num`    int(20)                                                 NOT NULL DEFAULT 0 COMMENT '收藏数',
    `thumb_num`      int(20)                                                 NOT NULL DEFAULT 0 COMMENT '点赞数',
    `create_time`    datetime(0)                                             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime(0)                                             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `is_delete`      tinyint(4)                                              NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `userId` (`user_id`) USING BTREE,
    INDEX `thumb` (`thumb_num`) USING BTREE,
    INDEX `is_delete` (`is_delete`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1637022433691082755
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '帖子'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_collect
-- ----------------------------
DROP TABLE IF EXISTS `post_collect`;
CREATE TABLE `post_collect`
(
    `id`          bigint(20)  NOT NULL auto_increment COMMENT 'id',
    `post_id`     bigint(20)  NOT NULL COMMENT '帖子 id',
    `user_id`     bigint(20)  NOT NULL COMMENT '创建用户 id',
    `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1637022474208059394
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '帖子收藏记录'
  ROW_FORMAT = Dynamic;
DROP TABLE IF EXISTS `post_read`;
CREATE TABLE `post_read`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT COMMENT 'id',
    `post_id`     bigint(20)  NOT NULL COMMENT '帖子 id',
    `user_id`     bigint(20)  NOT NULL COMMENT '创建用户 id',
    `read_count`  bigint(20)  NOT NULL default 1 COMMENT '阅读量',
    `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM
  COLLATE = utf8_general_ci COMMENT = '文章阅读';
-- ----------------------------
-- Table structure for post_comment
-- ----------------------------
DROP TABLE IF EXISTS `post_comment`;
CREATE TABLE `post_comment`
(
    `id`          bigint(20)                                                     NOT NULL AUTO_INCREMENT COMMENT 'id',
    `post_id`     bigint(20)                                                     NOT NULL COMMENT '帖子id',
    `user_id`     bigint(20)                                                     NOT NULL COMMENT '评论用户 id',
    `content`     varchar(1204) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '评论内容',
    `create_time` datetime(0)                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0)                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 36
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '评论表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_thumb
-- ----------------------------
DROP TABLE IF EXISTS `post_thumb`;
CREATE TABLE `post_thumb`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT COMMENT 'id',
    `post_id`     bigint(20)  NOT NULL COMMENT '帖子 id',
    `user_id`     bigint(20)  NOT NULL COMMENT '创建用户 id',
    `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1637437591211044867
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '帖子点赞记录'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for read_team_chat
-- ----------------------------
DROP TABLE IF EXISTS `read_team_chat`;
CREATE TABLE `read_team_chat`
(
    `id`          varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `user_id`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '用户id',
    `team_id`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '队伍id',
    `has_read`    int(11)                                                 NULL     DEFAULT 0 COMMENT '未读的信息条数',
    `create_time` datetime(0)                                             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_delete`   tinyint(4)                                              NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '队伍聊天记录表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for team
-- ----------------------------
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team`
(
    `id`          bigint(20)                                               NOT NULL COMMENT 'id',
    `name`        varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '队伍的名称',
    `user_id`     bigint(20)                                               NULL     DEFAULT NULL COMMENT '用户id',
    `description` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '描述',
    `max_num`     bigint(20)                                               NOT NULL DEFAULT 1 COMMENT '最大人数',
    `password`    varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci   NULL     DEFAULT NULL COMMENT '密码',
    `status`      varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '状态',
    `expire_time` datetime(0)                                              NULL     DEFAULT NULL COMMENT '创建队伍的时间',
    `is_delete`   tinyint(4)                                               NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime(0)                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0)                                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    `avatar_url`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '队伍头像',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '队伍表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for team_chat_record
-- ----------------------------
DROP TABLE IF EXISTS `team_chat_record`;
CREATE TABLE `team_chat_record`
(
    `id`          varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci        NOT NULL,
    `user_id`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL COMMENT '用户id',
    `team_id`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL COMMENT '队伍id',
    `message`     varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '消息',
    `has_read`    int(1)                                                         NULL     DEFAULT 0 COMMENT '是否已读 0 未读',
    `create_time` datetime(0)                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_delete`   tinyint(4)                                                     NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '队伍聊天记录表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`           bigint(20)                                               NOT NULL COMMENT 'id',
    `username`     varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '用户名',
    `user_account` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '登陆账号',
    `avatar_url`   varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '用户头像',
    `gender`       varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci   NULL     DEFAULT NULL COMMENT '性别',
    `password`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci   NULL     DEFAULT NULL COMMENT '密码',
    `tel`          varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '手机号',
    `email`        varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '邮箱',
    `profile`      varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '个人简介',
    `planet_code`  varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '编号',
    `tags`         varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '标签列表',
    `user_status`  int(11)                                                  NOT NULL DEFAULT 0 COMMENT '用户状态',
    `role`         int(11)                                                  NOT NULL DEFAULT 0 COMMENT '用户角色 0 - 普通用户 1 - 管理员',
    `is_delete`    tinyint(4)                                               NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time`  datetime(0)                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime(0)                                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `t` (`tags`) USING BTREE,
    INDEX `name` (`user_account`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_friend
-- ----------------------------
DROP TABLE IF EXISTS `user_friend`;
CREATE TABLE `user_friend`
(
    `id`          varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'id',
    `user_id`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '用户id',
    `accept_id`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '朋友id',
    `comments`    varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '朋友备注',
    `create_time` datetime(0)                                             NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '添加好友日期',
    `is_delete`   tinyint(1)                                              NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_friend_req
-- ----------------------------
DROP TABLE IF EXISTS `user_friend_req`;
CREATE TABLE `user_friend_req`
(
    `id`          varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `from_userid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '请求用户id',
    `to_userid`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '被请求好友用户',
    `message`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '发送的消息',
    `user_status` int(1)                                                  NOT NULL DEFAULT 0 COMMENT '消息是否已处理 0 未处理',
    `create_time` datetime(0)                                             NULL     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_label
-- ----------------------------
CREATE TABLE `tags`
(
    `id`          BIGINT(22)             NOT NULL,
    `tag_type`    VARCHAR(255) CHARACTER
        SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '标签类型',
    `tag`         VARCHAR(255) CHARACTER
        SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '标签',
    `creator_id`  BIGINT(22)             NOT NULL COMMENT '创建人id',
    `category`    TINYINT(5)             NOT NULL DEFAULT 0 COMMENT '标签类别 0-通用 1-用户 2-队伍 3-文章 其他',
    `tag_num`     INT(11)                NOT NULL DEFAULT 0 COMMENT '标签使用数',
    `create_time` datetime(0)            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0)            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    `is_delete`   TINYINT(4)             NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB
  CHARACTER
      SET = utf8
  COLLATE = utf8_general_ci COMMENT = '标签表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_notice
-- ----------------------------
DROP TABLE IF EXISTS `user_notice`;
CREATE TABLE `user_notice`
(
    `id`          varchar(22) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `notice`      text CHARACTER SET utf8 COLLATE utf8_general_ci        NULL COMMENT '发布的公告',
    `region`      tinyint(4)                                             NULL     DEFAULT NULL COMMENT '发布的位置',
    `is_delete`   tinyint(4)                                             NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime(0)                                            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0)                                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '公告表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_team
-- ----------------------------
DROP TABLE IF EXISTS `user_team`;
CREATE TABLE `user_team`
(
    `id`          bigint(20)                                              NOT NULL COMMENT 'id',
    `name`        varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '队伍的名称',
    `user_id`     bigint(20)                                              NULL     DEFAULT NULL COMMENT '用户id',
    `team_id`     bigint(20)                                              NULL     DEFAULT NULL COMMENT '队伍id',
    `join_time`   datetime(0)                                             NULL     DEFAULT NULL COMMENT '加入队伍时间',
    `is_delete`   tinyint(4)                                              NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime(0)                                             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0)                                             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `team` (`user_id`, `team_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '队伍表'
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;


-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
drop table if exists op_log;
create table op_log
(
    id          bigint(20) not null auto_increment comment '日志主键',
    method_name varchar(100)  default '' comment '方法名称',
    method      varchar(10)   default '' comment '请求方式',
    op_type     int(1)        default 0 comment '操作类别（0其它 1后台用户 2手机端用户）',
    op_url      varchar(255)  default '' comment '请求URL',
    ex_time     bigint(20)    default 0 comment '方法执行时间',
    op_ip       varchar(128)  default '' comment '主机地址',
    op_location varchar(255)  default '' comment '操作地点',
    param_data  varchar(2000) default '' comment '请求参数',
    result_data varchar(2000) default '' comment '返回参数',
    status      int(1)        default 0 comment '操作状态（0正常 1异常）',
    error_msg   varchar(2000) default '' comment '错误消息',
    op_time     datetime comment '操作时间',
    primary key (id)
) engine = innodb
  auto_increment = 100 comment = '操作日志记录';

CREATE TABLE `image`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT COMMENT 'id',
    `image_url`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '图片url',
    `update_time` datetime(0)                                             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    `create_time` datetime(0)                                             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_delete`   tinyint(4)                                              NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '图片'
  ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `user_score`;
CREATE TABLE `user_score`
(
    `id`          bigint(20)     NOT NULL AUTO_INCREMENT COMMENT 'id',
    user_id       varchar(22)    not null comment '用户id',
    score_id      varchar(22)    not null comment '对外暴露id',
    score         decimal(10, 2) null     default 0 comment '积分',
    `is_delete`   tinyint(4)     NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime(0)    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0)    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '积分表'
  ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `score_order`;
CREATE TABLE `score_order`
(
    `id`          bigint(20)     NOT NULL AUTO_INCREMENT COMMENT 'id',
    user_id       varchar(22)    not null comment '用户id',
    order_id      varchar(22)    not null comment '对外暴露id',
    score         decimal(10, 2) not null,
    score_amount  decimal(10, 2) not null comment '应付金额',
    order_status  tinyint(4)     not null comment '订单状态',
    `is_delete`   tinyint(4)     NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime(0)    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0)    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '订单表'
  ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `score_payment`;
CREATE TABLE `score_payment`
(
    `id`           bigint(20)     NOT NULL AUTO_INCREMENT COMMENT 'id',
    order_id       bigint(22)     not null comment '订单id',
    payment_id     varchar(22)    not null comment '对外暴露id',
    score_amount   decimal(10, 2) not null comment '积分金额',
    total_amount   decimal(10, 2) not null comment '支付金额',
    payment_status tinyint(4)     not null comment '支付状态',
    payment_type   tinyint(4)     not null comment '支付类型',
    `is_delete`    tinyint(4)     NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time`  datetime(0)    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime(0)    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '订单表'
  ROW_FORMAT = Dynamic;

create table `post_group`
(
    `id`          bigint(20)  not null auto_increment comment 'id',
    `post_id`     bigint(20)  not null comment '文章id',
    `group_id`    bigint(20)  not null comment '分组id',
    `is_delete`   tinyint(4)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX index_name (post_id, group_id)
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '帖子分组id'
  ROW_FORMAT = Dynamic;