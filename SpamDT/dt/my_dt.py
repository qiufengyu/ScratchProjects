import math
import copy
from collections import Counter

import numpy as np

def create_data(input_file, word_file):
  data = np.loadtxt(input_file, delimiter=',')
  features = []
  with open(word_file, 'r', encoding='utf-8') as f:
    while True:
      line = f.readline()
      if not line:
        break
      line = line.strip()
      features.append(line)
  return data, features

def split_data(data, axis, value):
  """
  根据给定的特征划分数据集
  :param data: 原始数据集
  :param axis: 划分数据集的特征（对应第几维）
  :param value: 特征划分的值
  :return: 符合该特征的所有实例，同时删去这一维特征的子数据
  """
  ret_list = []
  # print(data.shape)
  # print(f"axis = {axis}, value={value}")
  for sample in data:
    if sample[axis] == value:
      reduced_data = np.delete(sample, obj=axis)
      ret_list.append(reduced_data)
  return np.array(ret_list)

def cal_shannon_entropy(data):
  """
  计算训练数据中的（香农）熵
  :param data:
  :return:
  """
  num_of_samples = len(data)
  all_labels = data[: ,-1].tolist()
  label_counts = Counter(all_labels)
  shannon_entropy = 0.0
  for k, v in label_counts.most_common():
    prob = float(v) / num_of_samples
    shannon_entropy -= prob * math.log(prob, 2)

  return shannon_entropy

def cal_conditional_entropy(data, i, feature_list, unique_values):
  """
  给定 x_i，计算条件熵
  :param data: 数据集
  :param i: 维度，i
  :param feature_list: 特征列表
  :param unique_values: 数据集特征集合
  :return: 条件熵
  """
  cond_entropy = 0.0
  for value in unique_values:
    sub_data = split_data(data, i, value)
    prob = len(sub_data) / float(len(data))
    cond_entropy += prob * cal_shannon_entropy(sub_data)
  return cond_entropy

def cal_info_gain(data, base_entropy, i):
  """
  计算信息增益
  :param data: 数据集
  :param base_entropy: 数据集中是否为垃圾邮件的信息熵
  :param i: 特征维度 i
  :return: 该特征维度 i 对数据集的信息增益
  """
  feature_list = [sample[i] for sample in data]
  unique_values = set(feature_list)
  new_entropy = cal_conditional_entropy(data, i, feature_list, unique_values)
  info_gain = base_entropy - new_entropy
  return info_gain

def majority_count(class_list):
  """
  返回出现次数最多的分类名称
  :param class_list: 分类的列表
  :return: 出现次数最多的分类名称 （0 或 1）
  """
  class_count = Counter(class_list)
  return class_count.most_common()[0][0]

def choose_best_split_feature_by_ID3(data):
  """
  运用 ID3 决策树的方法，基于最大信息增益划分，选择最好的数据集划分
  :param data:
  :return:
  """
  num_features = data.shape[1] - 1 # 最后一维是分类结果，不是特征！
  base_entropy = cal_shannon_entropy(data)
  best_info_gain = 0.0
  best_feature = -1
  for i in range(num_features):
    info_gain = cal_info_gain(data, base_entropy, i)
    if info_gain > best_info_gain:
      best_info_gain = info_gain
      best_feature = i
  return best_feature

def cal_info_gain_rate(data, base_entropy, i):
  """
  计算信息增益比，用于 C4.5 决策树划分
  :param data: 数据
  :param base_entropy: 数据中分类的信息熵
  :param i: 特征维度
  :return: 特征 i 对数据集的信息增益
  """
  num_samples = data.shape[0]
  shannon_entropy = 0.0
  all_labels = data[: ,-1].tolist()
  label_counts = Counter(all_labels)
  for k, v in label_counts.most_common():
    prob = float(v) / num_samples
    shannon_entropy -= prob * math.log(prob, 2)
  return cal_info_gain(data, base_entropy, i) / shannon_entropy

def choose_best_split_feature_by_C45(data):
  """
  运用 C4.5 决策树，根据最大信息增益比进行数据划分
  :param data:
  :return:
  """
  num_features = data.shape[1] - 1
  base_entropy = cal_shannon_entropy(data)
  best_info_gain_rate = 0.0
  best_feature = -1
  for i in range(num_features):
    info_gain_rate = cal_info_gain_rate(data, base_entropy, i)
    if info_gain_rate > best_info_gain_rate:
      best_info_gain_rate = info_gain_rate
      best_feature = i
  return best_feature

