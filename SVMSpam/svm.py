import numpy as np
from sklearn.model_selection import train_test_split

import pickle

from data_process import text_process


"""
SVM 相关的算法，可以看这篇博客，https://blog.csdn.net/v_july_v/article/details/7624837
可以作为论文的参考，此外，李航的《统计机器学习》也不错
"""

def load_data(vectorizer):
  with open("./data/ham.txt", "r", encoding="utf-8") as f:
    ham_list = f.readlines()
  with open("./data/spam.txt", "r", encoding="utf-8") as f:
    spam_list = f.readlines()
  X = ham_list + spam_list
  X_trans = vectorizer.transform(X).toarray()
  y_ham = [-1] * len(ham_list)
  y_spam = [1] * len(spam_list)
  y = y_ham + y_spam
  return X_trans, y

def selectJrand(i, m):
  # 在 0-m 中随机选择一个不是 i 的整数
  j = i
  while (j == i):
    j = int(np.random.uniform(0, m))
  return j


def clipAlpha(aj, H, L):
  # 保证 a 在 L 和 H 范围内（L <= a <= H）
  if aj > H:
    aj = H
  if L > aj:
    aj = L
  return aj

def kernelTrans(X, A, kTup):
  # 核函数，输入参数：X 支持向量的特征树；A 某一行特征数据；kTup：('linear',k1) 核函数的类型和参数
  m, n = np.shape(X)
  K = np.zeros((m, 1))
  if kTup[0] == "linear":  # 线性函数
    K = X * A.T
  elif kTup[0] == "rbf":  # 径向基函数(radial bias function)
    for j in range(m):
      deltaRow = X[j, :] - A
      K[j] = deltaRow * deltaRow.T
    K = np.exp(K / (-1 * kTup[1] ** 2))  # 返回生成的结果
  else:
    raise NotImplementedError
  return K


# 定义类，方便存储数据
class optStruct:
  def __init__(self, dataMatIn, classLabels, C, toler, kTup):  # 存储各类参数
    self.X = dataMatIn  # 数据特征
    self.Y = classLabels  # 数据类别
    self.C = C  # 软间隔参数C，参数越大，非线性拟合能力越强
    self.tol = toler  # 停止阀值
    self.m = np.shape(dataMatIn)[0]  # 数据行数
    self.alphas = np.mat(np.zeros((self.m, 1)))
    self.b = 0  # 初始设为0
    self.eCache = np.mat(np.zeros((self.m, 2)))  # 缓存
    self.K = np.mat(np.zeros((self.m, self.m)))  # 核函数的计算结果
    print("计算核函数...")
    for i in range(self.m):
      self.K[:, i] = kernelTrans(self.X, self.X[i, :], kTup)
      if i % 200 == 0:
        print("{} 完毕".format(i))

def calcEk(oS, k):  # 计算Ek（参考《统计学习方法》p127公式7.105）
  fXk = float(np.multiply(oS.alphas, oS.Y).T * oS.K[:, k] + oS.b)
  Ek = fXk - float(oS.Y[k])
  return Ek

# 随机选取aj，并返回其E值
def selectJ(i, oS, Ei):
  maxK = -1
  maxDeltaE = 0
  Ej = 0
  oS.eCache[i] = [1, Ei]
  validEcacheList = np.nonzero(oS.eCache[:, 0].A)[0]  # 返回矩阵中的非零位置的行数
  if (len(validEcacheList)) > 1:
    for k in validEcacheList:
      if k == i:
        continue
      Ek = calcEk(oS, k)
      deltaE = abs(Ei - Ek)
      if (deltaE > maxDeltaE):  # 返回步长最大的aj
        maxK = k
        maxDeltaE = deltaE
        Ej = Ek
    return maxK, Ej
  else:
    j = selectJrand(i, oS.m)
    Ej = calcEk(oS, j)
  return j, Ej

def updateEk(oS, k):  # 更新os数据
  Ek = calcEk(oS, k)
  oS.eCache[k] = [1, Ek]

