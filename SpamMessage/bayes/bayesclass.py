import random

import math
import os
from collections import Counter
import pickle
import jieba
from SpamMessage import settings

def preprocess():
  # 对短信进行分词
  # 原始数据中的正常短信样本数量过多，所以过滤掉一些
  # 垃圾短信 : 正常短信 约为 1:2
  # 也是为了提高模型的效率
  with open(os.path.join(settings.BASE_DIR, "data.txt"), "r", encoding="utf-8") as f:
    with open(os.path.join(settings.BASE_DIR, "data_clean.txt"), "w", encoding="utf-8") as fw:
      samples = 0
      for line in f:
        samples += 1
        if samples % 5000 == 0:
          print("预处理：{}".format(samples))
        try:
          parts = line.strip().split("\t")
          label = int(parts[0])
          tokens = jieba.cut(parts[1])
          s_tokens = ' '.join(tokens)
          if label == 0:
            seed = random.randint(1, 9)
            if seed <= 2:
              fw.write("{}\t{}\n".format(label, s_tokens))
          else:
            fw.write("{}\t{}\n".format(label, s_tokens))
        except IndexError:
          print("! 残缺样本！")
        fw.flush()
      print("== 预处理、分词完毕 ==")

def vocabulary(dim=200):
  print("选择高频的 {} 词作特征".format(dim))
  # 获取短信中的关键词表，作为特征
  stopwords = set()
  spam_count = 0
  ham_count = 0
  # 去除停用词，即常见的词和标点符号等
  with open(os.path.join(settings.BASE_DIR, "stop.txt"), "r", encoding="utf-8") as f:
    for line in f:
      w = line.strip()
      stopwords.add(w)
  # 统计词频
  c_spam = {}
  c_ham = {}
  with open(os.path.join(settings.BASE_DIR, "data_clean.txt"), "r", encoding="utf-8") as f:
    for line in f:
      _lines = line.split("\t")
      l, s = int(_lines[0]), str(_lines[1]).strip()
      if l == 1:
        spam_count += 1
        for x in s.split():
          if x in stopwords:
            continue
          if x in c_spam:
            c_spam[x] += 1
          else:
            c_spam[x] = 1
      else:
        ham_count += 1
        for x in s.split():
          if x in stopwords:
            continue
          if x in c_ham:
            c_ham[x] += 1
          else:
            c_ham[x] = 1
  one_vocab = [word for word in c_spam if c_spam[word] >= dim]
  zero_vocab = [word for word in c_ham if c_ham[word] >= dim]
  vocab = set(one_vocab) | set(zero_vocab)
  print("特征词：", len(vocab), "个")
  return vocab, c_spam, c_ham, spam_count, ham_count

def bayes(vocab, ham_counter, spam_counter, ham_count, spam_count):
  # 写入统计文件，作为贝叶斯分类的特征，直接保存成一个 dict 格式
  # 首先需要统计每一个词在垃圾、非垃圾邮件中出现的概率
  # p(w|Spam) = w 在垃圾邮件中出现的次数 / 所有垃圾邮件中出现的词数 = w_spam / vocab_spam
  # p(w|Ham) = w 在正常邮件中出现的次数 / 所有正常邮件中出现的词数 = w_ham / vocab_ham
  # 统计分母
  spam_vocab = 0
  ham_vocab = 0
  # 为了防止出现 0 的情况，加入 Laplace 平滑：
  for w in vocab:
    w_spam = spam_counter[w] + 1
    w_ham = ham_counter[w] + 1
    spam_vocab += (w_spam + 1)
    ham_vocab += (w_ham + 1)
  # 计算两个概率
  # bayes_dict[w][0] = p(w|Ham) = w_ham / ham_vocab
  # bayes_dict[w][1] = p(w|Spam) = w_spam / spam_vocab
  bayes_dict = {}
  # 为了防止预测时概率相乘下溢出，改为取对数相加的方式，这里直接保存对数值
  for w in vocab:
    w_dict = {}
    w_dict[0] = math.log((ham_counter[w] + 1) / ham_vocab)
    w_dict[1] = math.log((spam_counter[w] + 1) / spam_vocab)
    bayes_dict[w] = w_dict
  total_count = {}
  total_count[0] = math.log(ham_count / (ham_count + spam_count))
  total_count[1] = math.log(spam_count / (ham_count + spam_count))
  bayes_dict["_reserved"] = total_count
  print("参数学习完毕，保存模型！")
  with open(os.path.join(settings.BASE_DIR, "bayes.pickle"), "wb") as f:
    pickle.dump(bayes_dict, f, pickle.HIGHEST_PROTOCOL)

def load_bayes():
  print("加载贝叶斯分类模型...")
  with open(os.path.join(settings.BASE_DIR, "bayes.pickle"), "rb") as f:
    bayes_dict = pickle.load(f)
  return bayes_dict

def test_bayes(bayes_dict, sentence):
  print("测试样例：{}".format(sentence))
  # 先验概率：
  ham_log_prob = bayes_dict["_reserved"][0]
  spam_log_prob = bayes_dict["_reserved"][1]
  for x in jieba.cut(sentence):
    if x in bayes_dict:
      ham_log_prob += bayes_dict[x][0]
      spam_log_prob += bayes_dict[x][1]
  # 哪个大即为哪个类
  return 0 if ham_log_prob > spam_log_prob else 1

if __name__ == '__main__':
  # 1. 预处理：分词，比较耗时，运行一次即可
  # preprocess()

  # 2. 学习贝叶斯分类器模型参数
  # vocab, c_spam, c_ham, n_spam, n_ham = vocabulary(dim=128)
  # spam_counter = Counter(c_spam)
  # ham_counter = Counter(c_ham)
  # bayes(vocab, ham_counter, spam_counter, n_ham, n_spam)
  # 3. 加载贝叶斯模型
  bayes_dict = load_bayes()
  # 写入一个可读的文件
  with open(os.path.join(settings.BASE_DIR, "bayes.txt"), "w", encoding="utf8") as f:
    for k in bayes_dict:
      f.write("{} {} {}\n".format(k, bayes_dict[k][0], bayes_dict[k][1]))
  x = test_bayes(bayes_dict, "你好！世界百强金融企业诚聘主管，销售，内勤，要求：大专及以上者，年令：xx左右及以上，工作时间自由(内勤除外)，收入：xx万一xx万，")
  print(x)
  x = test_bayes(bayes_dict, "病人总在抱怨为什么晚上就一个护士医生值班要不要拿卫生部的新规定糊他们一脸现在有人值班就不错了")
  print(x)



