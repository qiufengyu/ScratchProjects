/* 数据库 */

/* 新闻表 */
CREATE TABLE news.news
(
  id int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  title varchar(255),
  url varchar(255),
  category int(11),
  timestamp int(11),
  content blob
);
CREATE UNIQUE INDEX news_id_uindex ON news.news (id);
CREATE UNIQUE INDEX news_url_uindex ON news.news (url);


/* 权限种类表 */
CREATE TABLE news.role
(
  role_id INT AUTO_INCREMENT
    PRIMARY KEY,
  role    VARCHAR(255) NULL
)
ENGINE = InnoDB;
INSERT INTO news.role (role_id, role) VALUES (1, 'ADMIN');
INSERT INTO news.role (role_id, role) VALUES (2, 'USER');

/* 用户表 */
CREATE TABLE news.user
(
  user_id int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  username varchar(31) NOT NULL,
  password varchar(255) NOT NULL,
  email varchar(127) NOT NULL,
  enabled int(11)
);

INSERT INTO news.user (user_id, username, password, email, enabled) VALUES (4, 'admin', '$2a$10$nxYPa5/nlhruC/tZjhOpo.h/zR6TW5HYcglLJf7hvR2lXAWuuW2hm', 'admin@admin.com', 1);
INSERT INTO news.user (user_id, username, password, email, enabled) VALUES (5, 'user', '$2a$10$wOGgeFeIiO5KX2005P64S.dZJiij3C/34c7vrS/i6d8aMjwj0psX.', '123456@qq.com', 1);
INSERT INTO news.user (user_id, username, password, email, enabled) VALUES (6, 'user1', '$2a$10$1..Q2/lYYuekyIgW4Km/..BuwE2xiDMlJerUQg6Ym6G0OgAIX99Ci', '123456789@qq.com', 1);
INSERT INTO news.user (user_id, username, password, email, enabled) VALUES (7, 'user2', '$2a$10$XXW0SY5XuJQbkrdd7uDpQ.Zlw71HI3msfC8gymStDsd0GHnHrJc9e', '23432@dfs.com', 1);
INSERT INTO news.user (user_id, username, password, email, enabled) VALUES (25, 'userddddd', '$2a$10$.1L5ZHrD7NY8lbsKJCpcwu7A.NP0L7osLiq2JfAckZu4jEt0ZH0na', 'fdsfs@wedd.com', 1);

/* 用户-权限 表 */
CREATE TABLE news.user_role
(
  user_id int(11) NOT NULL,
  role_id int(11) NOT NULL,
  CONSTRAINT user_role_user_user_id_fk FOREIGN KEY (user_id) REFERENCES user (user_id),
  CONSTRAINT user_role_role_role_id_fk FOREIGN KEY (role_id) REFERENCES role (role_id)
);
CREATE INDEX user_role_user_user_id_fk ON news.user_role (user_id);
CREATE INDEX user_role_role_role_id_fk ON news.user_role (role_id);
INSERT INTO news.user_role (user_id, role_id) VALUES (6, 2);
INSERT INTO news.user_role (user_id, role_id) VALUES (4, 1);
INSERT INTO news.user_role (user_id, role_id) VALUES (5, 2);
INSERT INTO news.user_role (user_id, role_id) VALUES (7, 2);
INSERT INTO news.user_role (user_id, role_id) VALUES (25, 2);
