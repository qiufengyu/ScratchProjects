create table user_cathy
(
    id      int auto_increment
        primary key,
    `from`  varchar(63)   not null,
    `to`    varchar(63)   not null,
    subject varchar(255)  null,
    content blob          null,
    time    datetime      null,
    status  int default 0 null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO mail.user_cathy (id, `from`, `to`, subject, content, time, status) VALUES (1, 'cathy', 'alvin', '诚挚的问候', 0xE4BD99E4B880E58887E5AE89E5A5BDEFBC8CE58BBFE5BFB5E38082, '2018-12-14 19:34:52', 3);