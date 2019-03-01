"""
首先，不借助机器学习库，实现一个简单的朴素贝叶斯分类
"""
import math
import os
import pickle

from shelldata import get_opcode
from WebShellDetect.settings import BASE_DIR

def load_opcodes():
  # 预定义一个关键词：UNK 表示两种脚本的样本数的先验知识
  opcodes = {}
  opcodes["UNK"] = 0
  inversed_opcodes = {}
  inversed_opcodes[0] = "UNK"
  cnt = 1
  with open(os.path.join(BASE_DIR, "php.opcode.txt"), "r", encoding="utf-8") as f:
    for line in f:
      opcodes[line.strip()] = cnt
      inversed_opcodes[cnt] = line.strip()
      cnt += 1
  return opcodes, inversed_opcodes

def naive_bayes(opcodes):
  opcodes_stats = {}
  # 这里所有的统计量都已经 + 1，完成了平滑
  for k in opcodes:
    opcodes_stats[k] = {}
    opcodes_stats[k][0] = 1
    opcodes_stats[k][1] = 1
  with open(os.path.join(BASE_DIR, "php.white.train.txt"), "r", encoding="utf-8") as f:
    for line in f:
      opcodes_stats["UNK"][0] += 1
      for code in line.split():
        opcodes_stats[code][0] += 1
  with open(os.path.join(BASE_DIR, "php.black.train.txt"), "r", encoding="utf-8") as f:
    for line in f:
      opcodes_stats["UNK"][1] += 1
      for code in line.split():
        opcodes_stats[code][1] += 1
  # 为了方便计算概率，计算好频率，当作概率，即为 TF
  # p(opcode|White) = opcode 在正常 Webshell 中出现的次数 / 所有正常脚本中出现的 opcode 数
  # p(opcode|Black) = opcode 在异常 Webshell 中出现的次数 / 所有异常脚本中出现的 opcode 数
  # 先计算分母
  white_opcodes, black_opcodes = -opcodes_stats["UNK"][0], -opcodes_stats["UNK"][1]
  for k in opcodes:
    white_opcodes += opcodes_stats[k][0]
    black_opcodes += opcodes_stats[k][1]
  # 为了防止概率相乘下溢出，转换为对数相加的形式
  # 但是要注意，预留的统计两种脚本的数量的值，要重新计算
  white_count = opcodes_stats["UNK"][0]
  black_count = opcodes_stats["UNK"][1]
  total_count = white_count + black_count
  for k in opcodes:
    opcodes_stats[k][0] = math.log(opcodes_stats[k][0] / white_opcodes)
    opcodes_stats[k][1] = math.log(opcodes_stats[k][1] / black_opcodes)
  # 通过上面的 for 循环，统计数量的值被污染了，重新计算还原
  opcodes_stats["UNK"][0] = math.log(white_count / total_count)
  opcodes_stats["UNK"][1] = math.log(black_count / total_count)
  # 把 bayes 模型保存成一个二进制文件，下次可以直接调用
  with open(os.path.join(BASE_DIR, "bayes/bayes.pickle"), "wb") as f:
    pickle.dump(opcodes_stats, f, pickle.HIGHEST_PROTOCOL)
  # 写入一个可读的文件中
  with open(os.path.join(BASE_DIR, "bayes/bayes_model.txt"), "w", encoding="utf8") as f:
    for k in opcodes_stats:
      f.write("{} {} {}\n".format(k, opcodes_stats[k][0], opcodes_stats[k][1]))
  return opcodes_stats

def load_bayes_model():
  with open(os.path.join(BASE_DIR, "bayes/bayes.pickle"), "rb") as f:
    bayes_model = pickle.load(f)
  return bayes_model

# 如果不是 opcode 的 list，设置 ifopcode 为 false，
# 需要从对应的文件中，先解析为 opcode
def test_bayes_model(bayes_model, php_text, ifopcode=True):
  opcodes = php_text if ifopcode else get_opcode(php_text)
  white_log_prob = bayes_model["UNK"][0]
  black_log_prob = bayes_model["UNK"][1]
  for x in opcodes:
    if x in bayes_model:
      white_log_prob += bayes_model[x][0]
      black_log_prob += bayes_model[x][1]
  return 0 if white_log_prob > black_log_prob else 1


def run_app(single=False):
  # 初始化贝叶斯模型
  opcodes, inversed_opcodes = load_opcodes()
  # 学习模型参数，并写入 bayes 模型的 pickle 和 txt 中
  naive_bayes(opcodes)
  # 模型测试，首先加载模型 bayes.pickle
  bayes_model = load_bayes_model()
  if single:
    # 测试单个文件
    white_test_file = os.path.expanduser("~/Desktop/white.php")
    black_test_file = os.path.expanduser("~/Desktop/black.php")
    print(test_bayes_model(bayes_model, white_test_file, ifopcode=False))
    print(test_bayes_model(bayes_model, black_test_file, ifopcode=False))
    return (0, 0, 0, 0, 0)
  else:
    # 测试 test 中的样本
    black_test = 0
    black_correct = 0
    with open(os.path.join(BASE_DIR, "php.black.test.txt")) as f_test:
      for line in f_test:
        black_test += 1
        code = line.strip().split()
        if test_bayes_model(bayes_model, code) == 1:
          black_correct += 1
    black_false = black_test - black_correct
    white_test = 0
    white_correct = 0
    with open(os.path.join(BASE_DIR, "php.white.test.txt")) as f_test:
      for line in f_test:
        white_test += 1
        code = line.strip().split()
        if test_bayes_model(bayes_model, code) == 0:
          white_correct += 1
    white_false = white_test - white_correct
    accuracy = (black_correct + white_correct) / (black_test + white_test)
    confusion_matrix = "[[{:4d}, {:4d}],\n [{:4d}, {:4d}]]".format(white_correct, white_false, black_false, black_correct)
    print("Accuracy: {}".format(accuracy))
    print(confusion_matrix)
    return accuracy, white_correct, white_false, black_correct, black_false


if __name__ == '__main__':
  run_app()
