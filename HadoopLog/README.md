## 基于大数据平台的网站统计的设计与实现

### 开发环境

* Win10 64 位 / macOS Mojave
* Java 1.8.0_211 64位，具体小版本差异关系不大，Java 11 基本可以兼容，其他非长期版本不推荐
* Intellij IDEA 2019.1，推荐 Ultimate 版本，学生可以免费使用
* Maven（Intellij 已经集成，Eclipse 之类的不太确定）
* 理论上支持在 Hadoop 集群上运行，为了保证能够在 Windows 环境中运行，这里采用了 Hadoop-3.0.0 版本，[配置教程](https://qiufengyu.github.io/2018/03/15/hadoop-win10/)

请尽量使用相同或类似的开发环境，理论上支持 Linux 等其他环境，且 Hadoop 配置更为方便。


### 项目文件

* `src` 文件夹，涉及项目代码
  * `main/java/com.example.demo` 中是主要实现的功能
    * `controllers` 中是 Spring Boot 项目对应的逻辑处理模块，进行数据、操作交互
    * `data` 中是根据 hadoop 分析结束后的结果，加载为结构化数据，方便在 `controllers` 中使用，增强代码可读性
    * `entity` 中封装了两种网站统计结果的数据结构，被 `data` 中的加载类使用
    * `mapreduce` 中实现了两个 MapReduce 任务：网站访问统计排名和 web 事件统计，分别有 Mapper 和 Reducer 以及启动任务的入口。
  * `main/recources` 中是和展示网页前端的资源文件
    * `static` 中包含了 css 样式、js 脚本、图片等静态资源
    * `templates` 中是渲染的 html 网页
  * `DemoApplication.java` 是 Web 项目运行的入口
* `input`  和 `output` 是文本形式的 hadoop 输入和输出文件，下分 `site` 和 `trans` 两个子文件夹，对应两个任务，需要在运行时指定两者路径
* `pom.xml` 为 Maven 项目的配置文件，给出了依赖的 jar 包
* 两个 xls 表格是原始数据，另存为 csv 格式，并去掉首行表头，就是 `input`  中的内容
* 其他是与项目关系不太大的配置、说明文件 

### 程序运行方式

* 准备工作
  1. Maven 项目，可以在 Intellij IDEA 中直接选择打开，如果是 Eclipse，可以选择 `Import maven project`。之后会根据 `pom.xml` 自动下载配置与依赖的 jar 包，需要等待较长一段时间，推荐提前设置好 maven 国内的源，等待下载完成。
  2. 在 `input/site` 和 `input/trans` 中放入待统计的原始 csv 数据


* 运行 MapReduce 程序，以其中一个 `SiteAnalyzer` 为例

  1. Main class 设置为 `com.example.demo.mapreduce.SiteAnalyzer`，指定任务
  2. Program arguments 设置为：`input/site output/site`，指定输入、输出的路径
  3. 等到运行完毕后，`output/site` 中会有对应的结果

* 运行网页展示程序

  只需要执行 `DemoApplication.java` 中的 `main` 函数，当项目构建好后，会有如下类似的提示，实际可能略有偏差：

  ```
  Tomcat started on port(s): 8080 (http) with context path ''
  Started DemoApplication in 6.67 seconds (JVM running for 11.136)
  ```

  就可以从浏览器访问 localhost:8080 进行操作

