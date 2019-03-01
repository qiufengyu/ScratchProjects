import os

import numpy as np
import tensorflow as tf

from WebShellDetect.settings import BASE_DIR
from neural.feature import *


class MLP():
  def __init__(self, sess, dim):
    self.sess = sess
    self.dim = dim   # 200 维的输入特征
    self.labels = 2  # white/black 两类
    self.checkpoint_path=os.path.join(BASE_DIR, "checkpoint-mlp/mlp.ckpt")
    # 模型超参
    self.learning_rate = 0.0005
    # 网络参数
    self.layers = [64, 32] # 每一层的维度
    self.reg_layers = [0, 0] # 正则化因子
    self.reg_prediction = 0.2

  def init_all(self):
    # 初始化构建模型
    self.init_placeholders()
    self.build_mlp()
    self.init_loss()
    self.init_optimizer()
    init = tf.global_variables_initializer()
    self.sess.run(init)
    self.saver = tf.train.Saver()

  def init_placeholders(self):
    # 模型的输入
    self.X = tf.placeholder(dtype="float", shape=[None, self.dim], name="X")
    self.y = tf.placeholder(dtype="float", shape=[None, self.labels], name="y")

  # 构建 MLP model
  def build_mlp(self):
    assert(len(self.layers) == len(self.reg_layers))
    # MLP 的层数
    num_layers = len(self.layers)
    self.mlp_layers = [self.X]
    with tf.variable_scope("mlp"):
      for idx in range(num_layers):
        with tf.variable_scope("mlp-{}".format(idx)):
          layer = tf.layers.dense(inputs=self.mlp_layers[-1], units=self.layers[idx], activation=tf.nn.relu,
                                  use_bias=True, kernel_initializer=tf.initializers.glorot_normal(),
                                  kernel_regularizer=tf.contrib.layers.l2_regularizer(float(self.reg_layers[idx])),
                                  name="layer-{}".format(idx))
          self.mlp_layers.append(layer)
    with tf.variable_scope("mlp-pred"):
      self.prediction = tf.layers.dense(inputs=self.mlp_layers[-1], units=self.labels, activation=tf.nn.sigmoid,
                                   use_bias=True, kernel_initializer=tf.initializers.lecun_uniform(),
                                   kernel_regularizer=tf.contrib.layers.l2_regularizer(float(self.reg_prediction)),
                                   name="mlp-pred")

  def init_loss(self):
    # 定义模型优化的损失函数
    self.reg_loss = tf.reduce_sum(tf.get_collection(tf.GraphKeys.REGULARIZATION_LOSSES))
    self.xe_loss = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits_v2(logits=self.prediction, labels=self.y))
    self.total_loss = self.reg_loss + self.xe_loss

  def init_optimizer(self):
    # 模型优化
    self.optimizer = tf.train.AdamOptimizer(learning_rate=self.learning_rate)
    self.train_op = self.optimizer.minimize(self.total_loss)

  # 供外部 main 调用，训练模型
  def train(self, b_X, b_y):
    _, loss_val = self.sess.run([self.train_op, self.total_loss], feed_dict={self.X: b_X, self.y: b_y})
    return loss_val

  def test(self, test_X, test_y):
    prediction = self.sess.run(self.prediction, feed_dict={self.X: test_X, self.y: test_y})
    return prediction

  def test_one_sample(self, test_X, test_y):
    t_X = np.expand_dims(test_X, axis=0)
    t_y = np.expand_dims(test_y, axis=0)
    prediction = self.sess.run(self.prediction, feed_dict={self.X: t_X, self.y: t_y})
    result = np.argmax(prediction, axis=1)[0]
    return result

  def test_helper(self, ground_values, pred_values):
    compare = (ground_values == pred_values).sum()
    test_samples = len(ground_values)
    accuracy = compare / test_samples
    print("Accuracy: {}".format(accuracy))
    white_black_split = (ground_values == 0).sum()
    white_correct = 0
    white_wrong = 0
    for i in range(white_black_split):
      if ground_values[i] == pred_values[i]:
        white_correct += 1
      else:
        white_wrong += 1
    black_correct = 0
    black_wrong = 0
    for i in range(white_black_split + 1, test_samples):
      if ground_values[i] == pred_values[i]:
        black_correct += 1
      else:
        black_wrong += 1
    confusion_matrix = "[[{:4d}, {:4d}],\n [{:4d}, {:4d}]]".format(white_correct, white_wrong, black_wrong,
                                                                   black_correct)
    print(confusion_matrix)
    return (accuracy, white_correct, white_wrong, black_correct, black_wrong)

  def save(self):
    self.saver.save(self.sess, self.checkpoint_path)

  def restore(self):
    self.saver.restore(self.sess, self.checkpoint_path)

  def print_variables(self):
    print("All trainable variables:")
    variables = tf.trainable_variables()
    for v in variables:
      print(v)
    print("Regularized variables: ")
    weights = tf.get_collection(tf.GraphKeys.REGULARIZATION_LOSSES)
    if len(weights) == 0:
      print("None")
    else:
      for w in weights:
        print(w)

