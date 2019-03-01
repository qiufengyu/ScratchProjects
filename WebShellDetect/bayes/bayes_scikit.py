import os

from sklearn import metrics
from sklearn.externals import joblib
from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import GaussianNB

from WebShellDetect.settings import BASE_DIR

# 这种使用第三方库的方式，由于封装得太好，很难复用
# 当出现新的测试样本，要从原始的 php 脚本构建特征，
# 调用特征构造方法需要重新读取原始数据集，所以不推荐
# 重点：该文件仅作参考
from shelldata import get_opcode


def load_data():
  with open(os.path.join(BASE_DIR, "php.white.txt"), "r", encoding="utf-8") as f:
    white_list = f.readlines()
  with open(os.path.join(BASE_DIR, "php.black.txt"), "r", encoding="utf-8") as f:
    black_list = f.readlines()
  X = white_list + black_list
  y_white = [0] * len(white_list)
  y_black = [1] * len(black_list)
  y = y_white + y_black
  return X, y

def bayes_sci(tfidf=False):
  corpus, y = load_data()
  if tfidf:
    vectorizer = CountVectorizer()
    X = vectorizer.fit_transform(corpus).toarray()
  else:
    vectorizer = TfidfVectorizer(corpus)
    X = vectorizer.fit_transform(corpus).toarray()
  X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.1)
  gnb = GaussianNB()
  gnb.fit(X_train, y_train)
  joblib.dump(gnb, os.path.join(BASE_DIR, "bayes/gnb.pkl"))
  y_pred = gnb.predict(X_test)
  print("Accuracy :{}".format(metrics.accuracy_score(y_test, y_pred)))
  print(metrics.confusion_matrix(y_test, y_pred))
  return vectorizer

def bayes_sci_test(model, vectorizer, php):
  php_vector = vectorizer.transform(php).toarray()
  pred = model.predict(php_vector)
  return pred

if __name__ == '__main__':
  vectorizer = bayes_sci()
  model = joblib.load(os.path.join(BASE_DIR, "bayes/gnb.pkl"))
  test_corpus = []
  white_test_file = os.path.expanduser("~/Desktop/white.php")
  white_op_code = ' '.join(get_opcode(white_test_file))
  black_test_file = os.path.expanduser("~/Desktop/black.php")
  black_op_code = ' '.join(get_opcode(black_test_file))
  test_corpus.append(white_op_code)
  test_corpus.append(black_op_code)
  print(bayes_sci_test(model, vectorizer, test_corpus))

