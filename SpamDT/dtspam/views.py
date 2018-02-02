import os
import copy
import pickle

import numpy as np
from django.http import HttpResponseServerError
from django.shortcuts import render

# Create your views here.
from dt.my_dt import choose_best_split_feature_by_ID3, choose_best_split_feature_by_C45, no_info_gain_split_by_ID3, \
  create_data, create_tree, predict, accuracy, prf, predict_one

# 一些全局的变量
from dt.pre_process import get_all_files, get_index, vocabulary, load_vocab, make_feature, make_feature_for_test_email

split_choose_list = [no_info_gain_split_by_ID3, choose_best_split_feature_by_ID3, choose_best_split_feature_by_C45]
all_file_list = get_all_files('data')
spam_set = get_index('index')
using_file_list = all_file_list
global_vocab = load_vocab('vocab.txt', top_n=-1)

def index(request):
    if request.method == 'POST':
      print(request.POST)
      try:
        num_features = int(request.POST.get('name_num_features', default=100))
      except ValueError:
        num_features = 100
      print("开始提取特征，特征数:", num_features)
      vocab = load_vocab('vocab.txt', top_n=num_features)
      make_feature(using_file_list, vocab, spam_set, output='training.txt')
      try:
        train_test_ratio = float(request.POST.get('name_train_test_ratio', default=0.8))
      except ValueError:
        train_test_ratio = 0.8
      print("划分数据:", train_test_ratio)
      # 准备数据
      data, features = create_data('training.txt', 'vocab.txt')
      predict_labels = copy.copy(features)
      np.random.shuffle(data)
      data_length = len(data)
      # 交叉验证
      training_length = int(train_test_ratio * data_length)
      training_set, test_set = data[0:training_length].astype(int), data[training_length:].astype(int)

      try:
        max_depth = int(request.POST.get('name_max_depth', default=10))
      except ValueError:
        max_depth = 10
      print("决策树最大深度:", max_depth)
      try:
        split_choose = int(request.POST.get('name_split_choose', default=2))
      except ValueError:
        split_choose = 2
      print("决策树节点分类方法：", split_choose)
      split_choose = max(0, min(2, split_choose))
      print("开始构建决策树...")
      my_tree = create_tree(training_set, features, depth=0, max_depth=max_depth,
                            split_function=split_choose_list[split_choose])
      print("决策树构建完毕！")
      # 把决策树存起来
      with open('tree.pickle', 'wb') as f_tree:
        pickle.dump(my_tree, f_tree, pickle.HIGHEST_PROTOCOL)
      # 预测训练集
      predict_result = predict(test_set[:, :-1], my_tree, predict_labels)
      # 测试训练集准确率
      acc = accuracy(test_set, predict_result)
      precision, recall, f1 = prf(test_set, predict_result)

      ctx = {}
      ctx['last_num_features'] = num_features
      ctx['last_train_test_ratio'] = train_test_ratio
      ctx['last_max_depth'] = max_depth
      ctx['selected_{}'.format(split_choose)] = 1
      ctx['acc'] = f"{acc:.4f}"
      ctx['precision'] = f"{precision:.4f}"
      ctx['recall'] = f"{recall:.4f}"
      ctx['f1'] = f"{f1:.4f}"
      return render(request, 'index.html', context=ctx)
    return render(request, 'index.html')


def analysis(request):
  if request.method == 'POST':
    print(request.POST)
    print("加载决策树...")
    if os.path.exists('tree.pickle'):
      with open('tree.pickle', 'rb') as f_tree:
        my_tree = pickle.load(f_tree)
        content = request.POST.get('emailtext')
        if content:
          result = {}
          content_features = make_feature_for_test_email(content, word_dict=global_vocab)
          is_spam = predict_one(content_features, decision_tree=my_tree, features=list(global_vocab.keys()))
          if is_spam > 0:
            result['label'] = '垃圾邮件'
            result['value'] = 'spam'
            result['style'] = 'badge badge-danger'
          else:
            result['label'] = '正常邮件'
            result['value'] = 'ham'
            result['style'] = 'badge badge-success'
          return render(request, 'analysis.html', context={'result': result, 'last_email_text': content})
    else:
      return HttpResponseServerError("No models available! Train first!", content_type="text/plain", charset='utf-8')
  return render(request, 'analysis.html')
