import csv
import shutil
from time import time
from collections import Counter

import math

from spam.clean_data import clean_str, clean_data
from spam.preprocess_data import *


class BayesSpam(object):
  def __init__(self, train_file_list, test_file_list, spam_set, meta_path="", user_data=True, flag = False):
    self.train_file_list = train_file_list
    self.test_file_list = test_file_list
    self.spam_set = spam_set
    # 主要模型参数
    self.spam_word_prob = dict()
    self.ham_word_prob = dict()
    # 垃圾邮件、非垃圾邮件计数
    self.spam_count = 0
    self.ham_count = 0
    self.total_count = 0
    self.spam_prob = 0.0
    self.ham_prob = 0.0
    self.user_data = user_data
    self.flag = flag
    self.meta_path = meta_path

  # 模型的核心部分，建模的过程
  def _count(self):
    # 每次重新训练时，清空原本的数据！
    self.spam_word_prob = dict()
    self.ham_word_prob = dict()
    # 垃圾邮件、非垃圾邮件计数
    self.spam_count = 0
    self.ham_count = 0
    self.total_count = 0
    self.spam_prob = 0.0
    self.ham_prob = 0.0
    spam_counter = Counter()
    ham_counter = Counter()
    for file in self.train_file_list:
      try:
        with open(file, 'r', encoding='utf-8') as f:
          unique_word_set = set()
          if is_spam(file, self.spam_set):
            self.spam_count += 1
            while True:
              line = f.readline()
              if not line:
                break
              line = clean_str(line)
              seg = jieba.cut(line, cut_all=False)
              for x in seg:
                unique_word_set.add(x)
            spam_counter += Counter(unique_word_set)
              # spam_counter += Counter(set(seg))
          else:
            self.ham_count += 1
            while True:
              line = f.readline()
              if not line:
                break
              line = clean_str(line)
              seg = jieba.cut(line, cut_all=False)
              for x in seg:
                unique_word_set.add(x)
            ham_counter += Counter(unique_word_set)
          self.total_count += 1
          if self.total_count % 1000 == 0:
            print("{} 文件已处理！".format(self.total_count))
      except UnicodeDecodeError:
        continue

    # 是否加入用户数据
    if self.user_data:
      print("加载用户定义的正常邮件")
      my_ham_list = os.listdir(os.path.join(self.meta_path, 'myham'))
      for file in my_ham_list:
        with open(os.path.join(self.meta_path, os.path.join('myham', file)), 'r', encoding='utf-8') as f:
          unique_word_set = set()
          self.ham_count += 1
          while True:
            line = f.readline()
            if not line:
              break
            line = clean_str(line)
            seg = jieba.cut(line, cut_all=False)
            for x in seg:
              unique_word_set.add(x)
          ham_counter += Counter(unique_word_set)
        self.total_count += 1

      print("加载用户定义的垃圾邮件")
      my_spam_list = os.listdir(os.path.join(self.meta_path, 'myspam'))
      for file in my_spam_list:
        with open(os.path.join(self.meta_path, os.path.join('myspam', file)), 'r', encoding='utf-8') as f:
          unique_word_set = set()
          self.spam_count += 1
          while True:
            line = f.readline()
            if not line:
              break
            line = clean_str(line)
            seg = jieba.cut(line, cut_all=False)
            for x in seg:
              unique_word_set.add(x)
              spam_counter += Counter(unique_word_set)
        self.total_count += 1

    # 可以观察一下某些最容易出现在垃圾邮件中的词
    # print(spam_counter.most_common(20))
    # print(ham_counter.most_common(20))
    assert(self.ham_count == self.total_count - self.spam_count)

    # 计算两种邮件的概率，在全概率公式下要用到
    # 注意，我们不能假设垃圾邮件概率和非垃圾邮件的先验概率是相等的
    self.ham_prob = float(self.ham_count) / float(self.total_count)
    self.spam_prob = float(self.spam_count) / float(self.total_count)

    # 将计数转化成概率，并且进行 +1 的平滑操作，
    # 避免出现最终计算的概率为 0，或者设置一个较小的默认值，如 1x10^(-6) 这样的极小概率
    # 所以这里的从 counter 中得到的值要进行 +1
    for w, c in spam_counter.most_common():
      self.spam_word_prob[w] = (1.0 + float(c)) / (float(self.spam_count) * 2.0)
    for w, c in ham_counter.most_common():
      self.ham_word_prob[w] = (1.0 + float(c)) / (float(self.ham_count) * 2.0)

    # print(self.spam_word_prob.get('的'))
    # 设置模型是否为空的标志位
    self.flag = True
    model_path = os.path.join(self.meta_path, 'model')
    self.save_model(model_path)


  # 取对数是因为较小的概率连乘时，可能会导致精度溢出
  # 转换成对数相加的形式，可以规避这种溢出
  # 对于某些未出现的词，在进行了 +1 平滑操作后，就是 1 / (2*对应类别的邮件数)
  # 开始使用 Bayes 和全概率公式，计算某个词在
  def get_spam_word_log_prob(self, word):
    word_spam_prob = self.spam_word_prob.get(word, 0.5 / self.spam_count)
    word_ham_prob = self.ham_word_prob.get(word, 0.5 / self.ham_count)
    total_word_spam_prob = (self.spam_prob * word_spam_prob) / (self.spam_prob * word_spam_prob + self.ham_prob * word_ham_prob)
    return math.log(total_word_spam_prob)

  def get_ham_word_log_prob(self, word):
    word_spam_prob = self.spam_word_prob.get(word, 0.5 / self.spam_count)
    word_ham_prob = self.ham_word_prob.get(word, 0.5 / self.ham_count)
    total_word_ham_prob = (self.ham_prob * word_ham_prob) / (
        self.spam_prob * word_spam_prob + self.ham_prob * word_ham_prob)
    return math.log(total_word_ham_prob)

  # 把训练得到的模型存下来，在实时分析中可以直接使用
  # 不必重新训练，加快速度
  def save_model(self, path='model'):
    if not self.flag:
      self._count()
    print("保存模型文件中...")
    if not os.path.exists(path):
      os.mkdir(path)
    with open(os.path.join(path, 'spam_word_prob.txt'), 'w', encoding='utf-8') as f1:
      for k, v in self.spam_word_prob.items():
        f1.write(str(k) + '#@#' + str(v)+'\n')
    with open(os.path.join(path, 'ham_word_prob.txt'), 'w', encoding='utf-8') as f2:
      for k, v in self.ham_word_prob.items():
        f2.write(str(k) + '#@#' + str(v)+'\n')
    with open(os.path.join(path, 'meta.txt'), 'w', encoding='utf-8') as f3:
      f3.write(str(self.spam_count) + '\n')
      f3.write(str(self.spam_prob) + '\n')
      f3.write(str(self.ham_count) + '\n')
      f3.write(str(self.ham_prob) + '\n')
    print("保存完毕！")

  def load_model(self, path='model'):
    abs_path = os.path.join(self.meta_path, path)
    print("加载模型中...")
    with open(os.path.join(abs_path, 'spam_word_prob.txt'), 'r', encoding='utf-8') as f1:
      self.spam_word_prob = dict()
      while True:
        line = f1.readline()
        if not line:
          break
        kvs = line.rstrip().split('#@#')
        self.spam_word_prob[kvs[0]] = float(kvs[1])
    with open(os.path.join(abs_path, 'ham_word_prob.txt'), 'r', encoding='utf-8') as f2:
      self.ham_word_prob = dict()
      while True:
        line = f2.readline()
        if not line:
          break
        kvs = line.rstrip().split('#@#')
        self.ham_word_prob[kvs[0]] = float(kvs[1])
    with open(os.path.join(abs_path, 'meta.txt'), 'r', encoding='utf-8') as f3:
      four_meta = []
      for i in range(4):
        four_meta.append(f3.readline())
      self.spam_count = float(four_meta[0])
      self.spam_prob = float(four_meta[1])
      self.ham_count = float(four_meta[2])
      self.ham_prob = float(four_meta[3])
    print("模型加载完毕！")

  # 计算一封邮件是垃圾邮件的概率和非垃圾邮件的概率
  # 哪个大就是哪一类
  # 参数 email 这里是测试邮件的文件名
  def test_email(self, email):
    if not self.flag:
      self._count()
    spam_prob = 0.0
    ham_prob = 0.0
    with open(email, 'r', encoding='utf-8') as f:
      while True:
        line = f.readline()
        if not line:
          break
        line = clean_str(line)
        seg = jieba.cut(line, cut_all=False)
        for x in seg:
          # 转化成对数，所以是相加
          spam_prob += self.get_spam_word_log_prob(x)
          ham_prob += self.get_ham_word_log_prob(x)

    # spam_exp = math.exp(spam_prob) + 1e-12
    # ham_exp = math.exp(ham_prob) + 1e-12
    # print("垃圾邮件的概率：", spam_exp / (spam_exp + ham_exp))
    # print("垃圾邮件 vs 非垃圾邮件：", spam_prob, "vs", ham_prob)

    # 垃圾邮件返回 True，否则返回 False
    return spam_prob >= ham_prob

  def test_single_email(self, content):
    spam_prob = 0.0
    ham_prob = 0.0
    lines = content.split('\n')
    for line in lines:
      line = clean_str(line)
      seg = jieba.cut(line, cut_all=False)
      for x in seg:
        spam_prob += self.get_spam_word_log_prob(x)
        ham_prob += self.get_ham_word_log_prob(x)
    return spam_prob >= ham_prob

