import os
import re

import tensorflow as tf
import numpy as np
from rnnwriter import data_helper

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

def get_tensors(loaded_graph):
  inputs = loaded_graph.get_tensor_by_name("inputs:0")
  initial_state = loaded_graph.get_tensor_by_name("init_state:0")
  final_state = loaded_graph.get_tensor_by_name("final_state:0")
  probs = loaded_graph.get_tensor_by_name("probs:0")
  return inputs, initial_state, final_state, probs

def pick_word(probabilities, idx2vocab):
  # 根据最大概率选择
  num_word = np.random.choice(len(idx2vocab), p=probabilities)
  return idx2vocab[num_word]

def oneline_text_clean(input_string, token_dict):
  # 去掉首位空白
  line = input_string.strip()
  # 把中间的空格去掉
  pattern = re.compile(r"\s+")
  line = pattern.sub("", line)
  # 替换一些标点符号
  for k in token_dict:
    line = line.replace(k, "{}".format(token_dict[k]))
  return line

def api_write(graph, start_string: str, words=100):
  # 与下面的 main 一致，方便网页调用和返回
  _, vocab2idx, idx2vocab, token_dict = data_helper.load_preprocess()
  start_string = oneline_text_clean(start_string, token_dict)
  SEQ_LEN, _ = data_helper.load_params()
  LOAD_DIR = os.path.join((os.path.join(BASE_DIR, "writer_ckpt")), "writer")
  with tf.Session(graph=graph) as sess:
    loader = tf.train.import_meta_graph(LOAD_DIR + '.meta')
    loader.restore(sess, LOAD_DIR)
    # 通过名称获取缓存的 tensor
    input_text, initial_state, final_state, probs = get_tensors(graph)
    # 准备开始生成文本的输入
    gen_sentences = [w for w in start_string]
    prev_state = sess.run(initial_state, {input_text: np.array([[1]])})
    # 开始生成文本
    for n in range(words):
      # 因为模型的输入是 batch x seq，这里的 batch = 1，所以是一个 1 x seq_len 的列表
      # 每次预测生成的结果又作为下一次预测的输入
      dyn_input = [[vocab2idx[word] for word in gen_sentences[-SEQ_LEN:]]]
      dyn_seq_length = len(dyn_input[0])
      probabilities, prev_state = sess.run(
        [probs, final_state],
        {input_text: dyn_input, initial_state: prev_state})
      # 同理，probabilities 按照要求也是一个高维的结果，把 batch 这一维去掉
      squeeze_probs = probabilities[0]
      # 根据概率分布，选择对应位置的字
      pred_word = pick_word(squeeze_probs[dyn_seq_length - 1], idx2vocab)
      gen_sentences.append(pred_word)
    # 将标点符号还原
    novel = ''.join(gen_sentences)
    for key, token in token_dict.items():
      ending = ' ' if key in ['\n', '（', '“'] else ''
      novel = novel.replace(token, key)
    print(novel)
    return_val = novel.split('\n')
    return return_val

if __name__ == "__main__":
  # 加载辅助数据和模型
  _, vocab2idx, idx2vocab, token_dict = data_helper.load_preprocess()
  with open("vocab.txt", "w", encoding="utf-8") as f:
    for i in vocab2idx:
      f.write("{} - {}\n".format(i, vocab2idx[i]))
  SEQ_LEN, _ = data_helper.load_params()
  LOAD_DIR = os.path.join((os.path.join(BASE_DIR, "writer_ckpt")), "writer")
  # 生成文本的长度
  gen_length = 100
  # 指定开头，一定要是训练文件中出现过的字！
  start_string = "美好的一天"
  loaded_graph = tf.Graph()
  with tf.Session(graph=loaded_graph) as sess:
    # 加载保存过的 session
    loader = tf.train.import_meta_graph(LOAD_DIR + '.meta')
    loader.restore(sess, LOAD_DIR)
    # 通过名称获取缓存的 tensor
    input_text, initial_state, final_state, probs = get_tensors(loaded_graph)
    # 准备开始生成文本的输入
    gen_sentences = [w for w in start_string]
    prev_state = sess.run(initial_state, {input_text: np.array([[1]])})
    # 开始生成文本
    for n in range(gen_length):
      # 因为模型的输入是 batch x seq，这里的 batch = 1，所以是一个 1 x seq_len 的列表
      # 每次预测生成的结果又作为下一次预测的输入
      dyn_input = [[vocab2idx[word] for word in gen_sentences[-SEQ_LEN:]]]
      dyn_seq_length = len(dyn_input[0])
      probabilities, prev_state = sess.run(
        [probs, final_state],
        {input_text: dyn_input, initial_state: prev_state})
      # 同理，probabilities 按照要求也是一个高维的结果，把 batch 这一维去掉
      squeeze_probs = probabilities[0]
      # 根据概率分布，选择对应位置的字
      pred_word = pick_word(squeeze_probs[dyn_seq_length - 1], idx2vocab)
      gen_sentences.append(pred_word)
    # 将标点符号还原
    novel = ''.join(gen_sentences)
    # print(novel)
    for key, token in token_dict.items():
      ending = ' ' if key in ['\n', '（', '“'] else ''
      novel = novel.replace(token, key)
    print(novel)