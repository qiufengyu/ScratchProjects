from django.core.paginator import Paginator, PageNotAnInteger, EmptyPage
from django.shortcuts import render

# Create your views here.
from django.http import HttpResponse

from db_tools import DatabaseUtil
from WebSpider.settings import logger
db = DatabaseUtil()


def index(request):
  holder = ""
  items = []
  try:
    items = db.selectNewsByTime()
    logger.info("主页根据时间获取新闻")
  except:
    items = []
  # 每页展示 15 项，可以翻页访问
  per_page = 15
  paginator = Paginator(items, per_page)
  page = int(request.GET.get('page', default=1))
  logger.debug("展示第 {} 页内容".format(page))
  try:
    items = paginator.page(page)
  except PageNotAnInteger:
    items = paginator.page(1)
  except EmptyPage:
    items = paginator.page(paginator.num_pages)
    logger.error("数据库查找错误！")
  return render(request, 'index.html', context={'items': items, 'paginator': paginator, 'holder': holder})


def search(request):
  res = {}
  # 是否是第一次加载
  res["flag"] = 0
  if request.method == "POST":
    post_req = request.POST
    if 'inputkeys' in post_req:
      keywords = request.POST.get('inputkeys', default='')
    holder = keywords
    res["plhd"] = holder
    logger.info("获取含有关键词<{}>的内容")
    raw_items = db.selectNewsByKeywords(keywords)
    per_page = 15
    paginator = Paginator(raw_items, per_page)
    page = int(request.GET.get('page', default=1))
    logger.debug("展示第 {} 页内容".format(page))
    try:
      items = paginator.page(page)
    except PageNotAnInteger:
      items = paginator.page(1)
    except EmptyPage:
      items = paginator.page(paginator.num_pages)
      logger.error("数据库查找错误！")
    res["items"] = items
    res["flag"] = 1
  return render(request, "search.html", context=res)


def news(request, id=None):
  if id:
    id_attempt = int(id)
    if id_attempt > 0:
      newsItem = db.selectNewsById(id)
      if newsItem:
        return render(request, 'news.html', context={'item': newsItem})
    return render(request, 'news.html')

def about(request):
  return render(request, "about.html")