if __name__ == "__main__":
  start = time()
  # 运行一次即可，之后就不再使用
  # if os.path.exists('data'):
    # shutil.rmtree('data/')
  # clean_data('trec06c/data')
  # shutil.copy('trec06c/full/index', 'index')
  # 上面的运行一次就可以了
  # 在此之后可以把原始的数据删了
  file_list = get_all_files('data')
  print("合计有效文件：", len(file_list))
  spam_set = get_index('index')
  # 实际运行时，需要定义 total_fold 和 folder
  total_fold = 5
  # 进行交叉验证的代码：
  # for fold in range(total_fold)
    # train_file_list, test_file_list = split_data(file_list, total_fold, fold)
    # 下面就一样了...
  train_file_list, test_file_list = split_data(file_list, total_fold=5, fold=2)
  print("训练集文件数：", len(train_file_list))
  print("测试集文件数：", len(test_file_list))
  bs = BayesSpam(train_file_list, test_file_list, spam_set)
  bs.save_model()
  bs.load_model()
  correct = 0
  total_count = 0
  for file_i in test_file_list:
    total_count += 1
    if bs.test_email(file_i) == is_spam(file_i, spam_set):
      correct += 1
      if is_spam(file_i, spam_set):
        print(file_i, "是垃圾邮件，预测正确！")
      else:
        print(file_i, "不是垃圾邮件，预测正确！")
    else:
      if is_spam(file_i, spam_set):
        print(file_i, "是垃圾邮件，预测错误！")
      else:
        print(file_i, "不是垃圾邮件，预测错误！")
  end = time()
  print("准确率：", correct / total_count * 100.0, "%")
  print("运行时间：", end-start, "秒，即", (end-start)/60.0, "分")
