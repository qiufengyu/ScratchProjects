import json
import pprint
import random
import time
import urllib
import requests

from bs4 import BeautifulSoup
from snownlp import SnowNLP

# 一些全局变量
# 模拟浏览器的访问的信息
from databaseUtil import DatabaseUtil

headers = {
    'Content-Type': 'text/html',
    'User-Agent': "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36",
    'Referer': "http://roll.news.sina.com.cn/s/channel.php?ch=01",
    'Accept-Language': "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7",
    'Accept-Encoding': "gzip, deflate",
    'Host': "roll.news.sina.com.cn",
}
# 获取新闻的入口
newsUrl = "http://roll.news.sina.com.cn/interface/rollnews_ch_out_interface.php"
# 获取新闻链接中需要的一些参数
globalParams = {
  'col': '89',
  'spec': '',
  'type': '',
  'ch': '01',
  'k': '',
  'offset_page': '0',
  'offset_num': '0',
  'num': '60',
  'asc': '',
}

# 帮助解析 JSON 的辅助类
class Dummy(dict):
  def __getitem__(self, item):
    return item

class NewsCrawler():
  def __init__(self):
    # 初始化一个数据库对象
    self.dbUtil = DatabaseUtil()

  # 根据新闻的 url，获取新闻的具体内容
  def getNewsContent(self, url: str):
    content = []
    req = urllib.request.Request(url=url)
    response = urllib.request.urlopen(req, timeout=5)
    try:
      response_read = response.read().decode('utf-8')
    except:
      response_read = response.read().decode('gbk')
    soup = BeautifulSoup(response_read, 'lxml')
    article = soup.select_one('div.article')
    if not article:
      article = soup.select_one('div#artibody')
    paras = article.select('p')
    if paras:
      for para in paras:
        line = para.text.strip()
        if len(line) >= 1:
          content.append(line)
    return content

  # Python3 支持的指定参数形式
  def parseNewsDetail(self, r: dict):
    entity = {}
    # 新闻标题
    entity['title'] = r['title']
    print(entity['title'])
    # 新闻的 url，进一步获取新闻的具体内容，从而生成关键字、摘要
    entity['url'] = r['url']
    print(entity['url'])
    # 新闻的时间
    timeInt = int(r['time'])
    entity['time'] = timeInt
    # 新闻的具体文本内容
    content = self.getNewsContent(entity['url'])
    contentConcat = ''
    if len(content) >= 1:
      contentConcat = '\n'.join(content)
    entity['content'] = contentConcat
    # 使用 SnowNLP 提供的生成关键词、提取摘要的接口
    # 具体算法是 TextRank，毕业论文需要具体介绍这种方法
    if contentConcat:
      s = SnowNLP(contentConcat)
      # 生成关键词，因为有的时候关键词是一个单字（常用字），所以只保留长度大于等于 2 的
      keywords10 = s.keywords(10)
      keywords = [x for x in keywords10 if (len(x) > 1 and self.validKeywords(x))]
      keywordsSQL = '|'.join(keywords)
      entity['keywords'] = keywordsSQL
      # 生成一个三句话的摘要
      summary = s.summary(3)
      summarySQL = '|'.join(summary)
      entity['summary'] = summarySQL
      # print(keywordsSQL)
      # print(summarySQL)
      # print('='*80)
      # 写入数据库中
      self.dbUtil.insert(entity)

  def getSinaRollNews(self, startPage, endPage):
    for i in range(startPage, endPage):
      print("Page {}".format(i))
      params = globalParams.copy()
      params['page'] = str(i)
      allHtml = requests.get(newsUrl, params=params, headers=headers)
      pageHtml = allHtml.content.decode('gbk')
      pageHtml = pageHtml[pageHtml.index('{'):-1]
      # 解析获取到的 JSON 格式的新闻列表
      data_str = eval(pageHtml, Dummy())
      data_str = json.dumps(data_str)
      data_str = json.loads(data_str)
      data_str = data_str['list']
      for r in data_str:
        # 对每一个具体的新闻都进行解析，并且写入数据库
        self.parseNewsDetail(r)
        # 稍微暂停一下，避免爬取频率太高
        time.sleep(random.random()*6.0)
      time.sleep(10)

  # 有时候得到的关键词是一些标点符号、纯数字等，所以过滤掉
  def validKeywords(self, x: str):
    if ('—' in x) or ('：') in x or ('"' in x) or \
        ('，' in x) or ('。' in x) or (x.isdigit()):
      return False
    else:
      return True

if __name__ == '__main__':
  nc = NewsCrawler()
  # 获取页面[a,b)，不包括第 b 页，自己设置
  nc.getSinaRollNews(1, 5)
  # 下面是测试一下能否从数据库中查找到新闻
  # db = DatabaseUtil()
  # db.selectNewsByKeywords('今天')
  # res = db.selectNewsByKeywords('也许，真确，美国')
  # for x in res:
    # pprint.pprint(x)
  # rs = db.selectNewsById(1)
  # pprint.pprint(rs)


