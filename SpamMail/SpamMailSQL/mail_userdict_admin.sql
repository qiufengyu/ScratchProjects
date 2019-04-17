create table userdict_admin
(
    id   int auto_increment
        primary key,
    word varchar(63) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO mail.userdict_admin (id, word) VALUES (1, '超值');