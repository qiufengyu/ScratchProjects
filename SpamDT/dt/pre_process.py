import os
from collections import Counter

import jieba
from pathlib import Path

"""
获取所有邮件数据，便于进行交叉验证
也方便划分训练、测试集
"""
def get_all_files(root_dir):
  file_list = []
  for root, sub_folders, files in os.walk(root_dir):
    for file in files:
      f = Path(root) / file
      # 为了保证在 Windows 和 Linux / Unix 下的兼容性
      file_list.append(str(f).replace("\\", '/'))
  return file_list

"""
获取邮件是否为垃圾邮件的标志
"""
def get_index(index_file):
  spam_set = set()
  with open(index_file, 'r', encoding='utf-8') as f:
    while True:
      line = f.readline()
      if not line:
        break
      tokens = line.strip().split(' ')
      if tokens[0] == 'spam':
        spam_set.add(tokens[1])
  return spam_set

"""
判断是否为垃圾邮件的小函数
"""
def is_spam(file_name, spam_set):
  if file_name in spam_set:
    return True
  else:
    return False

def vocabulary(file_list):
  print("正在构建字典特征...")
  vocab_counter = Counter()
  file_cnt = 0
  for file in file_list:
    file_cnt += 1
    with open(file, 'r', encoding='utf-8') as f:
      while True:
        line = f.readline()
        if not line:
          break
        seg = jieba.cut(line.strip(), cut_all=False)
        vocab_counter += Counter(list(seg))
    if file_cnt % 250 == 0:
      print(f"{file_cnt} 文件已处理...")
  with open('vocab.txt', 'w', encoding='utf-8') as fwrite:
    most_common_list = vocab_counter.most_common()
    for word, cnt in most_common_list:
      word = word.strip()
      if len(word) > 0:
        fwrite.write(word+'\n')
    # print("最后一个频繁词是", most_common_list[-1][0], "，出现次数是", most_common_list[-1][1])

def load_vocab(vocab_file_name='vocab.txt', top_n=-1):
  print("加载字典中...")
  word_dict = dict()
  with open(vocab_file_name, 'r', encoding='utf-8') as f:
    line_cnt = 0
    while True:
      line = f.readline()
      if not line:
        break
      if top_n > 0 and line_cnt > top_n:
        break
      line = line.strip()
      if len(line) > 0:
        word_dict[line] = line_cnt
        line_cnt += 1
    # print(line_cnt)
  return word_dict

def make_feature(file_list: list, word_dict: dict, spam_set: set, output: str):
  print("正在生成特征文件", output)
  dim = len(word_dict)
  print("特征维数：", dim)
  # print(dim)
  with open(output, 'w', encoding='utf-8') as f_out:
    for file in file_list:
      this_counter = Counter()
      this_vector = [0]*dim
      with open(file, 'r', encoding='utf-8') as f:
        while True:
          line = f.readline()
          if not line:
            break
          seg = jieba.cut(line.strip(), cut_all=False)
          this_counter += Counter(list(seg))
      total_words = sum(this_counter.values())
      for word, cnt in this_counter.most_common():
        if word in word_dict:
          this_vector[word_dict[word]] = 1 # cnt
          # 此处也尝试过用 count 值，但是效果变差了...
      str_vector = [f"{x:d}" for x in this_vector]
      f_out.write(','.join(str_vector))
      if is_spam(file, spam_set):
        f_out.write(',1\n')
      else:
        f_out.write(',0\n')

def make_feature_for_test_email(content, word_dict: dict):
  dim = len(word_dict)
  this_vector = [0] * dim
  for line in content.split():
    line = line.strip()
    if len(line) > 0:
      seg = jieba.cut(line.strip(), cut_all=False)
      for x in seg:
        if x in word_dict:
          this_vector[word_dict[x]] = 1
  return this_vector


"""
有一个备选方案：（但是效果可能不好）
使用 word2vec 模型，计算邮件文本的特征
每一封邮件表示为一个 128 维的向量，可分为 4 组
根据中文分词的结果，将每个词通过 word2vec 表示为一个 32 维的向量
此外，不采用分词，对每个字通过 word2vec 表示为一个 32 维的向量
第一组：将邮件文本中每个词对应的向量取平均 （average-pooling，平均池化)
第二组：将邮件文本中每个词对应的向量，每一维取最大值 (max-pooling，最大池化)
第三组：将邮件文本中每个字对应的向量取平均 （average-pooling，平均池化)
第四组：将邮件文本中每个字对应的向量，每一维取最大值 (max-pooling，最大池化)
出于的问题：为什么不用基于字典的表示？
字典的规模非常大，约有 20000 词，一个 20000 维的特征向量太过庞大，必须进行特征压缩提取
目前折中使用最频繁的前 3000 词，准确率能达到 94%+ 左右。
"""
