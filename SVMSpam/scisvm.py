import os

from sklearn import metrics
from sklearn.externals import joblib
from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC, NuSVC, LinearSVC

import pickle

from data_process import text_process


def load_data():
  with open("./data/ham.txt", "r", encoding="utf-8") as f:
    ham_list = f.readlines()
  with open("./data/spam.txt", "r", encoding="utf-8") as f:
    spam_list = f.readlines()
  X = ham_list + spam_list
  y_ham = [-1] * len(ham_list)
  y_spam = [1] * len(spam_list)
  y = y_ham + y_spam
  return X, y

def svm_sci():
  """
  通过 scikit-learn 库的 svm 工具进行分析
  :return:
  """
  corpus, y = load_data()
  # 只选择最常见的标志性 4000 词（max_features）
  # 提取 tf-idf 特征
  vectorizer = TfidfVectorizer(corpus, max_features=4000)
  X = vectorizer.fit_transform(corpus).toarray()
  # 数据切分为训练集和测试集
  X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
  svm = SVC(gamma="scale")
  svm.fit(X_test, y_test)
  joblib.dump(svm, os.path.join(".", "scisvm.pkl"))
  y_pred = svm.predict(X_test)
  print("Accuracy :{}".format(metrics.accuracy_score(y_test, y_pred)))
  print(metrics.confusion_matrix(y_test, y_pred))
  # 把这个基于 tf-idf 的 vectorizer （特征化工具）保存下来，方便后续使用
  # 先是二进制文件，提高存储效率
  pickle.dump(vectorizer, open("vectorizer.pkl", "wb"))
  # 这个文件可以看到，在邮件分类中起到关键作用的词
  with open("vectorizer.words", "w", encoding="utf-8") as f_vec:
    for w in vectorizer.vocabulary_:
      f_vec.write(w+"\n")
  return vectorizer

def svm_test_one(model, vectorizer, mail):
  mail_vector = vectorizer.transform([mail]).toarray()
  pred = model.predict(mail_vector)
  return pred


if __name__ == '__main__':
  # 这个文件也需要运行一次，生成 vectorizer.pkl
  # 帮助生成样本的特征
  vectorizer = svm_sci()
  # 其他的内容是调用 Scikit-learn 的 svm 实现，但是效果不好
  # 所以只借助了它用来特征处理、数据分割的部分
  # 下面是进行单个测试的代码
  with open("vectorizer.pkl", "rb") as f:
    vectorizer = pickle.load(f)
  model = joblib.load("scisvm.pkl")
  raw_mail = """
  Received: from tom.com ([61.141.189.190])
	by spam-gw.ccert.edu.cn (MIMEDefang) with ESMTP id j7FK1sA2019428
	for <xue@ccert.edu.cn>; Thu, 18 Aug 2005 21:26:40 +0800 (CST)
  Message-ID: <200508160401.j7FK1sA2019428@spam-gw.ccert.edu.cn>
  From: =?GB2312?B?wdbV8bn6?= <bi@tom.com>
  Subject: =?gb2312?B?0rXO8aOh0bDV0rrP1/e777Dpo6E=?=
  To: xue@ccert.edu.cn
  Content-Type: text/plain;charset="GB2312"
  Date: Thu, 18 Aug 2005 21:40:17 +0800
  X-Priority: 3
  X-Mailer: Microsoft Outlook Express 6.00.2800.1106
  
  贵公司负责人(经理/财务）您好： 
      我是深圳市华隆源实业有限公司的（广州、东莞等市有分公司）。
  我司实力雄厚，有着良好的社会关系。因进项较多现完成不了每月销
  售额度。每月有一部分增值税电脑发票、海关缴款书等（6%左右）和
  普通商品销售税发票电脑运输发票，广告发票，服务业发票等 (国税
  地税2%左右）优惠代开或合作，点数较低。还可以根据所做数量额度
  的大小来商讨优惠的点数,公司成立多年一直坚持以“诚信”为中心作
  为公司的核心思想、树立公司形象, 真正做到“彼此合作一次、必成
  永久朋友，本公司郑重承诺所用绝对是真票！
  　　
      如贵司在发票的真伪方面有任何疑虑或担心，可上网查证或我司
  直接与贵司去税务局抵扣核对。 
     
      此信息长期有效，如须进一步洽商: 
      详情请电：13926517268
      邮  箱：szhly1688@tom.com
      联系人：林振国
   
  
  顺祝商祺！ 

    """
  mail_list = text_process(raw_mail.split("\n"))
  mail = " ".join(mail_list)
  res = svm_test_one(model, vectorizer, mail)
  if res > 0:
    print("垃圾邮件！")
  else:
    print("正常邮件！")