def run_app(single=False):
  opdict = load_opcode_dict()
  idf = load_idf()
  # 读取数据，需要转化为 numpy 的矩阵
  feature_array = []
  training_samples = 0
  with open(os.path.join(BASE_DIR, "php.white.train.txt"), "r", encoding="utf-8") as f:
    for line in f:
      training_samples += 1
      feature_i = make_feature_tfidf(opdict, line.strip(), idf) + [1.0, 0.0]
      feature_array.append(feature_i)
  with open(os.path.join(BASE_DIR, "php.black.train.txt"), "r", encoding="utf-8") as f:
    for line in f:
      training_samples += 1
      feature_i = make_feature_tfidf(opdict, line.strip(), idf) + [0.0, 1.0]
      feature_array.append(feature_i)
  np_features = np.array(feature_array)
  # 测试数据
  test_feature_array = []
  test_label_array = []
  test_samples = 0
  with open(os.path.join(BASE_DIR, "php.white.test.txt"), "r", encoding="utf-8") as f:
    for line in f:
      test_samples += 1
      feature_i = make_feature_tfidf(opdict, line.strip(), idf)
      test_label_array.append([1.0, 0.0])
      test_feature_array.append(feature_i)
  with open(os.path.join(BASE_DIR, "php.black.test.txt"), "r", encoding="utf-8") as f:
    for line in f:
      test_samples += 1
      feature_i = make_feature_tfidf(opdict, line.strip(), idf)
      test_label_array.append([0.0, 1.0])
      test_feature_array.append(feature_i)
  # 训练时的一些设置
  batch_size = 100 # 一次训练 100 个样本
  display_step = 2 # 每隔 2 次显示一次训练信息
  epoches = 200 # 最多训练 200 论
  early_stop = 10 # 如果连续 5 轮性能没有提升，则提前终止
  dim = len(opdict)# 统计出一共有 199 个 opcode，加一个保留字，输入的特征向量是 200 维
  # 读取一些辅助数据
  with tf.Session() as sess:
    # 初始化，输出相关信息
    mlp = MLP(sess=sess, dim=dim)
    mlp.init_all()
    mlp.print_variables()
    # 训练过程
    best_acc = 0.0
    early_count = 0
    for epoch in range(epoches):
      batches = training_samples // batch_size + 1
      np.random.shuffle(np_features)
      avg_loss = 0
      for b in range(batches):
        b_start = b * batch_size
        b_end = min(training_samples, b_start + batch_size)
        b_X = np_features[b_start:b_end, :-2]
        b_y = np_features[b_start:b_end, -2:]
        loss_val = mlp.train(b_X, b_y)
        avg_loss += loss_val / batches
      if epoch % display_step == 0:
        print("Epoch: {}, loss: {:.6f}".format(epoch+1, avg_loss))
      # 运行测试数据的结果
      test_pred = mlp.test(test_feature_array, test_label_array)
      ground_values = np.argmax(test_label_array, axis=1)
      pred_values = np.argmax(test_pred, axis=1)
      acc, v1, v2, v3, v4 = mlp.test_helper(ground_values, pred_values)
      if acc > best_acc:
        best_acc = acc
        mlp.save()
      else:
        early_count += 1
      if early_count > early_stop:
        break
    mlp.restore()
    if single:
      # 单样本的测试
      opcodes = """DEFINED JMPNZ EXIT VERIFY_INSTANCEOF RETURN RECV RECV RECV IS_IDENTICAL JMPZ INIT_STATIC_METHOD_CALL SEND_VAR_EX SEND_VAR_EX SEND_VAR_EX DO_FCALL RETURN IS_IDENTICAL JMPZ ASSIGN FETCH_OBJ_R INIT_METHOD_CALL SEND_VAR_EX DO_FCALL CONCAT QM_ASSIGN QM_ASSIGN COUNT JMP FETCH_DIM_R FETCH_DIM_R IS_NOT_IDENTICAL JMPZ FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA JMP INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FETCH_DIM_W ASSIGN_DIM OP_DATA FETCH_DIM_IS ISSET_ISEMPTY_DIM_OBJ JMPNZ FETCH_OBJ_R INIT_METHOD_CALL SEND_VAR_EX DO_FCALL CONCAT FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FAST_CONCAT FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA IS_IDENTICAL JMPZ FETCH_DIM_IS ISSET_ISEMPTY_DIM_OBJ JMPNZ FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT ASSIGN_DIM OP_DATA FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA PRE_INC IS_SMALLER JMPNZ CONCAT FAST_CONCAT ASSIGN_CONCAT COUNT IS_IDENTICAL JMPZ FETCH_DIM_R QM_ASSIGN JMP INIT_FCALL SEND_VAL SEND_VAR DO_ICALL FAST_CONCAT FAST_CONCAT QM_ASSIGN ASSIGN_CONCAT INIT_FCALL SEND_REF SEND_VAR DO_ICALL RETURN RECV RECV RETURN"""
      test_feature = make_feature_tfidf(opdict, opcodes, idf)
      test_label = [1.0, 0]
      test_value = mlp.test_one_sample(test_feature, test_label)
      print(test_value)
      return (test_value, 0, 0, 0, 0)
    else:
      test_pred = mlp.test(test_feature_array, test_label_array)
      ground_values = np.argmax(test_label_array, axis=1)
      pred_values = np.argmax(test_pred, axis=1)
      acc, v1, v2, v3, v4 = mlp.test_helper(ground_values, pred_values)
      return (acc, v1, v2, v3, v4)

if __name__ == '__main__':
  run_app()

