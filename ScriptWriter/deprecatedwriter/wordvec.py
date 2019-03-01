import os
import gensim
import numpy as np

class SentencesIter():
  def __init__(self, dirname):
    self.dirname = dirname

  def __iter__(self):
    for fname in os.listdir(self.dirname):
      for line in open(os.path.join(self.dirname, fname), encoding="utf-8"):
        if len(line.strip()) > 0:
          yield [segment for segment in line]

def vectorize_vocab(modelname):
  model = gensim.models.FastText.load(modelname)
  wv = model.wv
  idx2char = wv.index2word
  vocab_size = len(idx2char)
  char2idx = {idx2char[i]: i for i, c in enumerate(idx2char)}
  w2v_em = np.zeros((vocab_size + 1, 100))
  for i, word in enumerate(idx2char):
    w2v_em[i] = wv[word]
  w2v_em[vocab_size] = wv["\n"]
  return char2idx, idx2char, w2v_em

def text_as_int(text_file, char2idx):
  chars = []
  unk_idx = len(char2idx)
  with open(text_file, 'r', encoding="utf-8") as f:
    for line in f.readlines():
      linestrip = line.strip()
      if len(linestrip) > 1:
        for x in line:
          if x in char2idx:
            chars.append(char2idx[x])
          else:
            chars.append(unk_idx)
  return np.array(chars)

if __name__ == "__main__":
  sentences = SentencesIter("data")
  model = gensim.models.fasttext.FastText(sentences, size=100, min_count=5,
                                          max_vocab_size=10000, workers=4, iter=500)
  model.save("wordvec.model")
  model = gensim.models.FastText.load("wordvec.model")
  x = model.wv["我"]
  y = model.wv["你"]
  wv = model.wv
  vocab = model.wv.vocab
  print("字的个数：")
  vocab_size = len(vocab)
  print(wv.distance("你", "是"))
  print(wv.most_similar("人", topn=10))
  w2v_em = np.zeros((vocab_size+1, 100))
  for i, word in enumerate(model.wv.index2word):
    w2v_em[i] = wv[word]
