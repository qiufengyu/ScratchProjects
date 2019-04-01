import os
import re
import sys

import jieba

def check_contain_chinese(check_str):
  """
  判断是否为合法的中文，忽略数据中的邮件文本无关的信息
  :param check_str:
  :return:
  """
  for ch in check_str:
    if u'\u4e00'<= ch <= u'\u9fff':
      return True
  return False

def load_label_files(label_file):
  """
  读取邮件的标签
  :param label_file:
  :return:
  """
  label_dict = {}
  with open(label_file, "r", encoding="utf-8") as f:
    for line in f.readlines():
      list1 = line.strip().split("..")
      label_dict[list1[1].strip()[1:]] = list1[0].strip()
  return label_dict

def load_stop(stop_word_path):
  """
  读取常用词、停用词字典，把中文常用词、标点等去掉
  :param stop_word_path:
  :return:
  """
  stop_dict = set()
  with open(stop_word_path, "r", encoding="utf-8") as f:
    for line in f.readlines():
        line = line.strip()
        stop_dict.add(line)
  return stop_dict

def read_files(root_dir, label_dict, stop_dict, spam_file_path, ham_file_path, limits=600):
  """
  读取邮件，转换为 utf-8 格式，生成对应的垃圾、非垃圾邮件的样本
  两种类别分别选择 600 样本，不建议太多，否则训练过程太长了
  :param root_dir:
  :param label_dict:
  :param stop_dict: 停用词表
  :param spam_file_path: 垃圾邮件路径
  :param ham_file_path: 非垃圾邮件路径
  :return:
  """
  spam_file = open(spam_file_path, 'w', encoding="utf-8")
  ham_file = open(ham_file_path, 'w', encoding="utf-8")
  spam_count = 0
  ham_count = 0
  file_list = []
  for root, subFolders, files in os.walk(root_dir):
    for file in files:
      file_path = os.path.join(root, file)
      file_list.append(file_path.replace('\\', '/'))
  for file in file_list:
    if spam_count > limits and ham_count > limits:
      print("预处理 {} 样本完毕！".format(limits*2))
      break
    file_path_split = file.split('/')
    label_path = '/'.join(file_path_split[-3:])
    label = label_dict[label_path]
    temp_list = []
    # 原本的文件是 gb2312 编码，转换为 utf-8
    with open(file, 'r', encoding='gb2312') as f_read:
      print(file)
      try:
        lines = f_read.readlines()
        for line in lines:
          line = line.strip()
          if not check_contain_chinese(line):
            continue
          # 对中文分词
          seg_list = jieba.cut(line, cut_all=False)
          for word in seg_list:
            if word in stop_dict:
              continue
            else:
              temp_list.append(word)
        # 要求至少要有 5  个有效的词语，可以再行调整
        if len(temp_list) >= 5:
          lines = " ".join(temp_list)
          if label == "spam":
            if spam_count < limits:
              spam_file.write(lines + "\n")
            spam_count += 1
          if label == "ham":
            if ham_count < limits:
              ham_file.write(lines + "\n")
            ham_count += 1
      except UnicodeDecodeError:
        continue
  spam_file.close()
  ham_file.close()

def text_process(text_list):
  """
  从原始文本，进行清洗、分词，转化为词的序列
  :param text_list: 是源文本根据"\n" 进行切分的句子列表
  :return:
  """
  stop_dict = load_stop("./data/stopwords.txt")
  result_list = []
  for line in text_list:
    line = line.strip()
    if not check_contain_chinese(line):
      continue
    # 对中文分词
    seg_list = jieba.cut(line, cut_all=False)
    for word in seg_list:
      if word in stop_dict:
        continue
      else:
        result_list.append(word)
  return result_list

if __name__ == "__main__":
  # 去 https://plg.uwaterloo.ca/~gvcormac/treccorpus06/ 下载 trec06c 中文数据集，
  # 解压到桌面
  # 运行一次即可
  file_path = os.path.expanduser("~/Desktop/trec06c/data")
  label_path = os.path.expanduser("~/Desktop/trec06c/full/index")
  stop_word_path = "./data/stopwords.txt"
  spam_data = "./data/spam.txt"
  ham_data = "./data/ham.txt"
  # 加载对应邮件的标签
  label_dict = load_label_files(label_path)
  # 加载停用词
  stop_dict = load_stop(stop_word_path)
  # 会在 data 目录下生成处理好的 spam.txt 和 ham.txt 文件，limit 控制样本数量
  read_files(file_path, label_dict, stop_dict, spam_data, ham_data, limits=600)