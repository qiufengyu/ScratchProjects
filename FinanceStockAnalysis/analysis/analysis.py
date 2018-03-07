import numpy as np
import pymysql
import pandas as pd
import re
import requests
from bs4 import BeautifulSoup
from snownlp import SnowNLP
from sklearn.svm import SVR

from data.get_data import get_hist_data_by_code

# import matplotlib.pyplot as plt

headers = ['date', 'open', 'high', 'close', 'low', 'volume', 'price_change', 'p_change',
           'ma5', 'ma10', 'ma20', 'v_ma5', 'v_ma10', 'v_ma20', 'turnover']

def analysis_stock(code, days=15):
  con = pymysql.connect(host='localhost',
                           user='root',
                           password='root',
                           db='finance',
                           charset='utf8mb4')
  get_hist_data_by_code(code)
  try:
    with con.cursor() as cursor:
      sql = """SELECT * FROM `{}` ORDER BY `date` ASC""".format(code)
      cursor.execute(sql)
      # 数据库的信息放入 pandas DataFrame 之中，再转化为 numpy 数组，方便数据划分等操作
      field_names = [i[0] for i in cursor.description]
      get_data = [xx for xx in cursor]
      df = pd.DataFrame(get_data)
      df.columns = field_names
      df = df.drop('date', axis=1)
      data = df.as_matrix()
      all_days = len(data)
      # 最多选择 10 周 （10*5）的数据量，加上默认 15 的偏置
      if all_days > 50 + days:
        data = data[-(50+days):, :]
        all_days = 50 + days
      X = []
      y_open = []
      y_close = []
      X_ax = []
      for i in range(all_days-days):
        X_i = data[i:i+days, :]
        y_open_i = data[i+days, 0]
        y_close_i = data[i+days, 2]
        X.append(X_i.flatten())
        y_open.append(y_open_i)
        y_close.append(y_close_i)
        X_ax.append(i+1)
      X_train = np.array(X, dtype=np.float32)
      y_open_train = np.array(y_open, dtype=np.float32)
      y_close_train = np.array(y_close, dtype=np.float32)

      svr_open = SVR(kernel='rbf', C=1e3, gamma=0.1)
      print("训练开盘价格模型...")
      svr_open.fit(X_train, y_open_train)
      y_open_pred = svr_open.predict(X_train)
      svr_close = SVR(kernel='rbf', C=1e3, gamma=0.1)
      print("训练收盘价格模型...")
      svr_close.fit(X_train, y_close_train)
      y_close_pred = svr_close.predict(X_train)

      X_test = data[-days:, :].flatten().reshape(1, -1)
      y_open_test = svr_open.predict(X_test)
      y_close_test = svr_close.predict(X_test)

      # print(y_open_train.tolist())
      # print(y_open_pred.tolist())
      # print(y_close_train.tolist())
      # print(y_close_pred.tolist())
      # print(y_open_test[0])
      # print(y_close_test[0])

      y_open_pred_test = y_open_test[0]
      y_close_pred_test = y_close_test[0]

      return y_open_train.tolist(), y_open_pred.tolist(), y_close_train.tolist(), y_close_pred.tolist(), \
             y_open_pred_test, y_close_pred_test


  except Exception as e:
    print(e)
    print("数据库错误！")


def analysis_stock_news(code):
  headers = {
    'User-Agent': 'Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6'
  }
  params = {'s': 'bar', 'name': code}
  # 从新浪股吧获取一些用户的讨论
  guba_html = requests.get('http://guba.sina.com.cn', params=params, headers=headers)
  # 使用 BeautifulSoup 解析
  soup = BeautifulSoup(guba_html.content.decode('gbk'), 'lxml')
  contents = soup.find('div', 'table_content')
  all_text = []
  for a_tags in contents.find_all('a', re.compile('link*')):
    all_text.append(a_tags.text)
  # 计算每一篇讨论的情感值，得到一个平均值
  all_sentiment = [SnowNLP(x).sentiments for x in all_text]
  # print(all_text)
  # print(all_sentiment)
  return (sum(all_sentiment) / len(all_sentiment))




