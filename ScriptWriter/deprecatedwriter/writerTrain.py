import os
import tensorflow as tf

from zhwriter.wordvec import text_as_int, vectorize_vocab

tf.enable_eager_execution()

def split_input_target(chunk):
  input_text = chunk[:-1]
  target_text = chunk[1:]
  return input_text, target_text

def loss(labels, logits):
  return tf.keras.losses.sparse_categorical_crossentropy(labels, logits)

def build_model(w2v_em, rnn_units, batch_size):
  vocab_size, embedding_dim = w2v_em.shape[0], w2v_em.shape[1]
  model = tf.keras.Sequential([
    tf.keras.layers.Embedding(vocab_size, embedding_dim,
                              batch_input_shape=[batch_size, None],
                              weights=[w2v_em]),
    tf.keras.layers.GRU(rnn_units,
                        return_sequences=True,
                        recurrent_activation='sigmoid',
                        recurrent_initializer='glorot_uniform',
                        stateful=True
                        ),
    tf.keras.layers.GRU(rnn_units,
                        return_sequences=True,
                        recurrent_activation='sigmoid',
                        recurrent_initializer='glorot_uniform',
                        stateful=True
                        ),
    tf.keras.layers.Dense(vocab_size)
  ])
  return model

if __name__ == '__main__':
  # 1. 准备工作：
  # 1.1 加载预训练的 word2vec 模型和单词（字）表，以及对应的 word2vec embeddings
  char2idx, idx2char, w2v_em = vectorize_vocab("wordvec.model")
  # 1.2 读入训练数据，并转换
  script = "Titanic"
  text = text_as_int("./data/{}.txt".format(script), char2idx)
  text_int = text# [:10000]
  examples_per_epoch = len(text_int)
  # 1.3 创建训练样本和目标
  seq_length = 16
  chunks = tf.data.Dataset.from_tensor_slices(text_int).batch(seq_length+1, drop_remainder=True)
  dataset = chunks.map(split_input_target)
  # 1.4 数据切分与随机重排
  BATCH_SIZE = 64
  BUFFER_SIZE = 10000
  dataset = dataset.shuffle(BUFFER_SIZE).batch(BATCH_SIZE, drop_remainder=True)

  # 2. 实例化模型
  units = 128
  model = build_model(w2v_em, rnn_units=units, batch_size=BATCH_SIZE)
  # 选择一条数据试一试
  for input_ex, target_ex in dataset.take(1):
    ex_predictions = model(input_ex)
    print("Precition shape: ", ex_predictions.shape)
    example_loss = loss(target_ex, ex_predictions)
    print("scalar loss: ", example_loss.numpy().mean())
  model.summary()

  # 3. 训练模型
  optimizer = tf.train.AdadeltaOptimizer()
  model.compile(optimizer=optimizer, loss=loss)
  ckpt_dir = "./{}_ckpts".format(script)
  ckpt_prefix = os.path.join(ckpt_dir, "ckpt")
  ckpt_callback = tf.keras.callbacks.ModelCheckpoint(
    filepath=ckpt_prefix, save_weights_only=True
  )
  EPOCHS = 20
  steps_per_epoch = examples_per_epoch // BATCH_SIZE
  history = model.fit(dataset.repeat(),
                      epochs=EPOCHS,
                      steps_per_epoch=steps_per_epoch,
                      callbacks=[ckpt_callback]
                      )