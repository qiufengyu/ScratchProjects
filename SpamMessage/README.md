## 基于 Bayes 的短信过滤系统

### 开发环境

* Windows 10 64 位系统 / macOS
* Python 3.6.x (64 位)，需要安装下列依赖库，如果安装中提示缺少什么，根据直接安装：`pip install xxx`
  * jieba
  * Django
  * mysqlclient

* PyCharm Professional 版本不要太旧就行，可直接创建、导入 Django 项目，免去许多配置
* MySQL 5.x 或者 8.x 的新版本，设置好中文编码 utf8mb4，

为保证正确运行，请尽量使用相同的开发环境。 涉及到 python 的路径设置，在 PyCharm 中调试时，请选择正确的 python 解释器路径。

*注意：涉及到中文文本的处理，使用 UTF-8 编码！*

 ### 项目文件

* app 文件夹
  * 应用 Web UI 逻辑处理
  * urls.py 设定了路径与后台逻辑的对应关系，views.py 中实现了每一个后台对应的业务功能
* bayes 文件夹
  * 实现了一个贝叶斯分类模型，对垃圾短信进行分类
* bayes.pickle & bayes.txt
  * 贝叶斯分类模型保存的文件，由分类算法生成，可直接加载
* data.txt & data_clean.txt
  * 垃圾短信的数据集，后者是经过中文分词后的短信
* manage.py
  * Django Web 的管理，开始的入口
* messages_user.sql
  * 数据库建表、初始化一些数据
* SpamMessage
  * 网页界面的一些配置，包括连接的数据库、前后端逻辑交互配置
* static 与 templates
  * 网页界面设计，包括一些样式、js、网页排版
* stop.txt
  * 中文常用词表，可以提高分类器效果

### 程序运行方式

可以通过 Pycharm （Professional 版本）导入打开，或者命令行

注意 1：具体的操作过程参考对应代码中最后部分的 main 函数注释，按照步骤选择性运行 

注意 2：由于 python 环境和项目设置的路径不同，需要根据实际情况修改。直接运行可能会报错，重新选择一下 Python interpreter就没有问题了。

1. 首先，配置好数据库，建立一个叫做 messages 的数据库，执行提供的 sql 脚本，创建好各种表；

2. 在Pycharm 中的设置主要是 Script path 和 Working directory，如果直接运行 bayesclass.py 的话， Working directory 一定是到项目的根目录，而不是 "根目录\bayes"

   确保 data 处在项目根目录下，没有 data_clean.txt 的话，根据 bayesclass.py 的注释执行 preprocess

   然后就可以进行后面的工作，训练 bayes，并进行测试。

3. Web UI 演示运行

   命令行中，在根目录下，使用 `python manage.py runserver 8000` 运行，初始化完成后，会提示可以访问 <http://127.0.0.1:8000> ，进行相关的操作

   推荐在 Pycharm 中调试与运行，应该导入项目后，保持默认配置即可直接开启 Django 网页服务