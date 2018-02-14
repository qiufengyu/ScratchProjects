# 基于python爬虫技术抓取微博短文的情感分析

### 开发环境

* macOS High Sierra 10.13.2

* Python 3.6.4 (64 位)，需要安装下列依赖库

  可以直接 `pip install xxx`，附上开发时用的版本

  * Scrapy – 1.5.0 爬虫框架（Windows 系统的安装比较麻烦，需要自己先安装一个 Twisted 包，再安装 pywin32。嫌麻烦可以用 anaconda安装python3，比较简便。参考：<https://segmentfault.com/a/1190000010377113> ）
  * selenium – 3.9.0 模拟浏览器的自动化工具，同时需要配置浏览器的驱动，支持 Chrome、Firefox 等，推荐Chrome，需要下载 chromedriver，Windows版本驱动放在根目录下即可， macOS 则指明安装路径。
  * beautifulsoup4 – 4.6.0 方便解析 html，提取内容
  * pymysql – 0.8.0 连接 MySQL 数据库
  * Django - 2.0.2 UI 展示框架，前后端交互
  * Snownlp – 0.12.3 情感分析工具包

* MySQL Server 5.7.20

* PyCharm 2017.3.2，可以直接导入两个项目

  为保证正确运行，请尽量使用相同的开发环境。 涉及到 python 的路径设置，请在 Pycharm 中调试时，选择正确的运行路径。

注意：涉及到中文文本的处理，请使用 UTF-8 编码！

 

### 子项目，Scrapy 爬虫 ( weibo 文件夹)：

该项目通过 `scrapy startproject weibo` 创建，为了方便整理，一并放在这里

* `chromedriver.exe`，Windows 用到的浏览器驱动
* `main.py`，爬虫程序运行入口
* `scrapy.cfg`，Scrapy 项目的相关配置文件
* `utils` 文件夹，实现一些小的辅助函数和小的功能
* `weibo` 文件夹，爬虫实现的主要逻辑
  * `items.py`，爬虫获取到的数据实体定义
  * `middlewares.py`，设置了一个自动更换 User-agent 头的中间件
  * `pipelines.py`，实现了一个将数据写入数据库的管道
  * `settings.py`，爬虫的其他设置，最重要的是 17 – 26 行的设置，需要根据自己的实际情况设置，一定要填入自己的合法的微博账号密码！数据库的用户名、密码等也需要正确设置。
  * `spiders` 文件夹下的 `weibo_spider.py` 是爬虫的实现。

 

### 项目二，情感分析与展示

这是一个 Django 项目，相关的文件如下：

* `db.sqlite3`，Django 项目自带的一个小型数据库，该项目中暂时用不到
* `manage.py`，Django 项目生成的一个管理、运行的入口
* `neg.txt` / `pos.txt`，SnowNLP 进行情感分析的模型使用到的训练文件，可以自定义更新用户自己的模型
* `sentiment` 文件夹中，主要是 views.py，实现了前后端的交互、数据传输、渲染等功能
* `sentiment.marshal.3`，SnowNLP 情感分析的模型文件
* `static` 文件夹中是网页前端的一些 css 和 js
* `templates` 文件夹中是 html 页面
* `WeiboSentiment` 文件夹，主要是一些项目相关的设置，`urls.py` 中设置了一些路由



### 程序运行方式

可以通过 Pycharm 导入打开，支持命令行运行和 Web UI 运行。有的一些说明请仔细阅读注释相关。

*注意：由于 python 环境和项目设置的路径不同，需要根据实际情况修改。直接运行可能会出错。有的时候需要重新选择一下 Python interpreter 就没有问题了。*

1. 准备工作：安装好 python 环境和依赖包，同时安装并配置好 MySQL 环境。然后通过提供的 sql 文件创建数据表（`create_table.sql`）和数据 (其他两个 sql 文件)。

2. 运行爬虫，需要一个微博账号，将这个填入 `weibo/weibo/settings.py` 的第 24、25行。

   命令行模式，在 `weibo` 目录下，`python main.py` 即可，也可 Pycharm 直接调试

3. 情感分析展示，pycharm 的 Professional 版本支持直接调试，使用默认设置即可。命令行模式：在 pycharm 的 Terminal 中，输入 `python manage.py runserver 8000` 运行，当有如下的提示后，可以访问 <http://127.0.0.1:8000> ，看到项目的 UI 界面，下面的操作请直接在网页上操作即可。

   ```
   Starting development server at http://127.0.0.1:8000/
   Quit the server with CONTROL-C.
   ```

   ​