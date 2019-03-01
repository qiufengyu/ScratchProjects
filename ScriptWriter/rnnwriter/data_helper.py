import pickle
import os

import re

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

def load_text(path):
  input_file = os.path.join(path)
  with open(input_file, 'r', encoding="utf-8") as f:
    text_data = f.read()
  return text_data

def text_clean(text_data):
  lines_of_text = text_data.split("\n")
  # print(len(lines_of_text))
  # 去除空行
  lines_of_text = [line for line in lines_of_text if len(line) > 0]
  # 去除行首的空格
  lines_of_text = [line.strip() for line in lines_of_text]
  # 一些英文符号，全转换成中文的
  pattern = re.compile(r"\.+")
  lines_of_text = [pattern.sub("。", line) for line in lines_of_text]
  pattern = re.compile(r",")
  lines_of_text = [pattern.sub("，", line) for line in lines_of_text]
  pattern = re.compile(r":")
  lines_of_text = [pattern.sub("：", line) for line in lines_of_text]
  # 把空格转换成逗号
  pattern = re.compile(r"\s+")
  lines_of_text = [pattern.sub("，", line) for line in lines_of_text]
  # 有时会有 '\r' 在行尾，一并去掉
  pattern = re.compile(r'\\r')
  lines_of_text = [pattern.sub("", line) for line in lines_of_text]
  # print(len(lines_of_text))
  return lines_of_text

def token_lookup():
  # 把中文的标点符号用特殊的标记对应起来，区分『我。』和『我』
  symbols = ['。', '，', '“', "”", '；', '：', '！', '？', '（', '）', '——', '\n']
  tokens = ["P", "C", "Q", "T", "S", "H", "E", "M", "I", "O", "D", "R"]
  return dict(zip(symbols, tokens))

def create_table(input_data):
  # 把每一个字对应到一个下标数字
  vocab = set(input_data)
  # 字到数字映射
  vocab2idx = { word: idx for idx, word in enumerate(vocab) }
  # 数字到字的映射
  idx2vocab = dict(enumerate(vocab))
  return vocab2idx, idx2vocab

def preprocess_text(text, token_lookup, create_table):
  path = os.path.join(BASE_DIR, "pre.pickle")
  token_dict = token_lookup()
  for k in token_dict:
    text = text.replace(k, "{}".format(token_dict[k]))
  text = list(text)
  vocab2idx, idx2vocab = create_table(text)
  text_int = [vocab2idx[w] for w in text]
  pickle.dump((text_int, vocab2idx, idx2vocab, token_dict), open(path, 'wb'))

def load_preprocess():
  path = os.path.join(BASE_DIR, "pre.pickle")
  return pickle.load(open(path, mode='rb'))

def save_params(params):
  path = os.path.join(BASE_DIR, "parmas.pickle")
  pickle.dump(params, open(path, 'wb'))

def load_params():
  path = os.path.join(BASE_DIR, "parmas.pickle")
  return pickle.load(open(path, mode='rb'))