# 首先检验 ai 是否满足 KKT 条件，如果不满足，随机选择 aj 进行优化，更新 ai,aj,b 值
def innerL(i, oS):  # 输入参数i和所有参数数据
  Ei = calcEk(oS, i)  # 计算E值
  if ((oS.Y[i] * Ei < -oS.tol) and (oS.alphas[i] < oS.C)) or (
      (oS.Y[i] * Ei > oS.tol) and (oS.alphas[i] > 0)):  # 检验这行数据是否符合KKT条件 参考《统计学习方法》p128公式7.111-113
    j, Ej = selectJ(i, oS, Ei)  # 随机选取aj，并返回其E值
    alphaIold = oS.alphas[i].copy()
    alphaJold = oS.alphas[j].copy()
    if (oS.Y[i] != oS.Y[j]):  # 以下代码的公式参考《统计学习方法》p126
      L = max(0, oS.alphas[j] - oS.alphas[i])
      H = min(oS.C, oS.C + oS.alphas[j] - oS.alphas[i])
    else:
      L = max(0, oS.alphas[j] + oS.alphas[i] - oS.C)
      H = min(oS.C, oS.alphas[j] + oS.alphas[i])
    if L == H:
      # print("L==H")
      return 0
    eta = 2.0 * oS.K[i, j] - oS.K[i, i] - oS.K[j, j]  # 参考《统计学习方法》p127公式7.107
    if eta >= 0:
      # print("eta>=0")
      return 0
    oS.alphas[j] -= oS.Y[j] * (Ei - Ej) / eta  # 参考《统计学习方法》p127公式7.106
    oS.alphas[j] = clipAlpha(oS.alphas[j], H, L)  # 参考《统计学习方法》p127公式7.108
    updateEk(oS, j)
    if (abs(oS.alphas[j] - alphaJold) < oS.tol):  # alpha变化大小阀值（自己设定）
      # print("j not moving enough")
      return 0
    oS.alphas[i] += oS.Y[j] * oS.Y[i] * (alphaJold - oS.alphas[j])  # 参考《统计学习方法》p127公式7.109
    updateEk(oS, i)  # 更新数据
    # 以下求解b的过程，参考《统计学习方法》p129公式7.114-7.116
    b1 = oS.b - Ei - oS.Y[i] * (oS.alphas[i] - alphaIold) * oS.K[i, i] - oS.Y[j] * (
          oS.alphas[j] - alphaJold) * oS.K[i, j]
    b2 = oS.b - Ej - oS.Y[i] * (oS.alphas[i] - alphaIold) * oS.K[i, j] - oS.Y[j] * (
          oS.alphas[j] - alphaJold) * oS.K[j, j]
    if (0 < oS.alphas[i] < oS.C):
      oS.b = b1
    elif (0 < oS.alphas[j] < oS.C):
      oS.b = b2
    else:
      oS.b = (b1 + b2) / 2.0
    return 1
  else:
    return 0


# SMO 函数，用于快速求解出 alpha
def smo(X, Y, C, toler, maxIter, kTup=("linear", 0)):
  # 输入参数：数据特征，数据类别，参数C，阀值toler，最大迭代次数，核函数（默认线性核）
  oS = optStruct(np.mat(X), np.mat(Y).transpose(), C, toler, kTup)
  iter = 0
  entireSet = True
  alphaPairsChanged = 0
  while (iter < maxIter) and ((alphaPairsChanged > 0) or (entireSet)):
    alphaPairsChanged = 0
    iter += 1
    if entireSet:
      print("第 {} 次迭代，全部数据。".format(iter))
      for i in range(oS.m):  # 遍历所有数据
        alphaPairsChanged += innerL(i, oS)
      print("共有 {} 个 alpha 被更新。".format(alphaPairsChanged))
      # 显示第多少次迭代，这次改变了多少次 alpha
    else:
      nonBoundIs = np.nonzero((oS.alphas.A > 0) * (oS.alphas.A < C))[0]
      print("第 {} 次迭代，非边界数据。".format(iter))
      for i in nonBoundIs:  # 遍历非边界的数据
        alphaPairsChanged += innerL(i, oS)
      print("共有 {} 个 alpha 被更新。".format(alphaPairsChanged))
    if entireSet:
      entireSet = False
    elif (alphaPairsChanged == 0):
      entireSet = True
  with open("svm.pkl", "wb") as f_svm:
    pickle.dump(oS, f_svm)
  return oS.b, oS.alphas

