import os
from collections import deque
import tensorflow as tf

from deprecatedwriter.wordvec import vectorize_vocab
from deprecatedwriter.writerTrain import build_model

def generate_text(model, start_string, char2idx, idx2char, seq_len, num_generate=1000):
  """
  根据给定的词，开始创作
  :param model: rnn 生成模型
  :param start_string: 所给的开头
  :param char2idx: 字到 id 的映射，对输入进行编码
  :param idx2char: id 到字的映射，对结果进行解码
  :param num_generate: 生成的长度，默认为 1000 字
  :return:
  """
  raw_input_eval = [char2idx[s] for s in start_string]
  d = deque(raw_input_eval, maxlen=seq_len)
  input_eval = list(d)
  # rnn 模型输入需要二维的
  input_eval = tf.expand_dims(input_eval, 0)
  text_generated = [] # 存储生成的文本
  temperature = 0.5 # 越高则生成的结果越有"创意"
  model.reset_states()
  for i in range(num_generate):
    predictions = model(input_eval)
    # 从一个二维降到一维
    predictions = tf.squeeze(predictions, 0)
    predictions = predictions / temperature
    predicted_id = tf.multinomial(predictions, num_samples=1)[-1, 0].numpy()
    # print(predicted_id)
    # 更新下一个预测的输入
    d.append(predicted_id)
    input_eval = list(d)
    # print(input_eval)
    input_eval = tf.expand_dims(input_eval, 0)
    text_generated.append(idx2char[predicted_id])
  return (start_string + ''.join(text_generated))

def api_write(script, start_string, words):
  char2idx, idx2char, w2v_em = vectorize_vocab("wordvec.model")
  ckpt_dir = "./{}_ckpts".format(script)
  units = 128
  model = build_model(w2v_em, rnn_units=units, batch_size=1)
  model.load_weights(tf.train.latest_checkpoint(ckpt_dir))
  model.build(tf.TensorShape([1, None]))
  model.summary()
  res = generate_text(model, start_string, char2idx, idx2char, seq_len=32, num_generate=words)
  return res

if __name__ == "__main__":
  res = api_write("Titanic", "阳光甲板", 20)
  print(res)
