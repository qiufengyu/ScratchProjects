create table user_bob
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

INSERT INTO mail.user_bob (id, `from`, `to`, subject, content, time, status) VALUES (1, 'bob', 'alvin', '你好，alvin', 0xE68891E698AF626F62EFBC8CE5889DE69DA5E4B98DE588B0EFBC8CE8AFB7E5A49AE68C87E69599EFBC81, '2018-12-14 19:28:22', 3);