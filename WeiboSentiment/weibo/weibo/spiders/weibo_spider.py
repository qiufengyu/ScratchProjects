import random
import sys
import time

import scrapy

from bs4 import BeautifulSoup
from scrapy.utils.project import get_project_settings
from selenium import webdriver
from selenium.common.exceptions import WebDriverException

from utils.text_util import *
from weibo.items import WeiboItem


class WeiboSpider(scrapy.Spider):
  name = 'weibo'
  start_urls = [
    'https://weibo.com/'
  ]
  settings = get_project_settings()

  def config_driver(self):
    # 这里使用 Chrome 浏览器，其他的推荐 Firefox 之类的
    # 去 https://sites.google.com/a/chromium.org/chromedriver/ 下载最新版即可（翻墙）
    # 360、QQ、sougou 之类的如果有对应的驱动也可以，但最好别用
    # window 环境，保持现在的配置即可
    # macOS 则需要指定 chromedriver 路径
    if sys.platform.lower().startswith('win'):
      # 这里附了一个可用的 windows 平台的驱动
      self.driver = webdriver.Chrome('./chromedriver')
    elif sys.platform.lower().startswith('darwin'):
      # macOS 指定驱动的路径
      self.driver = webdriver.Chrome('/usr/local/bin/chromedriver')
    else:
      print("Not supported platform")
      exit(-1)
    self.driver.set_page_load_timeout(1000)

  """
  这是需要登录的情况，需要填入自己的账号用户名和密码
  爬取的结果比较自然一些
  """
  def parse(self, response):
    self.config_driver()
    print("Open driver...")
    self.driver.get(response.url)
    time.sleep(10)
    username = self.driver.find_element_by_name('username')
    password = self.driver.find_element_by_name('password')
    login_btn = self.driver.find_element_by_css_selector('div.info_list.login_btn>a')
    # 填入自己申请的账号密码
    username.send_keys(self.settings['WEIBO_USERNAME'])
    time.sleep(2+random.random())
    password.send_keys(self.settings['WEIBO_PASSWORD'])
    time.sleep(2+random.random())
    try:
      login_btn.click()
    except Exception:
      print("登录失败，请填写正确的账号和密码，暂不支持输入验证码的情况！")
      exit(0)
    print("登陆成功！跳转到热门微博！")
    time.sleep(10+random.random())
    find_href = self.driver.find_element_by_css_selector('div.gn_nav>ul>li:nth-child(3)>a')
    find_href.click()
    print("加载热门微博中，等待刷新")
    time.sleep(10+10*random.random())
    # 此时，到达登陆后的热门微博页面，开始抓取数据
    page = 1
    while (page <= self.settings['WEIBO_SCROLLDOWN']):
      time.sleep(max(random.random(), random.gauss(1.0, 1.0)))
      print(f"下拉 {page} 次...")
      self.driver.execute_script("window.scrollBy(0,{})".format(
        max(0, int(random.gauss(200.0, 50.0)))))
      page += 1
      try:
        # 有的时候是需要点击这个查看更多的按钮的
        more_btn = self.driver.find_element_by_class_name('WB_cardmore')
        if more_btn:
          more_btn.click()
      except Exception:
        print("还未出现该标签...")

    # 一些显示全文的按钮，需要点击
    try:
      show_alls = self.driver.find_elements_by_css_selector('a.WB_text_opt')
    except WebDriverException:
      print("不存在显示全文的按钮")
    if show_alls:
      for show_all in show_alls:
        print(show_all)
        try:
          show_all.click()
          time.sleep(2+random.random())
        except WebDriverException:
          print("无法点击...")

    # 页面内容获取完毕，开始解析
    source = self.driver.page_source
    # 调用 BeautifulSoup 解析 html
    soup = BeautifulSoup(source, 'lxml')
    wb_feed = soup.find('div', 'WB_feed')
    feeds = wb_feed.find_all('div', 'WB_feed_detail')
    for feed in feeds:
      url = '#'
      wb_from_text = ''
      content = ''
      feed_detail = feed.find('div', 'WB_detail')
      wb_from = feed_detail.find('div', 'WB_from')
      wb_from_a = wb_from.find('a')
      if wb_from_a and wb_from_a.has_attr('href'):
        url = wb_from_a['href']
      # 如果没有获取到正确的 url，无法判断是否重复，所以就不管这一条微博了
      if len(url) < 2:
        continue
      wb_info = feed_detail.find('div', 'WB_info')
      if wb_info:
        if wb_info.text:
          wb_from_text = wb_info.text.strip()

      wb_text = feed_detail.find('div', 'WB_text')
      if wb_text:
        if wb_text.text:
          content = wb_text.text.strip()
        emojis = []
        item_emojis = wb_text.find_all('img', alt=True)
        if item_emojis:
          for item_emoji in item_emojis:
            if item_emoji and item_emoji.has_attr('alt'):
              emojis.append(item_emoji['alt'])
        weibo_item = WeiboItem()
        weibo_item['url'] = url
        # print(item_text, ''.join(emojis))
        content = remove_multi_space(content).strip()
        weibo_item['wb_from'] = wb_from_text
        weibo_item['content'] = content + ''.join(emojis)

        yield weibo_item


  """
  这也是一种做法，不登录，但是会出现一直等待加载的情况
  默认调用 parse 函数
  如果要使用不登录的情况，
  就把这个函数改成 parse，上面的函数改成其他名字
  """
  def parse_1(self, response):
    # 初始化 selenium 引擎，打开一个浏览器模拟人的行为
    # 避免被反爬虫
    self.config_driver()
    print("Open driver...")
    self.driver.get(response.url)
    # 让浏览器能获取足够的数据，加载完成
    time.sleep(20)  # Let the user actually see something!
    page = 1

    # 微博的首页热门微博通过下拉出现新的微博
    # 尽量模仿人的行为去浏览网页，间隔一段时间下拉
    # 下拉的距离也稍微随机一下
    # 偶尔出现网页加载不完全的情况，可以手动刷新一下
    # 可能和网络状况有关，有的时候正常浏览也加载不出来，大概是微博自身的问题
    while (page <= 30):
      time.sleep(max(random.random(), random.gauss(0.5, 1.0)))
      print(f"下拉 {page} 次...")
      self.driver.execute_script("window.scrollBy(0,{})".format(
        max(0, int(random.gauss(150.0, 25.0)))))
      page += 1

    source = self.driver.page_source
    # 调用 BeautifulSoup 解析 html
    soup = BeautifulSoup(source, 'lxml')
    container = soup.find('div', class_='UG_contents')
    items = container.find_all('div', class_=re.compile('UG_list_*'))
    print("此次共获取 {} 条记录".format(len(items)))
    for item_des in items:
      url = '#'
      wb_from = ''
      if item_des.has_attr('href'):
        url = item_des['href']
      elif item_des.find('div', 'vid'):
        if item_des.find('div', 'vid').has_attr('href'):
          url = item_des.find('div', 'vid')['href']
      if len(url) < 2:
        continue
      item = item_des.find('h3', class_=re.compile('list_title_*'))
      emojis = []
      if item:
        item_text = item.text.strip()
        item_emojis = item.find_all('img', alt=True)
        if item_emojis:
          for item_emoji in item_emojis:
            if item_emoji and item_emoji.has_attr('alt'):
              emojis.append(item_emoji['alt'])
        weibo_item = WeiboItem()
        weibo_item['url'] = url
        # print(item_text, ''.join(emojis))
        content = remove_multi_space(item_text).strip()
        weibo_item['content'] = content + ''.join(emojis)
        sub_info = item_des.find('span', 'subinfo')
        if sub_info and sub_info.text:
          wb_from = sub_info.text.strip()
        weibo_item['wb_from'] = wb_from
        yield weibo_item

    # 可以退出
    # self.driver.close()
