# 利用爬虫，从新浪滚动新闻获取内容
import json
import logging
import random
import re
import time
import urllib.request
from queue import Queue
from threading import Thread

from bs4 import BeautifulSoup
from pymysql import IntegrityError
from snownlp import SnowNLP

from user_agents import agents
from db_tools import DatabaseUtil

from WebSpider.settings import logger

# 一些常量
ROOT_URL = "https://feed.mix.sina.com.cn/api/roll/get?pageid=153&lid=2509&k=&num=50&page={}"
REFERER = "https://news.sina.com.cn/roll/"
HEADERS = len(agents) - 1
db = DatabaseUtil()

def get_urls(manager, start=1, end=2):
  for i in range(start, end):
    time.sleep(5)
    current_url = ROOT_URL.format(i)
    req = urllib.request.Request(current_url)
    # 建立到 current_url 的网络连接，获取信息
    response = urllib.request.urlopen(req, timeout=5)
    try:
      response_read = response.read().decode("utf-8")
    except:
      response_read = response.read().decode("gbk")
    data_str = json.loads(response_read)
    result_json = data_str['result']['data']
    for r in result_json:
      # print(r)
      if "url" in r:
        try:
          manager.add_job(get_content, r["url"], r)
        except:
          logger.error("网页访问错误，重试其他内容。")

def get_content(args):
  """
  根据网页的链接，访问具体内容，并保存至数据库
  :param url:
  :param raw:
  :return:
  """
  # print(url)
  url = args[0]
  raw = args[1]
  logger.info("访问 {}".format(url))
  header_id = random.randint(0, HEADERS)
  random_header = agents[header_id]
  req = urllib.request.Request(url)
  # 添加浏览器访问头，一点点反爬虫小技巧
  req.add_header("User-Agent", random_header)
  req.add_header("Referer", REFERER)
  response = urllib.request.urlopen(req, timeout=10)
  content = response.read().decode("utf-8")
  if response.getcode() == 200:
    soup = BeautifulSoup(content, 'lxml')
    # 记录网页，并提取其中的内容
    item = {}
    item['url'] = url
    item['time'] = int(raw["mtime"]) if "mtime" in raw else 0
    soup_title = soup.find('div', 'main-title')
    if soup_title:
      title_text = soup_title.text.strip()
    else:
      title_text = raw["title"].strip()
    item['title'] = title_text
    # 正文，新闻的正文有的在 artibody 有的在 article 的 div id 中
    content_text = []
    artibody = soup.find('div', {'id': 'artibody'})
    if artibody:
      paras = artibody.find_all(re.compile('^[pd]'))
      for para in list(paras):
        parent_node = para.parent.attrs
        if 'id' in parent_node.keys():
          if parent_node['id'] == 'artibody':
            if para.name == 'p':
              para_text = para.text.strip()
              if len(para_text) > 0:
                content_text.append(para_text)
    article = soup.find("div", {"id": "article"})
    if article:
      paras = article.find_all(re.compile('^[pd]'))
      for para in list(paras):
        parent_node = para.parent.attrs
        if 'id' in parent_node.keys():
          if parent_node['id'] == 'article':
            if para.name == 'p':
              para_text = para.text.strip()
              if len(para_text) > 0:
                content_text.append(para_text)
    # 此时，如果有具体的正文内容，则通过 textrank 算法，提取内容的关键词、摘要
    # 当然，新浪的新闻有时在网页中已经蕴含了关键词、摘要
    if len(content_text) > 0:
      text_all = "\n".join(content_text)
      s = SnowNLP(text_all)
      item["content"] = text_all
      if "keywords" in raw and len(raw["keywords"]) > 1:
        item["keywords"] = raw["keywords"]
      else:
        gen_keywords = s.keywords(5)
        item["keywords"] = ",".join(gen_keywords)
      if "intro" in raw and len(raw["intro"]) > 5:
        item["summary"] = raw["intro"]
      else:
        gen_summary = s.summary(3)
        item["summary"] = ",".join(gen_summary)
      # print(item)
      # 此后，插入数据库中
      try:
        db.insert(item)
        # print(item)
        logger.info("插入新闻《{}》".format(item["title"]))
      except IntegrityError:
        logger.warning("新闻《{}》已经存在，忽略".format(item["title"]))
    else:
      logger.warning("新闻内容为空或不存在，跳过。")
  else:
    logger.warning("网页访问错误！")

# 使用基于内存池的多线程的方式运行爬虫
# 没有直接用 python 中的 concurrent.futures.ThreadPoolExecutor
# 手动实现了类似的机制
class WorkManager():
  def __init__(self):
    self.work_queue = Queue()
    self.threads = []

  def init_thread_pool(self, thread_num):
    for i in range(thread_num):
      self.threads.append(Work(self.work_queue))

  # 在队伍中添加一项工作
  def add_job(self, func, *args):
    self.work_queue.put((func, list(args)))

  # 检查队列中剩余的任务
  def check_queue(self):
    return self.work_queue.qsize()

  # 等待所有的线程运行完毕
  def wait_all(self):
    for item in self.threads:
      if item.isAlive():
        item.join()

class Work(Thread):
  def __init__(self, work_queue: Queue):
    super().__init__()
    self.work_queue = work_queue
    self.start()

  def run(self):
    while True:
      time.sleep(5)
      try:
        do, args = self.work_queue.get(block=False)
        do(args)
        self.work_queue.task_done()
      except Exception as e:
        logger.error("多任务执行错误，{}".format(e.__str__()))
        break

if __name__ == "__main__":
  logger.info("开启内存池多线程爬虫。")
  work_manager = WorkManager()
  get_urls(work_manager)
  work_manager.init_thread_pool(thread_num=2)
  work_manager.wait_all()
  logger.info("爬虫完毕")

