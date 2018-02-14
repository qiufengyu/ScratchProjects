-- auto-generated definition
CREATE TABLE weibo
(
  id        INT AUTO_INCREMENT
    PRIMARY KEY,
  url       VARCHAR(255) NULL,
  content   VARCHAR(511) NULL,
  wb_from   VARCHAR(61)  NULL,
  sentiment SMALLINT(6)  NULL,
  CONSTRAINT weibo_id_uindex
  UNIQUE (id),
  CONSTRAINT weibo_url_uindex
  UNIQUE (url)
)
  ENGINE = InnoDB
  CHARSET = utf8;

-- auto-generated definition
CREATE TABLE weibo_cand
(
  id      INT AUTO_INCREMENT
    PRIMARY KEY,
  url     VARCHAR(255) NULL,
  content VARCHAR(511) NULL,
  wb_from VARCHAR(31)  NULL,
  CONSTRAINT weibo_cand_id_uindex
  UNIQUE (id)
)
  ENGINE = InnoDB
  CHARSET = utf8;

