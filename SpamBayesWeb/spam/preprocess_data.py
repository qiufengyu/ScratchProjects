"""
数据预处理，从源数据中生成对应的文本 + 标签的格式
生成文件列表
"""
import os

import jieba


def get_all_files(root_dir, path=''):
  file_list = []
  for root, subFolders, files in os.walk(root_dir):
    for file in files:
      f = os.path.join(root, file)
      file_list.append(f.replace('\\', '/'))
  # 把所有的电子邮件的文件名提取出来
  # with open('fileList.txt', 'w') as f:
    # for file in file_list:
      # f.write(file)
      # f.write('\n')
  return file_list

def get_index(index_file, path=''):
  spam_set = set()
  with open(index_file, 'r') as f:
    while True:
      line = f.readline()
      if not line:
        break
      parts = line.strip().split(' ')
      if parts[0] == 'spam':
        spam_set.add(parts[1].replace('../', path))
  return spam_set

def is_spam(file_name, spam_set):
  return file_name in spam_set

def vocabulary(file_list, spam_set):
  vocab = set()
  spam = 0
  ham = 0
  invalid = 0
  for file in file_list:
    # print(file)
    try:
      with open(file, 'r', encoding='utf-8') as f:
        while True:
          line = f.readline()
          if not line:
            break
          else:
            seg = jieba.cut(line, cut_all=False)
            for x in seg:
              vocab.add(x)
        if is_spam(file, spam_set):
          spam += 1
        else:
          ham += 1
    except UnicodeDecodeError:
      invalid += 1
  # 把字典写到文件中，便于后续处理
  with open('vocabuary.txt', 'w', encoding='utf-8') as vocab_f:
    for x in vocab:
      vocab_f.write(x+'\n')
  return vocab

# 进行交叉验证，fold 是总共的划分组数，通常取 5、10
# 默认生成五组 训练集、测试集
def split_data(file_list, total_fold=5, fold=1):
  assert(total_fold >= fold, '总的份数不能小于所取的分片')
  all_data_length = len(file_list)
  # python3, 默认就是浮点数除法，所以转换成 int
  one_group = int(all_data_length / total_fold)
  start = (fold-1) * one_group
  end = min(start + one_group, all_data_length)
  test_file_list = file_list[start:end]
  train_file_list = file_list[0:start] + file_list[end:]
  return train_file_list, test_file_list

if __name__ == "__main__":
  file_list = get_all_files('data')
  print("合计有效文件：", len(file_list))
  spam_set = get_index('index')
  train_file_list, test_file_list = split_data(file_list, total_fold=5, fold=1)
  print("训练集文件数：", len(train_file_list))
  print("测试集文件数：", len(test_file_list))
  vocab = vocabulary(train_file_list, spam_set)
