# -*- coding: utf-8 -*-

# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://doc.scrapy.org/en/latest/topics/item-pipeline.html
import logging
import pymysql

from scrapy.utils.project import get_project_settings
from snownlp import SnowNLP

from weibo.items import WeiboItem


logger = logging.getLogger(__name__)


class WeiboPipeline(object):
  def process_item(self, item, spider):
    return item

"""
数据库插入，修改 mysql 中关于中文的编码
在命令行中使用
ALTER TABLE `weibo` CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE `weibo` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE `weibo` CHANGE `content` `content` VARCHAR(511) CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE `weibo` CHANGE `content` `content` VARCHAR(511) CHARACTER SET utf8 COLLATE utf8_general_ci;
"""
class MySQLPipeline(object):
  def __init__(self):
    self.settings = get_project_settings()
    self.connect = pymysql.connect(
      host=self.settings['MYSQL_HOST'],
      db=self.settings['MYSQL_DBNAME'],
      user=self.settings['MYSQL_USER'],
      passwd=self.settings['MYSQL_PASSWORD'],
      charset='utf8mb4',
      use_unicode=True)
    self.cursor = self.connect.cursor()
    self.check_statement = """
    SELECT * FROM `weibo` WHERE `url` LIKE '{}';
    """
    self.check_cand_statement = """
        SELECT * FROM `weibo_cand` WHERE `url` LIKE '{}';
        """
    self.insert_statement = """
    INSERT INTO `weibo_cand` (url, content, wb_from) VALUES ('{}', '{}', '{}');
    """

  def process_item(self, item, spider):
    if isinstance(item, WeiboItem):
      try:
        self.cursor.execute(self.check_statement.format(item['url']))
        ret = self.cursor.fetchone()
        self.cursor.execute(self.check_cand_statement.format(item['url']))
        ret2 = self.cursor.fetchone()
        # 如果已经存在，忽略
        # 不存在时，插入
        if ret or ret2:
          logger.warning("已存在重复内容，跳过")
        else:
          # snow_text = SnowNLP(item['content'])
          # print('='*80)
          # print(item['content'])
          # if snow_text.sentiments >= 0.5:
            # print("正向情感")
          # else:
            # print("负向情感")
          if item['url'].startswith('//'):
            item['url'] = item['url'][2:]
          self.cursor.execute(self.insert_statement.format(item['url'], item['content'], item['wb_from']))
          self.connect.commit()
          # logger.info("插入成功")
      except Exception as error:
        logger.warning(error)

    return item