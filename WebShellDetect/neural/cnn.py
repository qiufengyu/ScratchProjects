import os

import numpy as np
import tensorflow as tf

from WebShellDetect.settings import BASE_DIR
from shelldata import load_opcode_dict

class CNN():
  def __init__(self, sess, dim, max_len):
    self.sess = sess
    self.dim = dim   # 200 维词嵌入特征
    self.max_len = max_len # 最长的 opcode 序列长度
    self.labels = 2  # white/black 两类
    self.checkpoint_path=os.path.join(BASE_DIR, "checkpoint-cnn/cnn.ckpt")
    # 模型超参
    self.learning_rate = 0.001
    # 网络参数
    self.filters = [16, 8]
    self.kernel_size = [4, 2]
    self.reg_kernel = [0, 0]
    self.reg_prediction = 0.2
    self.dropout_rate = 0.5

  def init_all(self):
    # 初始化构建模型
    self.init_placeholders()
    self.init_variables()
    self.build_cnn()
    self.init_loss()
    self.init_optimizer()
    init = tf.global_variables_initializer()
    self.sess.run(init)
    self.saver = tf.train.Saver()

  def init_placeholders(self):
    # 模型的输入
    self.X = tf.placeholder(dtype=tf.int32, shape=[None, self.max_len], name="X")
    self.y = tf.placeholder(dtype=tf.float32, shape=[None, self.labels], name="y")
    self.dropout = tf.placeholder(dtype=tf.float32)

  def init_variables(self):
    self.word_embeddings = tf.eye(self.dim)
    self._X_input = tf.nn.embedding_lookup(self.word_embeddings, self.X) # None x max_len x dim (200)
    # 标准的 CNN 卷积层的输入需要一个四维张量，所以这里进行扩展，第四维这里需要设置为 1
    self.X_input = tf.expand_dims(self._X_input, axis=-1) # None x max_len x dim (200) x 1

  # 构建 CNN model
  def build_cnn(self):
    assert(len(self.filters) == len(self.kernel_size))
    # CNN 的层数
    num_layers = len(self.filters)
    self.nn_layers = [self.X_input]
    with tf.variable_scope("cnn"):
      for idx in range(num_layers):
        with tf.variable_scope("cnn-{}".format(idx)):
          # 卷积层
          conv = tf.layers.conv2d(inputs=self.nn_layers[-1], filters=self.filters[idx],
                                  kernel_size=[self.kernel_size[idx], self.kernel_size[idx]],
                                  padding='valid', activation=tf.nn.relu,
                                  kernel_initializer=tf.initializers.glorot_normal(),
                                  kernel_regularizer=tf.contrib.layers.l2_regularizer(float(self.reg_kernel[idx])),
                                  name="conv-{}".format(idx))
          # max 池化层
          pool = tf.layers.max_pooling2d(inputs=conv, pool_size=[2, 2], strides=[2, 2])
          # 输入层 X: None x len x dim x 1
          # conv: None x (1 + len - kernel_size[idx]) x (1 + dim - kernel_size[idx]) x filters[idx]
          # pool: None x (1 + len - kernel_size[idx])/2 x (1 + dim - kernel_size[idx])/2 x filters[idx]
          self.nn_layers.append(pool)
    self.pool_flat = tf.layers.flatten(self.nn_layers[-1])
    # 看一下 CNN 模型部分最后输出层的规模：
    print(self.pool_flat)
    # 根据模型性能，是否要引入 dropout 防止过拟合
    self.fc_dropout = tf.layers.dropout(inputs=self.pool_flat, rate=self.dropout, name="fc_dropout")
    # 用一个全连接层，预测所属类别
    with tf.variable_scope("cnn-pred"):
      self.prediction = tf.layers.dense(inputs=self.fc_dropout, units=self.labels, activation=tf.nn.sigmoid,
                                        kernel_initializer=tf.initializers.lecun_uniform(),
                                        kernel_regularizer=tf.contrib.layers.l2_regularizer(float(self.reg_prediction)),
                                        name="cnn-prediction")

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
    _, loss_val = self.sess.run([self.train_op, self.total_loss], feed_dict={self.X: b_X, self.y: b_y,
                                                                             self.dropout: self.dropout_rate})
    return loss_val

  def test(self, test_X, test_y):
    # 注意测试时，不引入 dropout
    prediction = self.sess.run(self.prediction, feed_dict={self.X: test_X, self.y: test_y, self.dropout: 0.0})
    return prediction

  def test_one_sample(self, test_X, test_y):
    t_X = np.expand_dims(test_X, axis=0)
    t_y = np.expand_dims(test_y, axis=0)
    prediction = self.sess.run(self.prediction, feed_dict={self.X: t_X, self.y: t_y, self.dropout: 0.0})
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

