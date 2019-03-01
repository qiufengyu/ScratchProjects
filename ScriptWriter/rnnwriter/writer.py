import os
import data_helper

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

def pre_writer(training_words = 100000):
  data_dir = os.path.join(os.path.join(BASE_DIR, "data"), "zh.txt")
  text_data = data_helper.load_text(data_dir)
  text_data = text_data[:training_words]
  lines_of_text = data_helper.text_clean(text_data)
  # 预处理数据，并保存数据
  data_helper.preprocess_text('\n'.join(lines_of_text), data_helper.token_lookup, data_helper.create_table)

import numpy as np
import tensorflow as tf
from tensorflow.contrib import seq2seq

# RNN 模型
def get_inputs():
  inputs = tf.placeholder(tf.int32, [None, None], name="inputs")
  targets = tf.placeholder(tf.int32, [None, None], name="targets")
  lr = tf.placeholder(tf.float32, name="learning_rate")
  return inputs, targets, lr


def get_a_cell(rnn_size, keep_prob):
  cell = tf.nn.rnn_cell.LSTMCell(rnn_size)
  # 通常使用 dropout 机制，防止过拟合
  drop = tf.nn.rnn_cell.DropoutWrapper(cell, output_keep_prob=keep_prob)
  return drop


def get_init_cell(batch_size, rnn_size, rnn_layers, keep_prob):
  # 创建 RNN(LSTM) cell
  # 建立多层 LSTM，提高性能
  cell = tf.nn.rnn_cell.MultiRNNCell([get_a_cell(rnn_size, keep_prob) for _ in range(rnn_layers)])
  # 初始化状态为 0
  init_state = cell.zero_state(batch_size, tf.float32)
  # 在计算图中添加名字，方便查找
  init_state = tf.identity(init_state, name="init_state")
  return cell, init_state


def get_embed(input_data, vocab_size, embed_dim):
  # 建立一个 embedding 变量
  embedding = tf.Variable(tf.truncated_normal([vocab_size, embed_dim], stddev=0.1),
                          dtype=tf.float32, name="embedding")
  return tf.nn.embedding_lookup(embedding, input_data, name="embed_data")


def build_rnn(cell, inputs):
  # 将输入经过模型，得到输出
  outputs, final_state = tf.nn.dynamic_rnn(cell, inputs, dtype=tf.float32)
  final_state = tf.identity(final_state, name="final_state")
  return outputs, final_state

def build_nn(cell, rnn_size, input_data, vocab_size, embed_dim):
  # 建立一个全连接层，计算 RNN 输出结果的 logits
  # 首先得到 embedding 后的输入
  embed = get_embed(input_data, vocab_size, embed_dim)
  # 计算 outputs 和 final state
  outputs, final_state = build_rnn(cell, embed)
  # 全连接层
  logits = tf.contrib.layers.fully_connected(outputs, vocab_size, activation_fn=None,
                                             weights_initializer=tf.truncated_normal_initializer(stddev=0.1),
                                             biases_initializer=tf.zeros_initializer())
  return logits, final_state


def get_batches(text_int, batch_size, seq_len):
  # 计算 batch 数量，最后不足的丢弃
  characters_per_batch = batch_size * seq_len
  num_batchs = len(text_int) // characters_per_batch
  # 数据的下一位即为结果，进行位移
  input_data = np.array(text_int[: num_batchs * characters_per_batch])
  target_data = np.array(text_int[1: num_batchs * characters_per_batch + 1])
  # 规模转换，符合 RNN 的输入输出
  inputs = input_data.reshape(batch_size, -1)
  targets = target_data.reshape(batch_size, -1)
  inputs = np.split(inputs, num_batchs, 1)
  targets = np.split(targets, num_batchs, 1)
  batches = np.array(list(zip(inputs, targets)))
  batches[-1][-1][-1][-1] = batches[0][0][0][0]
  return batches


if __name__ == "__main__":
  # 初始化一些数据，运行一次即可，保证已经有 pre.pickle 在就行
  print("初始化数据")
  TRAINING_WORDS = 300000
  pre_writer(training_words=TRAINING_WORDS)
  # 读取需要的数据
  text_int, vocab2idx, idx2vocab, token_dict = data_helper.load_preprocess()
  # rnn 模型的相关参数
  # 循环次数
  EPOCHES = 500
  # batch 大小
  BATCH_SIZE = 256
  # LSTM 层数
  RNN_LAYERS = 3
  # LSTM 层中的 unit 个数
  RNN_SIZE = 512
  # word2vec embedding 的大小
  EMBED_DIM = 512
  # 训练时序列长度
  SEQ_LEN = 64
  # 设置 dropout，防止过拟合
  KEEP_PROB = 0.8
  # 学习率
  LEARNING_RATE = 0.005
  # 训练信息显示间隔
  DISP = 8
  # 模型保存位置
  SAVE_DIR = os.path.join(os.path.join(BASE_DIR, "writer_ckpt"), "writer")
  print("初始化模型")
  train_graph = tf.Graph()
  with train_graph.as_default():
    # 文字的总数
    vocab_size = len(idx2vocab)
    # 模型输入
    input_text, targets, lr = get_inputs()
    # 数据的 shape
    input_data_shape = tf.shape(input_text)
    # 创建 rnn 的 cell 和初始状态节点，rnn 的 cell 已经包含了 lstm 和 dropout
    # 这里的 rnn_size 表示每个 lstm cell 中包含了多少的神经元
    cell, initial_state = get_init_cell(input_data_shape[0], RNN_SIZE, RNN_LAYERS, KEEP_PROB)
    # 创建计算 loss 和 finalstate 的节点
    logits, final_state = build_nn(cell, RNN_SIZE, input_text, vocab_size, EMBED_DIM)
    # 使用 softmax 计算最后的预测概率
    probs = tf.nn.softmax(logits, name="probs")
    # 计算损失
    cost = seq2seq.sequence_loss(
      logits,
      targets,
      tf.ones([input_data_shape[0], input_data_shape[1]]))
    optimizer = tf.train.AdamOptimizer(lr)
    # 裁剪梯度输出，最后都在 [-1, 1] 的范围内
    gradients = optimizer.compute_gradients(cost)
    capped_gradients = [(tf.clip_by_value(grad, -1.0, 1.0), var) for grad, var in gradients if grad is not None]
    train_op = optimizer.apply_gradients(capped_gradients)
  # 获得训练用的所有 batch
  batches = get_batches(text_int, BATCH_SIZE, SEQ_LEN)
  # 打开 session 开始训练，将上面创建的 graph 对象传递给 session
  with tf.Session(graph=train_graph) as sess:
    sess.run(tf.global_variables_initializer())
    for epoch_i in range(EPOCHES):
      state = sess.run(initial_state, {input_text: batches[0][0]})
      for batch_i, (x, y) in enumerate(batches):
        feed = {input_text: x, targets: y, initial_state: state, lr: LEARNING_RATE}
        train_loss, state, _ = sess.run([cost, final_state, train_op], feed)
        # 打印训练信息
        if (epoch_i * len(batches) + batch_i) % DISP == 0:
          print('Epoch {:>3} Batch {:>4}/{}   train_loss = {:.4f}'.format(
            epoch_i,
            batch_i,
            len(batches),
            train_loss))

    # 保存模型
    saver = tf.train.Saver()
    saver.save(sess, SAVE_DIR)
    print("训练完成")
    data_helper.save_params((SEQ_LEN, SAVE_DIR))