def no_info_gain_split_by_ID3(data):
  """
  不用信息增益，直接用熵大小，即最大熵
  :param data:
  :return:
  """
  num_features = data.shape[1] - 1
  best_cond_entropy = 0.0
  best_feature = -1
  for i in range(num_features):
    feature_list = [sample[i] for sample in data]
    unique_values = set(feature_list)
    cond_entropy = cal_conditional_entropy(data, i, feature_list, unique_values=unique_values)
    if cond_entropy >= best_cond_entropy:
      best_cond_entropy = cond_entropy
      best_feature = i
  return best_feature

def create_tree(data, features, depth, max_depth=10, split_function=no_info_gain_split_by_ID3):
  """
  创建决策树
  :param data: 数据特
  :param features: 每一维特征名称，这里是字符
  :param split_function: 决策树节点分类的标准
  :param depth: 当前节点的深度
  :param max_depth: 最大深度，防止过拟合，进行剪枝
  :return: 决策树的根节点
  """
  class_list = data[:,-1].tolist() # 类别列表
  class_counter = Counter(class_list)
  if class_counter.most_common()[0][1] == len(class_list): # 统计属于列别 class_list[0] 的个数
    return class_list[0] # 当类别完全相同则停止继续划分
  if depth >= max_depth or data.shape[1] == 1:
    # 当只有一种特征或者是
    # 遍历所有的样本，返回出现次数最多的分类结果
    return majority_count(class_list) # 返回分类最多的结果
  best_feature = split_function(data)
  best_label_name = features[best_feature]
  print('  ' * depth, "当前深度：", depth, "，选择的特征是：", best_label_name)
  my_tree = {best_label_name: {}} # 用一个字典存储决策树的结构
  del (features[best_feature])
  # 找需要分类的特征子集
  feature_values = [sample[best_feature] for sample in data]
  unique_values = set(feature_values)
  for value in unique_values:
    sub_labels = features[:] # 复制已经删去一个用来分类的特征
    my_tree[best_label_name][value] = create_tree(split_data(data, best_feature, value), sub_labels,
                                                  depth=depth+1, max_depth=max_depth,split_function=split_function)
  return my_tree

def map_feature_name_to_index(map, features):
  # print(map)
  for k in map.keys():
    for i in range(len(features)):
      if k == features[i]:
        return k, i

def predict_one(test_data, decision_tree, features):
  # 获得决策树节点的下标
  feature_name, feature_index = map_feature_name_to_index(decision_tree, features)
  if test_data[feature_index] in decision_tree[feature_name]:
    tree = decision_tree[feature_name][test_data[feature_index]]
  else:
    last_feature = sorted(decision_tree[feature_name].keys())[-1]
    sorted_keys = sorted(decision_tree[feature_name].keys())
    for sk in sorted_keys:
      if decision_tree[feature_name][sk] <= test_data[feature_index]:
        last_feature = sk
    tree = decision_tree[feature_name][last_feature]
  if not isinstance(tree, dict):
    return tree
  else:
    return predict_one(test_data, tree, features)

def predict(test_set, decision_tree, features):
  result_list = []
  for sample in test_set:
    result_list.append(predict_one(sample, decision_tree, features))
  return result_list

def accuracy(data, pred):
  """
  计算准确率
  :param data:
  :param pred:
  :return:
  """
  total_count = len(data)
  correct_count = 0
  for i in range(total_count):
    if data[i, -1] == pred[i]:
      correct_count += 1
  return correct_count / total_count * 100.0

def prf(data, pred):
  total_count = len(data)
  correct_count = 0
  ground_count = 0
  predict_count = 0
  for i in range(total_count):
    if data[i, -1] > 0:
      if pred[i] > 0:
        correct_count += 1
      ground_count += 1
    if pred[i] > 0:
      predict_count += 1
  p = correct_count / predict_count * 100.0
  r = correct_count / ground_count * 100.0
  f = 2 * p * r / (p + r)
  return p, r, f
