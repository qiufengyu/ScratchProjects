# -*- coding: utf-8 -*-
import scrapy


class ExSpider(scrapy.Spider):
    name = 'ex'
    allowed_domains = ['baidu.com']
    start_urls = ['http://baidu.com/']

    def parse(self, response):
        pass
