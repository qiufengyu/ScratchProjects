import gensim
import os
import jieba

import pathlib


class Word2Vec(object):
  def __init__(self, data_path):
    self.data_path = data_path

  def extract_corpus(self):
    file_list = []
    for root, subFolders, files in os.walk(self.data_path):
      for file in files:
        file_path = pathlib.Path(root) / file
        file_list.append(str(file_path).replace('\\', '/'))
    with open('corpus.txt', 'w', encoding='utf-8') as f_write:
      with open('rmrb.txt', 'r', encoding='utf-8') as f_rmrb:
        while True:
          line = f_rmrb.readline()
          if not line:
            break
          if len(line) > 1:
            tokens = line.strip().split('  ')
            words_tags = tokens[1:]
            words = [wt.split('/')[0].replace('[', '').replace(']','') for wt in words_tags]
            f_write.write(' '.join(words))
            f_write.write('\n')


