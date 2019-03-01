import math
import os
from collections import Counter
import pickle
import jieba
from SpamMail import settings

def preprocess():
  # 对邮件进行分词
  with open(os.path.join(settings.BASE_DIR, "data_raw.txt"), "r", encoding="utf8") as f:
    with open(os.path.join(settings.BASE_DIR, "data.txt"), "w", encoding="utf8") as fw:
      for line in f:
        l_s = line.strip().split("\t")
        tokens = jieba.cut(l_s[1])
        s_split = ' '.join(tokens)
        fw.write("{}\t{}\n".format(l_s[0], s_split))

def vocabulary():
  # 获取邮件中的关键词表，作为特征
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
  with open(os.path.join(settings.BASE_DIR, "data.txt"), "r", encoding="utf8") as f:
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
  one_vocab = [word for word in c_spam if c_spam[word] >= 200]
  zero_vocab = [word for word in c_ham if c_ham[word] >= 200]
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
  # 为了防止出现 0 的情况，加入平滑：
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
  with open(os.path.join(settings.BASE_DIR, "bayes.pickle"), "wb") as f:
    pickle.dump(bayes_dict, f, pickle.HIGHEST_PROTOCOL)

def load_bayes():
  with open(os.path.join(settings.BASE_DIR, "bayes.pickle"), "rb") as f:
    bayes_dict = pickle.load(f)
  return bayes_dict

def test_bayes(bayes_dict, sentence):
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
  # 1. 预处理：分词，比较耗时
  # c_spam, c_ham = preprocess()
  # 2. 学习贝叶斯分类器模型参数
  vocab, c_spam, c_ham, n_spam, n_ham = vocabulary()
  spam_counter = Counter(c_spam)
  ham_counter = Counter(c_ham)
  bayes(vocab, ham_counter, spam_counter, n_ham, n_spam)
  # 3. 加载贝叶斯模型
  bayes_dict = load_bayes()
  # 写入一个可读的文件
  with open(os.path.join(settings.BASE_DIR, "bayes.txt"), "w", encoding="utf8") as f:
    for k in bayes_dict:
      f.write("{} {} {}\n".format(k, bayes_dict[k][0], bayes_dict[k][1]))
  x = test_bayes(bayes_dict, "丝宇名妆（国际）美业 顺城店庆祝三八妇女节暨x月xx日消费者权益日回馈老顾客优惠活动内容如下；x.充值优惠活动充值xxxx送xxx")
  print(x)
  x = test_bayes(bayes_dict, "这是一个回环式的故事，姜文在技法使用上非常成熟，不过电影上映后差评如潮，很多人表示看不懂，他也就再没有使用过，非常遗憾。")
  print(x)



