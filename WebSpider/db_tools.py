import datetime
import re

import pymysql

from WebSpider.settings import logger

# 需要设置数据库使用 utf-8 mb4 的编码，否则插入失败
# alter table TABLE_NAME convert to character set utf8mb4 collate utf8mb4_bin

class DatabaseUtil():
  """
  数据库操作相关：
  1. 新闻存储
  2. 新闻查询
  """
  # 数据库中插入新闻
  insertSQL = """
              INSERT INTO `news` (`title`, `content`, `keywords`, `summary`, `time`, `url`) VALUES 
              ('{}', '{}', '{}', '{}', {}, '{}');
  """
  # 检查数据库中是否已经存在新闻，但在插入新闻时已经对 url 进行了唯一性限制
  checkSQL = """
             SELECT * FROM `news` WHERE `url` LIKE '{}';
  """
  # 根据 id，检查/查找数据库中是否已经存在新闻
  selectSQL = """
             SELECT * FROM `news` WHERE `id` = {};
  """
  # 根据时间降序排列
  selectByTimeDesc = """
      SELECT * FROM `news` ORDER BY `time` DESC LIMIT 1000;
      """
  selectByKeywords = """
      SELECT * FROM `news` WHERE {} ORDER BY `time` DESC;
      """

  tz_utc_8 = datetime.timezone(datetime.timedelta(hours=8))

  def __init__(self):
    self.connection = pymysql.connect(host='localhost',
                                 user='root',
                                 password='root',
                                 db='webs',
                                 charset='utf8mb4')

  def insert(self, entity: dict):
    # print(entity)
    if entity['url']:
      with self.connection.cursor() as cursor:
        title = entity['title'] if entity['title'] else ''
        url = entity['url'] if entity['url'] else ''
        content = entity['content'] if entity['content'] else ''
        time = int(entity['time']) if entity['time'] else 0
        keywords = entity['keywords'] if entity['keywords'] else ''
        summary = entity['summary'] if entity['summary'] else ''
        try:
          cursor.execute(self.insertSQL.format(title, pymysql.escape_string(content), keywords, summary, time, url))
          self.connection.commit()
        except Exception as e:
          logger.error("数据库插入错误,{}！请检查！".format(e.__str__()))
          self.connection.rollback()

  def selectNewsById(self, id) -> dict:
    res = {}
    with self.connection.cursor() as cursor:
      cursor.execute(self.selectSQL.format(id))
      r = cursor.fetchone()
      if r:
        res['id'] = r[0]
        res['title'] = r[1]
        res['content'] = r[2].decode('utf-8').split('\n')
        res['keywords'] = r[3]
        res['summary'] = r[4]
        timeInt = int(r[5])
        time = datetime.datetime.fromtimestamp(timeInt, tz=datetime.timezone.utc) + datetime.timedelta(hours=8)
        res['time'] = time.strftime('%Y-%m-%d %H:%M')
        res['url'] = r[6]
    return res

  def selectNewsByTime(self):
    res = []
    with self.connection.cursor() as cursor:
      cursor.execute(self.selectByTimeDesc)
      rs = cursor.fetchall()
      if rs:
        for r in rs:
          item = {}
          item['id'] = r[0]
          item['title'] = r[1]
          item['keywords'] = r[3]
          item['summary'] = r[4]
          timeInt = int(r[5])
          time = datetime.datetime.fromtimestamp(timeInt, tz=datetime.timezone.utc) + datetime.timedelta(hours=8)
          item['time'] = time.strftime('%Y-%m-%d %H:%M')
          item['url'] = r[6]
          res.append(item)
    return res

  def selectNewsByKeywords(self, keywords):
    res = []
    keywordSplit = re.split(r'[,\W，]', keywords)
    print(keywordSplit)
    keywordSQL = "`keywords` LIKE '{}'"
    keywordList = []
    for k in keywordSplit:
      keywordList.append(keywordSQL.format('%'+k+'%'))
    finalKeywordSQL = ' OR '.join(keywordList)
    print(self.selectByKeywords.format(finalKeywordSQL))
    with self.connection.cursor() as cursor:
      cursor.execute(self.selectByKeywords.format(finalKeywordSQL))
      rs = cursor.fetchall()
      if rs:
        for r in rs:
          item = {}
          item['id'] = r[0]
          item['title'] = r[1]
          item['keywords'] = r[3]
          item['summary'] = r[4]
          timeInt = int(r[5])
          # 转换为北京时间
          time = datetime.datetime.fromtimestamp(timeInt, tz=datetime.timezone.utc) + datetime.timedelta(hours=8)
          item['time'] = time.strftime('%Y-%m-%d %H:%M')
          item['url'] = r[6]
          res.append(item)
    return res
