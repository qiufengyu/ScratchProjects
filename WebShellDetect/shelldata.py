import math
import os
import pickle
import random
import re
import subprocess

from WebShellDetect.settings import BASE_DIR

def get_opcode(php_file):
  try:
    x = subprocess.run(["php", "-dvld.active=1", "-dvld.execute=0", php_file], stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT)
    out_string = str(x.stdout.decode("utf-8"))
    # 正则表达式匹配其中的 opcode
    op_tokens = re.findall(r'\s(\b[A-Z_]{2,}\b)\s', out_string)
    if (len(op_tokens) > 2):
      return op_tokens
    else:
      return []
  except:
    return []

def get_php_files(dir, black=False, append=False):
  count = 0
  ignore = 0
  target = "white"
  if black:
    target = "black"
  mode = "w"
  if append:
    mode = "a"
  with open("php.{}.txt".format(target), mode=mode, encoding="utf-8") as f:
    for root, dirs, files in os.walk(dir):
      for filename in files:
        if "php" in filename:
          filepath = os.path.join(root, filename)
          try:
            x = subprocess.run(["php", "-dvld.active=1", "-dvld.execute=0", filepath], stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT)
            out_string = str(x.stdout.decode("utf-8"))
            op_tokens = re.findall(r'\s(\b[A-Z_]{2,}\b)\s', out_string)
            if(len(op_tokens) > 2):
              f.write(' '.join(op_tokens))
              f.write("\n")
              f.flush()
              count += 1
          except:
            ignore += 1
            print("Invalid shell, ignore")
          if count % 100 == 0:
            print("100 finished")
  total_count = count + ignore
  print(f"Total: {total_count}, Valid: {count}, Invalid: {ignore}")

def build_opcode_vocab():
  # 没有使用 http://php.net/manual/en/internals2.opcodes.php 给出的列表
  # 因为调用 VLD 生成的 opcode 有的没有 ZEND 前缀，不完全覆盖，但是差不多
  # 这样直接从样本数据中构建，更稳健，且两者统计完结果基本一致
  opcode_set = set()
  with open("php.black.txt", "r", encoding="utf-8") as f:
    for line in f:
      for op in line.strip().split():
        opcode_set.add(op)
  with open("php.white.txt", "r", encoding="utf-8") as f:
    for line in f:
      for op in line.strip().split():
        opcode_set.add(op)
  print(len(opcode_set))
  opdict = {}
  with open("php.opcode.txt", "w", encoding="utf-8") as f:
    for w in opcode_set:
      f.write("{}\n".format(w))
  # 同时将数据保存在文件中，方便直接加载
  # 预定义一个关键词：UNK 表示两种脚本的样本数的先验知识
  opdict = {}
  opdict["UNK"] = 0
  cnt = 1
  with open(os.path.join(BASE_DIR, "php.opcode.txt"), "r", encoding="utf-8") as f:
    for line in f:
      opdict[line.strip()] = cnt
      cnt += 1
  with open("php.opcode.pickle", "wb") as f:
    pickle.dump(opdict, f, pickle.HIGHEST_PROTOCOL)

def load_opcode_dict():
  with open(os.path.join(BASE_DIR, "php.opcode.pickle"), "rb") as f:
    opdict = pickle.load(f)
  return opdict

def train_test_split(raw_file, test=0.1, black=False):
  target = "white"
  if black:
    target = "black"
  with open(raw_file, "r", encoding="utf-8") as f:
    with open(os.path.join(BASE_DIR, "php.{}.train.txt".format(target)), "w", encoding="utf-8") as f_train:
      with open(os.path.join(BASE_DIR, "php.{}.test.txt".format(target)), "w", encoding="utf-8") as f_test:
        for line in f:
          if random.random() > test:
            f_train.write(line)
          else:
            f_test.write(line)

def calculate_idf():
  opcodes_dict = load_opcode_dict()
  opcodes_idf = {}
  for x in opcodes_dict:
    opcodes_idf[x] = 1
  documents = 0
  with open(os.path.join(BASE_DIR, "php.white.train.txt"), "r", encoding="utf-8") as f:
    for line in f:
      documents += 1
      for code in set(line.split()):
        opcodes_idf[code] += 1
  with open(os.path.join(BASE_DIR, "php.black.train.txt"), "r", encoding="utf-8") as f:
    for line in f:
      documents += 1
      for code in set(line.split()):
        opcodes_idf[code] += 1
  for x in opcodes_idf:
    opcodes_idf[x] = math.log10(documents / opcodes_idf[x])
  return opcodes_idf

if __name__ == '__main__':
  # 0. 从收集到的 php webshell 中提取 opcode，使用 php 的 vld 工具
  # 将 php 代码转化为执行码，写入对应的 black.txt 和 white.txt 中
  # 很耗时，运行一次即可
  # 这会生成 php.white.txt 和 php.black.txt
  # print("white list: ")
  # get_php_files(os.path.expanduser("~/Desktop/white-list"), black=False)
  # print("black list: ")
  # get_php_files(os.path.expanduser("~/Desktop/black-list"), black=True)
  # 1. 预先统计一下所有的 opcode 类型，199 个，写入 php.opcode.txt 中，作为特征词典
  # 这在朴素贝叶斯、SVM、CNN、RNN 上特征提取时都用得到，
  # 同时保存为一个 dict 类型的对象，（加入了一个 UNK，是 200 维）方便读取
  # 这会生成 php.opcode.txt 和 php.opcode.pickle
  build_opcode_vocab()
  # 2. 对数据进行训练集和测试集的划分，大致为 9:1
  # 生成 train 和 test 文件
  train_test_split(os.path.join(BASE_DIR, "php.white.txt"))
  train_test_split(os.path.join(BASE_DIR, "php.black.txt"), black=True)
  # 3. 计算 idf 作为特征提取的辅助数据
  opcodes_idf = calculate_idf()
  # 写入二进制文件 php.opcode.idf.pickle，方便直接读取为 dict 类型
  with open(os.path.join(BASE_DIR, "php.opcode.idf.pickle"), "wb") as f:
    pickle.dump(opcodes_idf, f, pickle.HIGHEST_PROTOCOL)
  # 写入一个可读的文件 php.opcode.idf.txt
  with open(os.path.join(BASE_DIR, "php.opcode.idf.txt"), "w") as f:
    for x in opcodes_idf:
      f.write("{}: {}\n".format(x, opcodes_idf[x]))




