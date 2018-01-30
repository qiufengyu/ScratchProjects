import numpy as np

class BayesUCI(object):
  def __init__(self, features, labels):
    # 邮件的特征向量
    self.features = features
    # 是否为垃圾邮件的标签
    self.labels = labels
    # 对于垃圾和非垃圾邮件，
    # 假设每一维都是服从参数为 mu 和 sigma 的高斯分布
    # 从统计数据中估计
    self._count()

  def _count(self):
    spam_index = (self.labels == 1)
    self.spam_mus = np.mean(self.features[spam_index], axis=0)
    # 为了避免出现除以 0 的情况
    self.spam_stds = np.std(self.features[spam_index], axis=0) + 1e-12
    ham_index = np.logical_not(spam_index)
    self.ham_mus = np.mean(self.features[ham_index], axis=0)
    self.ham_stds = np.std(self.features[ham_index], axis=0) + 1e-12
    # np.savetxt('spam_stds.txt', self.spam_stds, fmt="%.6f")
    # np.savetxt('ham_stds.txt', self.ham_stds, fmt="%.6f")

   # 计算一封邮件（根据其特征向量）是否是垃圾邮件
  def calculate_spam_prob_log(self, item):
    exp_value = np.exp(-(item-self.spam_mus)**2 / (2 * self.spam_stds**2))
    gauss_prob = 1.0 / (np.sqrt(2 * np.pi) * self.spam_stds) * exp_value + 1e-12
    # 为避免许多太小的数相乘，转化为对数相加的方式
    gauss_prob_log = np.log(gauss_prob)
    return np.sum(gauss_prob_log, axis=1)

  def calculate_ham_prob_log(self, item):
    exp_value = np.exp(-(item-self.ham_mus)**2 / (2 * self.ham_stds**2))
    gauss_prob = 1.0 / (np.sqrt(2 * np.pi) * self.ham_stds) * exp_value + 1e-12
    # 为避免许多太小的数相乘，转化为对数相加的方式
    gauss_prob_log = np.log(gauss_prob)
    return np.sum(gauss_prob_log, axis=1)

  def test(self, test_features, test_labels):
    p_spam = self.calculate_spam_prob_log(test_features)
    p_ham = self.calculate_ham_prob_log(test_features)
    predict = (p_spam >= p_ham)
    accuracy = (predict == test_labels.astype(bool))
    accuracy_float = accuracy.astype(float)
    # 返回改组的准确率
    return np.sum(accuracy_float)/len(accuracy_float)*100.0

def split_data(data, total_fold=5, fold=1):
  assert (total_fold >= fold, '总的份数不能小于所取的分片')
  all_data_length = len(data)
  # python3, 默认就是浮点数除法，所以转换成 int
  one_group = int(all_data_length / total_fold)
  start = (fold - 1) * one_group
  end = min(start + one_group, all_data_length)
  index = np.ones(len(data)).astype(bool)
  index[start:end] = False
  test_data = data[np.logical_not(index)]
  train_data = data[index]
  return train_data, test_data

if __name__ == '__main__':
  data = np.loadtxt('UCI/spambase.data', delimiter=',')
  # print(data)
  # 对原始数据进行划分
  np.random.shuffle(data)
  # 设置分片的总数进行测试
  total_fold = 5
  print(f"使用 {total_fold} 折交叉验证")
  acc_list = []
  for i in range(1, total_fold+1):
    print(f"当前是第 {i} 部分")
    train_data, test_data = split_data(data, total_fold, i)
    train_features = train_data[:, 0:-1]
    train_labels = train_data[:, -1].astype(int)
    bayesUCI = BayesUCI(train_features, train_labels)
    acc_i = bayesUCI.test(test_data[:, 0:-1], test_data[:, -1].astype(int))
    print(f"平均准确率：{acc_i:.6f}%")
    acc_list.append(acc_i)
  print('=' * 80)
  avg_acc = sum(acc_list) / len(acc_list)
  print(f"总体准确率：{avg_acc:.6f}%")

