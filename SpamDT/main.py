import os
import copy
import shutil
import pathlib
import pickle

import numpy as np

from dt.pre_process import get_all_files, vocabulary, load_vocab, get_index, make_feature, make_feature_for_test_email
from dt.prepare import clean_data, copy_index
from dt.scipy_dt import scipy_dt, get_feature_names
from dt.my_dt import *
from dt.view_dt import create_plot

if __name__ == '__main__':
  # 将 trec06c 数据集的压缩包在根目录下解压，运行下面 5 行代码即可
  # 只需要运行一次！！！
  # if pathlib.Path('data').exists():
    # shutil.rmtree('data/')
  # clean_data('trec06c/data')
  # copy_index('trec06c/full/index', 'index')
  # shutil.rmtree('trec06c')

  # 数据生成如何生成和转化，参考上面的代码
  # 上面的代码，会得到生成一个 data 文件夹，里面有 50000+ 样本
  # 为了提高程序运行的效率，暂时先用约 6000 条数据作为训练集
  # 要求是 4000+ 条数据，可以自行调整
  # 为了减小文件的大小，解压缩这么多文件也很耗时
  # 所附的代码中也只有约 6000 条数据

  # 正式开始特征抽取等工作
  # all_file_list = get_all_files('data')
  # print("合计有效文件数：", len(all_file_list))
  # spam_set = get_index('index')

  # 如果使用了全部的数据集，也可以指定哪些为训练、哪些为测试的
  # 如 using_file_list = all_file_list[0:5000] 表示使用前 5000 条数据
  # using_file_list = all_file_list

  # 生成字典运行一次即可，避免重复劳动
  # 为了加快训练的速度，通常只取最频繁的 100 词
  # vocabulary(using_file_list)

  # vocab = load_vocab('vocab.txt', top_n=100)
  # print(len(vocab))
  # make_feature(using_file_list, vocab, spam_set, output='training.txt')
  # 这上面从第 33 行开始的代码，做的是特征抽取，基于词袋模型构建的
  # 在测试阶段，不必再运行了

  """
  这一段代码是调用已有的决策树算法，仅用作对比用
  """
  # 如果要可视化，需要安装 Graphviz，把可执行文件的路径放进来
  # Windows 环境需要自己安装这个软件，macOS 可以通过 brew install graphviz 安装
  # 要不要这部分问题不大，实在不行可以不管
  # os.environ["PATH"] += os.pathsep + 'D:\\Program Files (x86)\\Graphviz2.38\\bin'
  # 先用 sklearn 工具包中的决策树尝试计算一下
  # vocab = load_vocab('vocab.txt', top_n=100)
  # data, feature_names = create_data('training.txt', 'vocab.txt')
  # scipy_dt(data, feature_names[0:len(vocab)])

  """
  下面是自己实现的决策树分类算法
  """
  # 测试不同数据集的决策树构建
  data, features = create_data('training.txt', 'vocab.txt')
  predict_labels = copy.copy(features)

  np.random.shuffle(data)
  data_length = len(data)
  # 定义交叉验证比例
  rate = 0.8
  training_length = int(rate * data_length)

  training_set, test_set = data[0:training_length].astype(int), data[training_length:].astype(int)
  # 可以设置最大深度 max_depth
  # 选择基于最大信息熵/最大信息增益/信息增益比等方法选择属性构建决策树
  # split_function 取值如下：
  # 1. choose_best_split_feature_by_ID3 -> 最大信息增益
  # 2. choose_best_split_feature_by_C45 -> 信息增益比
  # 3. no_info_gain_split_by_ID3 -> 最大信息熵 （效果很差）
  print("开始构建决策树...")
  my_tree = create_tree(training_set, features, depth=0, max_depth=5, split_function=choose_best_split_feature_by_C45)
  print("决策树构建完毕！")
  # 把决策树存起来
  with open('tree.pickle', 'wb') as f_tree:
    pickle.dump(my_tree, f_tree, pickle.HIGHEST_PROTOCOL)

  # 加载决策树
  print("加载决策树")
  with open('tree.pickle', 'rb') as f_tree:
    new_tree = pickle.load(f_tree)
  print(new_tree)

  # 预测训练集
  predict_result = predict(test_set[:, :-1], new_tree, predict_labels)

  # 测试训练集准确率
  acc = accuracy(test_set, predict_result)
  print("决策树分类精确度: {:.4f}%".format(acc))

  p, r, f = prf(test_set, predict_result)
  print(f"准确率：{p:.4f}%，召回率：{r:.4f}%，F值：{f:.4f}%")

  print('='*80)
  print("单个实例测试：")
  vocab = load_vocab('vocab.txt')
  mail = '专业诚信代开发票，联系QQ: 123456。本公司竭诚为您服务！'
  mail_feature = make_feature_for_test_email(mail, word_dict=vocab)
  result = predict_one(mail_feature, new_tree, predict_labels)
  print('邮件正文：', mail)
  if result > 0:
    print('垃圾邮件')
  else:
    print('正常邮件')

  # 绘制决策树
  from pylab import *
  # 下面的设置是为了图中能够正确显示中文
  # Windows 10 环境默认好像是 OK 的，macOS 需要设置一下
  # 参考：https://www.zhihu.com/question/25404709
  mpl.rcParams['font.sans-serif'] = ['SimHei']  # 指定默认字体
  mpl.rcParams['axes.unicode_minus'] = False  # 解决保存图像时负号'-'显示为方块的问题

  create_plot(new_tree)

  print('end')
