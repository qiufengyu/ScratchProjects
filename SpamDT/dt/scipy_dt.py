import graphviz
from sklearn.metrics import accuracy_score
from sklearn.tree import DecisionTreeClassifier, export_graphviz
from sklearn.model_selection import train_test_split

"""
这是调用机器学习库关于决策树的工具，用来调试超参的
比如 使用多少维的特征、决策树深度、
"""
def scipy_dt(data, feature_names):
  X = data[:, 0:-1]
  y = data[:, -1].astype(int)
  X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=100)
  clf_gini = DecisionTreeClassifier(criterion="entropy", random_state=100,
                                    max_depth=10, min_samples_leaf=5)
  clf_gini.fit(X_train, y_train)
  y_pred = clf_gini.predict(X_test)
  acc = accuracy_score(y_test, y_pred) * 100.0
  print(f"Accuray: {acc:.4f}%")
  # 解决引号不能正确解析的问题：
  revised_feature_names = []
  for feat in feature_names:
    revised_feature_names.append('mark' if feat == '"' else feat)

  dot_data = export_graphviz(clf_gini, out_file=None, feature_names=revised_feature_names)
  graph = graphviz.Source(dot_data)
  # print(graph)
  graph.render('scipy_tree', view=True, )

def get_feature_names(word_file) -> list:
  features = []
  with open(word_file, 'r', encoding='utf-8') as f:
    while True:
      line = f.readline()
      if not line:
        break
      line = line.strip()
      features.append(line)
  return features


