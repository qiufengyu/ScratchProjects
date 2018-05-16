## 公路运输文本数据的分析与转换

#### 开发环境

* Windows 10 64 位系统 / macOSHigh Sierra 10.13.4
* Python 3.6.x (64 位)，至少需要安装下列依赖库，可以直接 `pip install xxx`，附上开发时用的版本
  * Django – 2.0.x，项目 Web 框架
  * PyMySQL – 0.8.0，数据库接口


* MySQL 5.7.x，数据库，安装完毕后需要配置好用户名和密码
* PyCharm 2018.1，Professional 版本，学生可以免费使用，或者也可以破解。可直接创建、导入 Django 项目，免去许多配置

为保证正确运行，请尽量使用相同的开发环境。 涉及到python 的路径设置，请在 Pycharm 中调试时，选择正确的运行路径。

 

#### 项目结构（基于  **Django** 开发的 Web 界面）

* `polls` 中，项目逻辑处理单元，管理整个 Web 的前后端交互、请求、渲染等；
* `highway` 文件夹，Django 项目的设置
* `static` 文件夹，网页端使用到的 `css` 样式和一些 `js` 代码，用于渲染网页
* `templates` 文件夹，`html` 网页代码，通过 Django 注入数据，作为前端 UI
* `manage.py` 是项目运行的入口，其他的一些 `python` 代码文件系前期开发的一些残留处理、测试代码，需求更改后已经不起作用，可做参考。
* `highway.sql` 对应了数据库创建、数据初始化

 

#### 主要代码文件说明

本项目是 Django 框架开发的，主要的业务逻辑都在 poll 文件夹中的代码，又以 `views.py` 为核心，`select_incidents.py` 提供了一些数据库上的增、查的接口。

* `views.py`
  * `index` 函数，返回一些主页上显示的信息，如事件总数，事件严重程度的统计情况，最新事件
  * `AnalysisView` 及 `analysis` 函数，由于这部分指导老师不要求，所以只留了结构化的信息，没有实现任何的代码逻辑
  * `about`, `help` 函数，返回的是网站系统的相关信息和使用说明，需要自行完善，不是本项目的主要任务，修改对应的 `templates/about.html` 和 `templates/help/index.html` 文件即可
  * `detail` 函数，根据用户提供的事件 ID，返回事件的完整信息，并且在事件的 Detail 选项卡中显示；与之类似的 `detail_with_id` 则是可以直接从请求路径中获取事件 ID，两者内部实现一致，可以处理两种不同的用户操作
  * `add` 函数，根据用户在 Web 中的输入和选择，向数据库中插入一条事件的信息
  * `filtering` 函数，根据用户在 Web 中的输入、选择筛选条件，查询符合的事件
* `select_incidents.py`
  * 首先，`tuple_to_dict` 函数将 MySQL 查询到的结果转换为便于 Python 程序解读和封装、传递的 `dict` （字典）类型
  * 各种 statement 预先定义好了一些数据库的语句，在使用时只需要对其中一些域进行填入即可
  * `IncidentSelector` 类则是封装了一些数据库的操作方法，可以从函数名上直接推出函数的作用，就不解释了

 

#### 程序运行方式

1. 首先，确保已经正确安装了数据库、根据所给的 SQL 文件初始化好，配置好Python 环境和相关的依赖库。

2. 可以通过 PyCharm 导入打开，支持命令行运行和 Web UI 运行。

   *注意：由于 python 环境和项目设置的路径不同，需要根据实际情况修改。直接运行可能会出错。有的时候需要重新选择一下 Python interpreter 或者是设置好 Working directory 就没有问题了。*


3. Web UI 演示运行，命令行中，在第一层文件夹下，使用 “python manage.py runserver 8000” 运行，当有如下的提示后，可以访问 <http://127.0.0.1:8000> ，看到项目的 UI 界面，下面的操作请根据演示视频直接在网页上操作即可。 

   ```bash
   System check identified no issues (0 silenced).
   May 16, 2018 - 08:57:18
   Django version 2.0.2, using settings 'Highway.settings'
   Starting development server at http://127.0.0.1:8000/
   Quit the server with CONTROL-C.

   ```

   推荐在 PyCharm 中调试与运行，但是需要 Professional 版本，保持默认配置即可