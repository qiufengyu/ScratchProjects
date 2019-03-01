import os
import time

import numpy as np
import tensorflow as tf
from django.shortcuts import render

from WebShellDetect.settings import BASE_DIR
from bayes import bayes
from bayes.bayes import load_bayes_model, test_bayes_model
from neural.feature import *
from neural.mlp import MLP, run_app
from neural.cnn import CNN
from shelldata import load_opcode_dict, get_opcode

# 初始化模型数据
opdict = load_opcode_dict()
idf = load_idf()
max_len = 300

# Create your views here.
def index(request):
  if request.method == 'POST':
    option = int(request.POST["method"])
    ctx = {}
    ctx["option"] = option
    if option == 1:
      # 朴素贝叶斯
      accuracy, white_correct, white_wrong, black_correct, black_wrong = bayes.run_app()
      ctx["black_correct"] = black_correct
      ctx["black_wrong"] = black_wrong
      ctx["white_correct"] = white_correct
      ctx["white_wrong"] = white_wrong
      ctx["accuracy"] = "{:.4f}".format(accuracy*100)
      return render(request, "index.html", context=ctx)
    elif option == 2:
      # 使用多层感知器训练和预测
      accuracy, white_correct, white_wrong, black_correct, black_wrong = run_app()
      ctx["black_correct"] = black_correct
      ctx["black_wrong"] = black_wrong
      ctx["white_correct"] = white_correct
      ctx["white_wrong"] = white_wrong
      ctx["accuracy"] = "{:.4f}".format(accuracy * 100)
      return render(request, "index.html", context=ctx)
    elif option == 3:
      test_features = []
      test_labels = []
      with open(os.path.join(BASE_DIR, "php.white.test.txt"), "r", encoding="utf-8") as f:
        for line in f:
          features = [opdict[x] for x in line.strip().split()]
          test_labels.append([1, 0])
          test_features.append(features)
      with open(os.path.join(BASE_DIR, "php.black.test.txt"), "r", encoding="utf-8") as f:
        for line in f:
          features = [opdict[x] for x in line.strip().split()]
          test_features.append(features)
          test_labels.append([0, 1])
      padded_test_features = tf.keras.preprocessing.sequence.pad_sequences(test_features, dtype='int32', maxlen=max_len,
                                                                           padding='post', truncating='post',
                                                                           value=opdict["UNK"])
      g2 = tf.Graph()
      with tf.Session(graph=g2) as cnn_sess:
        cnn = CNN(sess=cnn_sess, dim=len(opdict), max_len=max_len)
        cnn.init_all()
        cnn.restore()
        test_pred = cnn.test(padded_test_features, test_labels)
        ground_values = np.argmax(test_labels, axis=1)
        pred_values = np.argmax(test_pred, axis=1)
        accuracy, white_correct, white_wrong, black_correct, black_wrong = cnn.test_helper(ground_values, pred_values)
        ctx["black_correct"] = black_correct
        ctx["black_wrong"] = black_wrong
        ctx["white_correct"] = white_correct
        ctx["white_wrong"] = white_wrong
        ctx["accuracy"] = "{:.4f}".format(accuracy * 100)
        return render(request, "index.html", context=ctx)
    else:
      ctx["message"] = "对应的模型存在异常，请线下检查！"
      return render(request, "index.html", context=ctx)
  return render(request, "index.html")

def analysis(request):
  if request.method == 'POST':
    ctx = {}
    option = int(request.POST["method"])
    phpcodes = str(request.POST["phpcode"])
    ctx["option"] = option
    ctx["lastcode"] = phpcodes
    t = time.time()
    ts = int(t * 100)
    with open(os.path.join(BASE_DIR, "testcases/p{}.php".format(ts)), "w", encoding="utf-8") as f:
      f.write(phpcodes)
    phpfile = os.path.join(BASE_DIR, "testcases/p{}.php".format(ts))
    opcodes = get_opcode(phpfile)
    if len(opcodes) > 2:
      if option == 1:
        bayes_model = load_bayes_model()
        r = test_bayes_model(bayes_model, opcodes, ifopcode=True)
        if r == 0:
          ctx["result"] = "正常脚本"
        else:
          ctx["result"] = "有害脚本"
        return render(request, 'analysis.html', context=ctx)
      elif option == 2:
        test_feature = make_feature_tfidf_v2(opdict, opcodes, idf)
        test_label = [1.0, 0]
        try:
          g1 = tf.Graph()
          with tf.Session(graph=g1) as mlp_sess:
            mlp = MLP(sess=mlp_sess, dim=len(opdict))
            mlp.init_all()
            mlp.restore()
            r = mlp.test_one_sample(test_feature, test_label)
          if r == 0:
            ctx["result"] = "正常脚本"
          else:
            ctx["result"] = "有害脚本"
          return render(request, 'analysis.html', context=ctx)
        except:
          ctx["message"] = "模型加载错误！"
          return render(request, 'analysis.html', context=ctx)
      elif option == 3:
        test_feature = [opdict[x] for x in opcodes]
        feature_length = len(test_feature)
        if (feature_length < max_len):
          padding_feature = test_feature + [0] * (max_len - feature_length)
        else:
          padding_feature = test_feature[:max_len]
        try:
          g2 = tf.Graph()
          with tf.Session(graph=g2) as cnn_sess:
            cnn = CNN(sess=cnn_sess, dim=len(opdict), max_len=max_len)
            cnn.init_all()
            cnn.restore()
            test_label = [1.0, 0]
            r = cnn.test_one_sample(padding_feature, test_label)
          if r == 0:
            ctx["result"] = "正常脚本"
          else:
            ctx["result"] = "有害脚本"
          return render(request, 'analysis.html', context=ctx)
        except:
          ctx["message"] = "模型加载错误！"
          return render(request, 'analysis.html', context=ctx)
    else:
      ctx["message"] = "请输入合法的 PHP 脚本！"
      return render(request, 'analysis.html', context=ctx)
  return render(request, 'analysis.html')

def about(request):
  return render(request, 'about.html')