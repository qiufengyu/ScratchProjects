create table userdict_alvin
(
    id   int auto_increment
        primary key,
    word varchar(63) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO mail.userdict_alvin (id, word) VALUES (1, '广告');
INSERT INTO mail.userdict_alvin (id, word) VALUES (3, '美女');
INSERT INTO mail.userdict_alvin (id, word) VALUES (4, '你好');