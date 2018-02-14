# -*- coding: utf-8 -*-

import scrapy
from scrapy.crawler import CrawlerProcess
from scrapy.utils.project import get_project_settings

from weibo.spiders.weibo_spider import WeiboSpider


if __name__ == '__main__':
  settings = get_project_settings()
  process = CrawlerProcess(get_project_settings())
  process.crawl(WeiboSpider)

  process.start()
