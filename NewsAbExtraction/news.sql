CREATE TABLE news
(
  id       INT AUTO_INCREMENT
    PRIMARY KEY,
  title    VARCHAR(255) NULL,
  content  BLOB         NULL,
  keywords VARCHAR(127) NULL,
  summary  VARCHAR(511) NULL,
  time     INT          NULL,
  url      VARCHAR(255) NULL,
  CONSTRAINT news_id_uindex
  UNIQUE (id)
)
  ENGINE = InnoDB
  CHARSET = utf8;

