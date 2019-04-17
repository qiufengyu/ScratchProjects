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

INSERT INTO mail.user (username, nickname, password) VALUES ('admin', 'admin', 'admin');
INSERT INTO mail.user (username, nickname, password) VALUES ('alvin', 'Alvin', 'alvin');
INSERT INTO mail.user (username, nickname, password) VALUES ('bob', 'bob', 'bob');
INSERT INTO mail.user (username, nickname, password) VALUES ('cathy', 'cathy', 'cathy');
INSERT INTO mail.user (username, nickname, password) VALUES ('test', 'testname', '123456');