def testRbf(trainX, trainY, testX, testY):
  b, alphas = smo(trainX, trainY, 200, 0.0001, 10000, ("rbf", 1.3))  # 通过SMO算法得到b和alpha
  XMat = np.mat(trainX)
  YMat = np.mat(trainY).transpose()
  SVInd = np.nonzero(alphas)[0]  # 选取不为0  数据的行数（也就是支持向量）
  SVs = trainX[SVInd]  # 支持向量的特征数据
  SVY = YMat[SVInd]  # 支持向量的类别（1 或 -1）
  print("支持向量个数：{}".format(np.shape(SVs)[0]))  # 打印出共有多少的支持向量
  m, n = np.shape(trainX)  # 训练数据的行列数
  errorCount = 0
  for i in range(m):
    kernelEval = kernelTrans(SVs, XMat[i, :], ("rbf", 1.3))  # 将支持向量转化为核函数
    predict = kernelEval.T * np.multiply(SVY, alphas[SVInd]) + b
    # 这一行的预测结果（代码来源于《统计学习方法》p133里面最后用于预测的公式）注意最后确定的分离平面只有那些支持向量决定。
    if np.sign(predict) != np.sign(YMat[i]):  # sign函数 -1 if x < 0, 0 if x==0, 1 if x > 0
      errorCount += 1
  print("训练集错误率：{}".format((float(errorCount) / m)))  # 打印出错误率
  errorCount_test = 0
  XMat_test = np.mat(testX)
  YMat_test = np.mat(testY).transpose()
  m, n = np.shape(XMat_test)
  for i in range(m):  # 在测试数据上检验错误率
    kernelEval = kernelTrans(SVs, XMat_test[i, :], ('rbf', 1.3))
    predict = kernelEval.T * np.multiply(SVY, alphas[SVInd]) + b
    if np.sign(predict) != np.sign(YMat_test[i]):
      errorCount_test += 1
  errorRate_test = float(errorCount_test) / m
  print("测试集错误率：{}".format(errorRate_test))
  return errorRate_test

def train_only():
  with open("vectorizer.pkl", "rb") as f:
    vectorizer = pickle.load(f)
  X, y = load_data(vectorizer)
  # 数据切分为训练集和测试集
  X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
  return testRbf(X_train, y_train, X_test, y_test)

def test_only(raw_text_list):
  # 对原始文本进行预处理
  text_list = text_process(raw_text_list)
  text = " ".join(text_list)
  try:
    with open("vectorizer.pkl", "rb") as f:
      vectorizer = pickle.load(f)
    with open("svm.pkl", "rb") as f_svm:
      svmStruct = pickle.load(f_svm)
  except IOError:
    return 0
  features = vectorizer.transform([text]).toarray()
  b, alphas = svmStruct.b, svmStruct.alphas
  XMat = svmStruct.X
  YMat = svmStruct.Y
  SVInd = np.nonzero(alphas)[0]  # 选取不为0  数据的行数（也就是支持向量）
  SVs = XMat[SVInd]  # 支持向量的特征数据
  SVY = YMat[SVInd]  # 支持向量的类别（1 或 -1）
  kernelEval = kernelTrans(SVs, features[0, :], ('rbf', 1.3))
  predict = kernelEval.T * np.multiply(SVY, alphas[SVInd]) + b
  if predict > 0:
    return 1
  else:
    return -1


if __name__ == '__main__':
  with open("vectorizer.pkl", "rb") as f:
    vectorizer = pickle.load(f)
  X, y = load_data(vectorizer)
  # 数据切分为训练集和测试集
  X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
  testRbf(X_train, y_train, X_test, y_test)
