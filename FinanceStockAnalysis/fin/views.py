import pymysql
from django.shortcuts import render

# Create your views here.
from django.views import View
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import authentication, permissions
from django.contrib.auth.models import User

from analysis import analysis
from util import utils

class HomeView(View):
  def get(self, request, *args, **kwargs):
    return render(request, 'index.html')

class GetData(APIView):
    authentication_classes = []
    permission_classes = []

    def get(self, request, format=None):
      data = {}
      print(request.GET)
      # 默认是获取上证指数
      stock_id = request.GET.get('stockid', 'sh000001')
      stock_id = utils.name2code(stock_id.strip())
      print(stock_id)
      if utils.valid_stock(stock_id) or stock_id == 'sh000001':
        open_list, open_pred_list, close_list, close_pred_list, y_open, y_close = analysis.analysis_stock(stock_id)
        data['stockname'] = utils.code2name(stock_id) + ' (' + stock_id + ')'
        senti = analysis.analysis_stock_news(stock_id)
        data['open'] = [round(x, 2) for x in open_list]
        data['open_pred'] = [round(x, 2) for x in open_pred_list]
        data['close'] = [round(x, 2) for x in close_list]
        data['close_pred'] = [round(x, 2) for x in close_pred_list]
        # x_ax = list(range(1, len(open_list)+1))
        data['x_ax'] = list(range(1, len(open_list) + 1))
        data['ymin'] = round(min(open_list + open_pred_list + close_list + close_pred_list) * 0.85, 2)
        data['ymax'] = round(max(open_list + open_pred_list + close_list + close_pred_list) * 1.15, 2)
        data['y_open'] = round(y_open, 2)
        data['y_close'] = round(y_close, 2)
        data['delta'] = round(y_close - y_open, 2)
        data['delta_percent'] = round(round(y_close - y_open, 2)/y_open*100.0, 2)
        data['flag'] = 1 if y_close > y_open else 0
        data['senti'] = round(senti, 2) if y_close > y_open else round(1.0-senti, 2)
      else:
          data['error_message'] = '请输入正确的股票代码或股票名称！'

      return Response(data)

