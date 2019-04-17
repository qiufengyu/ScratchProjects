## 基于 python 的网络爬虫

### 开发环境

* Windows 10 64 位系统 / macOS Mojave 10.14.4
* Python 3.6.x (64 位)，至少需要安装下列依赖库，可以直接 `pip install xxx`，默认版本会用最新的，差别不大
  * django， 2.x 版本，项目 Web 框架
  * PyMySQL，数据库接口
  * beautifulsoup4，解析网页代码，提取内容
  * lxml，html 内容解析器
  * requests，发送网页请求，获取内容
  * snownlp，中文自然语言处理库，可生成关键词、摘要等
* MySQL 5.7.x 数据库，或者更新的版本，安装完毕后需要配置好，默认代码中的用户名和密码都是 root，并且设置好中文编码！
* PyCharm 2018.3 IDE，最好是 Professional 版本，学生可以免费使用，或者也可以破解。可直接导入 Django 项目，还自带 MySQL 数据库的可视化界面

**注意：涉及到中文文本的处理，请务必使用 UTF-8 编码！**

### 项目文件说明

* `app` 文件夹，实现了网页界面的交互逻辑，其中
  * `urls.py` 定义了网页请求路径与后台的处理逻辑的对应关系
  * `views.py` 实现了各种请求的处理逻辑，并且向用户返回结果数据
  * 其他的文件为创建项目时生成的文件，在本项目中无关
* `static` 与 `templates` 文件夹中的 css、js 和 html 文件，实现了界面展示的前端
* `WebSpider` 中是关于项目界面的设置，主要是 `settings.py`，定义了一个日志工具，进行了相关的配置
* 两个 sql 文件，一个是创建表的 query，另一个是插入数据的
* `db_tools.py` 中实现了数据库上的相关操作，创建连接，插入数据、查询数据等逻辑，在其他模块中被调用
* `.log` 文件是日志记录，按照运行的日期命名
* `manager.py` 是 Django 网页界面运行的入口，创建项目时生成，直接会被调用
* `spider.py` 实现了爬虫，并且通过多线程进行了调度，进行资源的分配。爬虫得到的数据，存入数据库，并且在缺少关键词、摘要的情况下，会自动从内容中抽取对应内容
* `user_agents.py` 定义了浏览器的 User-Agent 字段，是应对反爬虫的常用手段

#### 程序运行方式

可以通过 Pycharm 导入打开，在 IDE 中调试比较方便。

*注意 1：由于 python 环境和项目设置的路径不同，需要根据实际情况修改。直接运行可能会出错。有的时候需要重新选择一下 Python interpreter 就没有问题了。*

*注意 2：相关的运行说明和配置在注释中说明了，可以根据实际情况修改。*

* 安装并配置好 MySQL 环境、Python 3 及相应的包，执行 sql 文件创建表（导入数据可选）

* 运行爬虫程序，可以手动修改爬取新闻的页码范围，即在 `spider.py` 的 `main` 函数中，`get_urls` 中修改 `start` 和 `end`，之后在 IDE 中调试运行，或者命令行模式，输入`python spider.py` 

* Web UI 演示运行，直接选择 WebSpider 项目在 IDE 中运行，或者在命令行中，第一层文件夹下，使用 `python manage.py runserver 8000` 运行，当有如下的提示后，可以访问 <http://127.0.0.1:8000> ，看到项目的 UI 界面，之后就可以在网页上操作即可。

  ```shell
  ...
  Starting development server athttp://127.0.0.1:8000/
  Quit the server with CONTROL-C.
  ```

程序运行的记录、异常情况都在日志文件中。