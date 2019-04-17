create table user
(
    username varchar(63)  not null,
    nickname varchar(63)  null,
    password varchar(127) null,
    constraint user_username_uindex
        unique (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

alter table user
    add primary key (username);

INSERT INTO messages.user (username, nickname, password) VALUES ('admin', 'admin', 'admin');
INSERT INTO messages.user (username, nickname, password) VALUES ('alvin', 'alvin', 'alvin');
INSERT INTO messages.user (username, nickname, password) VALUES ('bob', 'bob', 'bob');
INSERT INTO messages.user (username, nickname, password) VALUES ('cathy', 'cathy', 'cathy');
INSERT INTO messages.user (username, nickname, password) VALUES ('test', 'testname', '123456');
create table user_admin
(
    id      int auto_increment,
    `from`  varchar(63)  not null,
    `to`    varchar(63)  not null,
    subject varchar(255) null,
    time    datetime     null,
    status  int          not null,
    constraint user_admin_id_uindex
        unique (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

alter table user_admin
    add primary key (id);

INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (1, 'alvin', 'admin', '主题admin', '2018-11-10 11:51:52', 2);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (2, 'admin', 'alvin', '来自admin', '2018-09-10 11:53:18', 3);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (3, 'admin', 'alvin', '这是删除的短信', '2018-09-15 11:57:02', 3);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (4, 'alvin', 'admin', '你好，管理员', '2018-12-11 15:43:13', 1);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (5, 'admin', 'alvin', '向佐与向太默契满分，母子间的感情煞羡旁人', '2018-12-11 20:26:34', 3);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (6, 'admin', 'alvin', '深交所拟对长生生物强制退市，哪些投资者可以索赔？', '2018-12-11 20:27:02', 3);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (7, 'alvin', 'admin', '电影行业迈入新时代 逐步成熟走向良性发展', '2018-12-12 20:49:20', 1);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (8, 'admin', 'alvin', '垃圾短信测试', '2018-12-14 15:59:36', 3);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (9, 'admin', 'alvin', '贝叶斯测试', '2018-12-14 16:00:04', 3);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (10, 'admin', 'alvin', '测试关键词垃圾短信', '2018-12-14 19:37:52', 3);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (11, 'admin', 'alvin', '正常短信测试', '2018-12-14 19:38:17', 3);
INSERT INTO messages.user_admin (id, `from`, `to`, subject, time, status) VALUES (12, 'alvin', 'admin', '模糊匹配，结果不好', '2018-12-14 19:40:20', 2);
create table user_alvin
(
    id      int auto_increment
        primary key,
    `from`  varchar(63)   not null,
    `to`    varchar(63)   not null,
    subject varchar(255)  null,
    time    datetime      null,
    status  int default 0 null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (1, 'alvin', 'test', '主题一', '2018-12-10 11:51:12', 3);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (2, 'alvin', 'admin', '主题admin', '2018-11-10 11:51:52', 3);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (4, 'admin', 'alvin', '来自admin', '2018-09-10 11:53:18', 1);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (6, 'test', 'alvin', '垃圾短信测试', '2018-12-01 11:54:34', 2);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (7, 'test', 'alvin', '垃圾短信测试', '2018-09-10 11:54:56', 1);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (8, 'test', 'alvin', '真的垃圾短信测试', '2018-10-10 11:55:57', 2);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (9, 'test', 'alvin', '真的垃圾短信测试', '2018-05-13 11:56:21', 2);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (10, 'admin', 'alvin', '这是删除的短信', '2018-09-15 11:57:02', 1);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (13, 'alvin', 'alvin', '写给自己的信', '2018-12-11 19:16:47', 4);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (14, 'admin', 'alvin', '向佐与向太默契满分，母子间的感情煞羡旁人', '2018-12-11 20:26:34', 2);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (15, 'admin', 'alvin', '深交所拟对长生生物强制退市，哪些投资者可以索赔？', '2018-12-11 20:27:02', 1);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (16, 'test', 'alvin', '被高数虐是一种怎样的体验？', '2018-12-11 21:07:28', 2);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (17, 'alvin', 'admin', '电影行业迈入新时代 逐步成熟走向良性发展', '2018-12-12 20:49:20', 3);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (18, 'admin', 'alvin', '垃圾短信测试', '2018-12-14 15:59:36', 4);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (19, 'admin', 'alvin', '贝叶斯测试', '2018-12-14 16:00:04', 1);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (21, 'cathy', 'alvin', '诚挚的问候', '2018-12-14 19:34:52', 1);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (22, 'admin', 'alvin', '测试关键词垃圾短信', '2018-12-14 19:37:52', 4);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (23, 'admin', 'alvin', '正常短信测试', '2018-12-14 19:38:17', 2);
INSERT INTO messages.user_alvin (id, `from`, `to`, subject, time, status) VALUES (24, 'alvin', 'admin', '模糊匹配，结果不好', '2018-12-14 19:40:20', 3);
create table user_bob
(
    id      int auto_increment
        primary key,
    `from`  varchar(63)   not null,
    `to`    varchar(63)   not null,
    subject varchar(255)  null,
    time    datetime      null,
    status  int default 0 null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO messages.user_bob (id, `from`, `to`, subject, time, status) VALUES (1, 'bob', 'alvin', '你好，alvin', '2018-12-14 19:28:22', 3);
create table user_cathy
(
    id      int auto_increment
        primary key,
    `from`  varchar(63)   not null,
    `to`    varchar(63)   not null,
    subject varchar(255)  null,
    time    datetime      null,
    status  int default 0 null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO messages.user_cathy (id, `from`, `to`, subject, time, status) VALUES (1, 'cathy', 'alvin', '诚挚的问候', '2018-12-14 19:34:52', 3);
create table user_test
(
    id      int auto_increment
        primary key,
    `from`  varchar(63)   not null,
    `to`    varchar(63)   not null,
    subject varchar(255)  null,
    time    datetime      null,
    status  int default 0 null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO messages.user_test (id, `from`, `to`, subject, time, status) VALUES (1, 'alvin', 'test', '主题一', '2018-12-10 11:51:12', 2);
INSERT INTO messages.user_test (id, `from`, `to`, subject, time, status) VALUES (2, 'test', 'alvin', '来自test', '2018-12-06 11:52:16', 3);
INSERT INTO messages.user_test (id, `from`, `to`, subject, time, status) VALUES (3, 'test', 'alvin', '垃圾短信测试', '2018-12-07 11:53:59', 6);
INSERT INTO messages.user_test (id, `from`, `to`, subject, time, status) VALUES (4, 'test', 'alvin', '垃圾短信测试', '2018-12-01 11:54:34', 3);
INSERT INTO messages.user_test (id, `from`, `to`, subject, time, status) VALUES (5, 'test', 'alvin', '垃圾短信测试', '2018-09-10 11:54:56', 3);
INSERT INTO messages.user_test (id, `from`, `to`, subject, time, status) VALUES (6, 'test', 'alvin', '真的垃圾短信测试', '2018-10-10 11:55:57', 3);
INSERT INTO messages.user_test (id, `from`, `to`, subject, time, status) VALUES (7, 'test', 'alvin', '真的垃圾短信测试', '2018-05-13 11:56:21', 3);
INSERT INTO messages.user_test (id, `from`, `to`, subject, time, status) VALUES (8, 'test', 'alvin', '被高数虐是一种怎样的体验？', '2018-12-11 21:07:28', 3);
create table userdict_admin
(
    id   int auto_increment
        primary key,
    word varchar(63) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO messages.userdict_admin (id, word) VALUES (1, 'alvin');
create table userdict_alvin
(
    id   int auto_increment
        primary key,
    word varchar(63) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO messages.userdict_alvin (id, word) VALUES (1, 'admin');
create table userdict_bob
(
    id   int auto_increment
        primary key,
    word varchar(63) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


create table userdict_cathy
(
    id   int auto_increment
        primary key,
    word varchar(63) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


create table userdict_test
(
    id   int auto_increment
        primary key,
    word varchar(63) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


create table userlog
(
    id      int auto_increment
        primary key,
    `from`  varchar(63)   not null,
    `to`    varchar(63)   not null,
    subject varchar(255)  null,
    time    datetime      null,
    status  int default 0 null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (1, 'alvin', 'test', '主题一', '2018-12-10 11:51:12', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (2, 'alvin', 'admin', '主题admin', '2018-11-10 11:51:52', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (3, 'test', 'alvin', '来自test', '2018-12-06 11:52:16', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (4, 'admin', 'alvin', '来自admin', '2018-09-10 11:53:18', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (5, 'test', 'alvin', '垃圾短信测试', '2018-12-07 11:53:59', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (6, 'test', 'alvin', '垃圾短信测试', '2018-12-01 11:54:34', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (7, 'test', 'alvin', '垃圾短信测试', '2018-09-10 11:54:56', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (8, 'test', 'alvin', '真的垃圾短信测试', '2018-10-10 11:55:57', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (9, 'test', 'alvin', '真的垃圾短信测试', '2018-05-13 11:56:21', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (10, 'admin', 'alvin', '这是删除的短信', '2018-09-15 11:57:02', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (11, 'alvin', 'admin', '你好，管理员', '2018-12-11 15:43:13', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (12, 'alvin', 'alvin', '写给自己的信', '2018-12-11 19:16:47', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (13, 'admin', 'alvin', '向佐与向太默契满分，母子间的感情煞羡旁人', '2018-12-11 20:26:34', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (14, 'admin', 'alvin', '深交所拟对长生生物强制退市，哪些投资者可以索赔？', '2018-12-11 20:27:02', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (15, 'test', 'alvin', '被高数虐是一种怎样的体验？', '2018-12-11 21:07:28', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (16, 'alvin', 'admin', '电影行业迈入新时代 逐步成熟走向良性发展', '2018-12-12 20:49:20', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (17, 'admin', 'alvin', '垃圾短信测试', '2018-12-14 15:59:36', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (18, 'admin', 'alvin', '贝叶斯测试', '2018-12-14 16:00:04', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (19, 'bob', 'alvin', '你好，alvin', '2018-12-14 19:28:22', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (20, 'cathy', 'alvin', '诚挚的问候', '2018-12-14 19:34:52', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (21, 'admin', 'alvin', '测试关键词垃圾短信', '2018-12-14 19:37:52', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (22, 'admin', 'alvin', '正常短信测试', '2018-12-14 19:38:17', 1);
INSERT INTO messages.userlog (id, `from`, `to`, subject, time, status) VALUES (23, 'alvin', 'admin', '模糊匹配，结果不好', '2018-12-14 19:40:20', 1);