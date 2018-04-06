from django.core.paginator import Paginator, PageNotAnInteger, EmptyPage
from django.shortcuts import render

# Create your views here.

from django.http import HttpResponse
from django.views import View

from newsCrawler.databaseUtil import DatabaseUtil


class IndexView(View):

  def __init__(self):
    self.db = DatabaseUtil()

  def get(self, request, *args, **kwargs):
    print(request)
    holder = ""
    items = []
    if 'keywords' in request.GET:
      keywords = request.GET.get('keywords', default='')
      holder = keywords
      items = self.db.selectNewsByKeywords(keywords)
    else:
      items = self.db.selectNewsByTime()
    per_page = 15
    paginator = Paginator(items, per_page)
    page = int(request.GET.get('page', default=1))
    try:
      items = paginator.page(page)
    except PageNotAnInteger:
      items = paginator.page(1)
    except EmptyPage:
      items = paginator.page(paginator.num_pages)
    return render(request, 'index.html', context={'items': items, 'paginator': paginator, 'holder': holder})

  def post(self, request, *args, **kwargs):
    pass

class NewsView(View):

  def __init__(self):
    self.db = DatabaseUtil()

  def get(self, request, *args, **kwargs):
    print(request)
    if 'id' in kwargs:
      id = int(kwargs['id'])
      newsItem = self.db.selectNewsById(id)
      if newsItem:
        return render(request, 'news.html', context={'item': newsItem})
    return render(request, 'news.html')

  def post(self, request, *args, **kwargs):
    return render(request, 'news.html')

