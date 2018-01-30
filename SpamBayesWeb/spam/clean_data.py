import os
import re
import shutil

"""
这个函数运行一次即可！！！
对原始数据进行清洗，只保留邮件的正文部分，保存在 data 目录下
一些 meta 信息、其他的特殊字符文本就忽略了
同时为了顺畅解决中文字符编码问题，统一用 utf-8 格式重新编码
"""
def clean_data(origin_dir, dst_dir=""):
  file_list = []
  for root, subFolders, files in os.walk(origin_dir):
    for file in files:
      file_path = os.path.join(root, file)
      file_list.append(file_path.replace('\\', '/'))
  for file in file_list:
    # print(file)
    with open(file, 'r', encoding='gb2312') as f_read:
      try:
        while True:
          line = f_read.readline()
          if not line:
            break
          if len(line) > 1:
            continue
          else:
            break
        clean_file_path_split = file.split('/')
        clean_file_path = clean_file_path_split[1] + '/' + clean_file_path_split[2]
        clean_file_path = os.path.join(dst_dir, clean_file_path)
        if not os.path.exists(clean_file_path):
          os.makedirs(clean_file_path)
        with open(os.path.join(clean_file_path, clean_file_path_split[3]), 'w', encoding='utf-8') as f_write:
          while True:
            line = f_read.readline()
            if not line:
              break
            clean_line = clean_str(line)
            if len(clean_line) > 1:
              f_write.write(clean_line)
              f_write.write('\n')
      except UnicodeDecodeError:
        continue

def clean_str(string):
  """
  Tokenization/string cleaning for all datasets except for SST.
  Original taken from https://github.com/yoonkim/CNN_sentence/blob/master/process_data.py
  """
  # string = re.sub(u"[^\u4e00-\u9fff]", " ", string) <- 去除非中文字符
  # string = re.sub(r"[^A-Za-z0-9(),!?\'\`]", " ", string)
  # string = re.sub(r"\'s", " \'s", string)
  # string = re.sub(r"\'ve", " \'ve", string)
  # string = re.sub(r"n\'t", " n\'t", string)
  # string = re.sub(r"\'re", " \'re", string)
  # string = re.sub(r"\'d", " \'d", string)
  # string = re.sub(r"\'ll", " \'ll", string)
  # string = re.sub(r",", " , ", string)
  # string = re.sub(r"!", " ! ", string)
  # string = re.sub(r"\(", " \( ", string)
  # string = re.sub(r"\)", " \) ", string)
  # string = re.sub(r"\?", " \? ", string)
  string = re.sub(r"\s{2,}", " ", string)
  # return string.strip().lower()
  return string.strip()

if __name__ == "__main__":
  # 运行一次即可，之后就不再使用
  shutil.rmtree('data/')
  clean_data('trec06c/data')
  # 在此之后可以把源数据删了



