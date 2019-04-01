## 基于 SVM 算法的垃圾邮件过滤系统设计

### 开发环境

* Windows 10 64 位系统 / macOS Mojava

* Python 3.6 (64 位)，需要安装下列依赖库

  可以直接 `pip install xxx`，默认使用最新的版本，差别不大

  *  jieba，中文分词
  *  numpy，数据、矩阵操作
  *  scipy  和 scikit-learn，提供了 SVM 的实现，主要用来对比和数据切分、特征抽取
  *  PyQt5，简单的图形化界面

* PyCharm 2018.3 IDE

为保证正确运行，请尽量使用相同的开发环境。涉及到 python 的路径设置，在 Pycharm 中调试时，选择正确的python 运行路径。

**注意：涉及到中文文本的处理，请使用 UTF-8 编码！**

### 项目文件

* data 文件夹
  * 相关的文本数据
  * 经过清洗、转储过的 trec06c 中的数据，默认是包含垃圾/非垃圾邮件各 600 条
  * stopwords.txt，是网上找的中文停用词表
* data_process.py
  * 对原始的 trec06c 数据进行清洗，中文提取、分词，并得到对应的两类样本
* form.ui & form.py
  * PyQt 的界面设计，ui 文件是通过 Qt Designer 绘制的，py 文件是生成的 python 代码
* scisvm.py
  * 调用 scikit learn 中的 SVM 方法，生成一个基本的 SVM 分类器
  * 这里还有一个特征提取工具，在之后的 SVM 中也会用到
* scisvm.pkl
  * scikit SVM 模型保存为二进制文件，之后可以被加载使用，不必重复训练
* svm.py
  * 使用 SMO 算法学习 SVM 模型，依赖于 tf-idf 的特征抽取模块
* svm.pkl
  - SVM 模型保存的二进制文件，之后可以被加载使用，不必重复训练
  - 也是用户界面调用的 svm 模型
* ui.py
  * 界面运行的入口，实现了一些事件响应和交互
* vectorizer.words & vectorizer.pkl
  * 基于 tf-idf 的特征提取模型
  * 前者可以阅读，可以查看关注到的用于构造特征的词
  * 后者是一个二进制文件，可以直接加载并使用，不必重新学习

### 程序运行方式

可以通过 Pycharm 导入打开，或者命令行运行

*注意1：具体操作可以参考注释查看运行过程，main 函数都在文件的最后*

*注意2：由于 python 环境和项目设置的路径不同，需要根据实际情况修改。直接运行可能会出错。有的时候需要重新选择一下 Python interpreter 就没有问题了。*

1. 首先，运行 data_process.py

   根据注释的要求，下载好 trec06c 数据放在桌面路径，再运行

   当 data 文件夹下的 ham.txt 和 spam.txt 都生成完毕，结束

2. 接着，运行 scisvm.py

   只需要第一部分生成 vectorizer 即可，后面的单例测试可以不运行

   当在工程中，生成了 vectorizer.words 和 .pkl，这一阶段就完成了

3. 最后，执行 ui.py，就有可视化界面了

   确保工程目录中有 vectorizer.pkl，没有的话执行第二步

   确保工程目录中有 svm.pkl，否则就需要点击“重新训练模型”，生成对应的 SVM 模型

   然后根据界面要求操作即可

   