# Create your views here.
from django.core.paginator import Paginator, PageNotAnInteger, EmptyPage
from django.http import HttpResponse
from django.shortcuts import render, redirect
from django.views import View
from django.conf import settings
import pymysql
from snownlp import SnowNLP
from snownlp import sentiment

class IndexView(View):
  """
  建立与数据库的连接，分别是候选未标记的和已经标记的
  """
  db = pymysql.connect(
      host=settings.MYSQL_HOST,
      db=settings.MYSQL_DBNAME,
      user=settings.MYSQL_USER,
      passwd=settings.MYSQL_PASSWORD,
      charset='utf8mb4',
      use_unicode=True)
  cursor = db.cursor()

  # 一些数据库操作语句
  select_all_candidates = """
  SELECT * FROM `weibo_cand`;
  """

  select_all_weibos = """
  SELECT * FROM `weibo` WHERE `sentiment` = {} ORDER BY `id` DESC LIMIT 2000;
  """

  select_candidate_by_id = """
  SELECT * FROM `weibo_cand` WHERE `id` = {};
  """

  delete_candidate = """
  DELETE FROM `weibo_cand` WHERE `id` = {};  
  """

  delete_from_weibo_by_url = """
  DELETE FROM `weibo` WHERE `url` LIKE '{}';
  """

  check_weibo_by_url = """
  SELECT * FROM `weibo` WHERE `url` LIKE '{}';
  """

  insert_into_weibo = """
  INSERT INTO `weibo` (url, content, wb_from, sentiment) VALUES ('{}', '{}', '{}', {});
  """

  def get(self, request, *args, **kwargs):
    print(request)
    self.cursor.execute(self.select_all_candidates)
    ret = self.cursor.fetchall()
    if ret:
      weibo_items = []
      for r in ret:
        weibo_item = {}
        weibo_item['candid'] = int(r[0])
        weibo_item['url'] = r[1]
        weibo_item['content'] = r[2]
        weibo_item['wb_from'] = r[3]
        senti = SnowNLP(r[2])
        weibo_item['sentiment'] = senti.sentiments
        # print(weibo_item['sentiment'])
        weibo_items.append(weibo_item)
      per_page = 1
      paginator = Paginator(weibo_items, per_page)
      page = int(request.GET.get('page', default=1))
      try:
        weibos = paginator.page(page)
      except PageNotAnInteger:
        weibos = paginator.page(1)
      except EmptyPage:
        weibos = paginator.page(paginator.num_pages)
      return render(request, 'index.html', context={'weibos': weibos, 'paginator': paginator})
    return render(request, 'index.html', context={'empty_content': 1})

  def post(self, request):
    print(request.POST)
    cand_id = -1
    senti = -1
    if 'submit' in request.POST:
      for k in request.POST.keys():
        if k.startswith("group"):
          try:
            cand_id = int(k[5:])
            senti = int(request.POST.get(k))
          except ValueError:
            cand_id = -1
            senti = -1
      if cand_id > 0:
        self.cursor.execute(self.select_candidate_by_id.format(cand_id))
        cand_weibo = self.cursor.fetchone()
        if cand_weibo and senti >= 0:
          weibo_url = cand_weibo[1]
          weibo_content = cand_weibo[2]
          weibo_wb_from = cand_weibo[3]
          weibo_sentiment = senti
          self.cursor.execute(self.insert_into_weibo.format(weibo_url, weibo_content, weibo_wb_from, weibo_sentiment))
          self.cursor.execute(self.delete_candidate.format(cand_id))
          self.db.commit()
      return redirect('index')
    elif 'refresh' in request.POST:
      print('refresh')
      with open('neg_updated.txt', 'w', encoding='utf-8') as neg_writer:
        with open('neg.txt', 'r', encoding='utf-8') as neg_reader:
          while True:
            line = neg_reader.readline()
            if not line:
              break
            line = line.strip()
            neg_writer.write(line+'\n')
        self.cursor.execute(self.select_all_weibos.format(0))
        neg_results = self.cursor.fetchall()
        for neg_result in neg_results:
          neg_writer.write(neg_result[2].strip()+'\n')
      with open('pos_updated.txt', 'w', encoding='utf-8') as pos_writer:
        with open('pos.txt', 'r', encoding='utf-8') as pos_reader:
          while True:
            line = pos_reader.readline()
            if not line:
              break
            line = line.strip()
            pos_writer.write(line+'\n')
        self.cursor.execute(self.select_all_weibos.format(1))
        pos_results = self.cursor.fetchall()
        for pos_result in pos_results:
          pos_writer.write(pos_result[2].strip()+'\n')
      print("开始训练新模型...")
      sentiment.train('neg_updated.txt', 'pos_updated.txt')
      sentiment.save('sentiment.marshal')
      print("训练完成！")
      return redirect('index')

class AnalysisView(View):
  def get(self, request, *args, **kwargs):
    return render(request, 'analysis.html')

  def post(self, request, *args, **kwargs):
    print(request.POST)
    weibo_text = request.POST.get('weiboText', '')
    weibo_text = weibo_text.strip()
    senti = SnowNLP(weibo_text).sentiments
    if senti > 0.5:
      senti_value = "正向情感"
    else:
      senti_value = "负向情感"
    return render(request, 'analysis.html', context={'holder': weibo_text, 'res': 1, 'senti': senti_value})