if __name__ == '__main__':
  opdict = load_opcode_dict()
  # 平均的 opcode 长度为 250 多，为了减少内存开销，提高运行速度，这里设置为 300
  max_len = 300
  raw_features = []
  raw_labels = []
  with open(os.path.join(BASE_DIR, "php.white.train.txt"), "r", encoding="utf-8") as f:
    for line in f:
      features = [opdict[x] for x in line.strip().split()]
      raw_labels.append([1, 0])
      raw_features.append(features)
  with open(os.path.join(BASE_DIR, "php.black.train.txt"), "r", encoding="utf-8") as f:
    for line in f:
      features = [opdict[x] for x in line.strip().split()]
      raw_features.append(features)
      raw_labels.append([0, 1])
  padded_features = tf.keras.preprocessing.sequence.pad_sequences(raw_features, dtype='int32', maxlen=max_len,
                                                                  padding='post', truncating='post',
                                                                  value=opdict["UNK"])
  complete_data = np.concatenate((padded_features, raw_labels), axis=1)
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
  complete_data = np.concatenate((padded_features, raw_labels), axis=1)
  training_samples = len(complete_data)
  test_samples = len(test_labels)
  # 训练时的一些设置
  batch_size = 50  # 一次训练 50 个样本
  # TODO: fix
  epoches = 200  # 最多训练 200 轮
  early_stop = 10  # 如果连续 10 轮性能没有提升，则提前终止
  dim = len(opdict)  # 统计出一共有 199 个 opcode，加一个保留字，输入的特征向量是 200 维
  with tf.Session() as sess:
    cnn = CNN(sess=sess, dim=dim, max_len=max_len)
    cnn.init_all()
    cnn.print_variables()
    # 训练过程
    best_acc = 0.0
    early_count = 0
    for epoch in range(epoches):
      batches = training_samples // batch_size + 1
      np.random.shuffle(complete_data)
      avg_loss = 0
      print("Epoch: {}".format(epoch + 1))
      # TODO: fix
      for b in range(batches):
        b_start = b * batch_size
        b_end = min(training_samples, b_start + batch_size)
        b_X = complete_data[b_start:b_end, :-2]
        b_y = complete_data[b_start:b_end, -2:]
        if b%10 == 0:
          print("B-{}/{}".format(b, batches), end=" ", flush=True)
        loss_val = cnn.train(b_X, b_y)
        avg_loss += loss_val / batches
      print("\nLoss: {:.6f}".format(avg_loss))
      # 运行测试数据的结果
      test_pred = cnn.test(padded_test_features, test_labels)
      ground_values = np.argmax(test_labels, axis=1)
      pred_values = np.argmax(test_pred, axis=1)
      acc, v1, v2, v3, v4 = cnn.test_helper(ground_values, pred_values)
      if acc > best_acc:
        best_acc = acc
        early_count = 0
        cnn.save()
      else:
        early_count += 1
      if early_count > early_stop:
        print("Early stop...")
        break
    cnn.restore()
    test_pred = cnn.test(padded_test_features, test_labels)
    ground_values = np.argmax(test_labels, axis=1)
    pred_values = np.argmax(test_pred, axis=1)
    acc = cnn.test_helper(ground_values, pred_values)
    # 单样本的测试
    opcodes = """DEFINED JMPNZ EXIT VERIFY_INSTANCEOF RETURN RECV RECV RECV IS_IDENTICAL JMPZ INIT_STATIC_METHOD_CALL SEND_VAR_EX SEND_VAR_EX SEND_VAR_EX DO_FCALL RETURN IS_IDENTICAL JMPZ ASSIGN FETCH_OBJ_R INIT_METHOD_CALL SEND_VAR_EX DO_FCALL CONCAT QM_ASSIGN QM_ASSIGN COUNT JMP FETCH_DIM_R FETCH_DIM_R IS_NOT_IDENTICAL JMPZ FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA JMP INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FETCH_DIM_W ASSIGN_DIM OP_DATA FETCH_DIM_IS ISSET_ISEMPTY_DIM_OBJ JMPNZ FETCH_OBJ_R INIT_METHOD_CALL SEND_VAR_EX DO_FCALL CONCAT FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FAST_CONCAT FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA IS_IDENTICAL JMPZ FETCH_DIM_IS ISSET_ISEMPTY_DIM_OBJ JMPNZ FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT ASSIGN_DIM OP_DATA FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA PRE_INC IS_SMALLER JMPNZ CONCAT FAST_CONCAT ASSIGN_CONCAT COUNT IS_IDENTICAL JMPZ FETCH_DIM_R QM_ASSIGN JMP INIT_FCALL SEND_VAL SEND_VAR DO_ICALL FAST_CONCAT FAST_CONCAT QM_ASSIGN ASSIGN_CONCAT INIT_FCALL SEND_REF SEND_VAR DO_ICALL RETURN RECV RECV RETURN"""
    test_feature = [opdict[x] for x in opcodes.strip().split()]
    feature_length = len(test_feature)
    if(feature_length < max_len):
      padding_feature = test_feature + [0] * (max_len - feature_length)
    else:
      padding_feature = test_feature[:max_len]
    test_label = [1.0, 0]
    test_value = cnn.test_one_sample(padding_feature, test_label)
    print(test_value)





