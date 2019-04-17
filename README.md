## 外包项目集合

### 存放那些年做过的外包项目。

#### 为了精简项目文件的大小，对于所用的数据集都只保留了很小的一部分，完整数据可见源代码注释、相关文档等。
#### 不保证一定能顺利运行，记录代码实现的思想，仅做纪念。
#### 愿祖国更加繁荣昌盛，以上。

### 1. [电子病历命名实体识别](./DianZiBingLiNER)
识别病历文本中的药品、症状、疾病等命名实体

### 2. [概率语言模型分词](./PLMSegment)
基于概率语言模型的中文分词，效果十分一般

### 3. [基于自然语言的新闻摘要处理](./NewsAbstractAnalysis)
爬虫获取新闻摘要，对新闻摘要分词、提取关键词，利用 Apriori 算法挖掘频繁项集

### 4. [Bayes 垃圾邮件分类](./SpamBayesWeb)
使用 Bayes 分类器过滤垃圾邮件和正常邮件，支持 Web UI 进行操作

### 5. [决策树垃圾邮件分类](./SpamDT)

使用决策树对邮件进行是否垃圾邮件的分类，支持简单 Web UI

### 6. [微博爬虫与情感分析](./WeiboSentiment)
Scrapy 爬虫，SnowNLP 情感分析

### 7. [新浪滚动新闻分类与检索](./SinaNewsSearchAnalysis)

获取新浪滚动新闻，使用 Lucene 建立索引以供搜索，deeplearning4j 进行简单的文本分类，基于 Spring Boot 的 Web 应用

### 8. [金融（股票）数据采集与分析](./FinanceStockAnalysis)

获取股票的历史行情和新浪股吧的相关讨论，简单预测开盘、收盘价格及涨跌趋势

### 9. [垃圾邮件过滤（贝叶斯&MLP)](./SpamBayesMLP)

使用朴素贝叶斯分类器和多层感知机对垃圾邮件进行分类与过滤，Java，Deeplearning4j & Spring Boot

### 10. [Hadoop学生考试成绩统计分析](./HadoopGrades)

使用 Hadoop MapReduce 对某校学生在计算机课程基础这门课上的考试成绩进行统计和分析

### 11. [新闻自动摘要与关键词提取](./NewsAbExtraction)

借助 SnowNLP 实现的基于 TextRank 算法的自动摘要和关键词提取技术，解析新闻

### 12. [公路运输文本数据的分析与转换](./Highway)

通过简单的 UI 交互，支持交通事件的添加与查找和简单统计信息的查询，掌握了一点点前端的东西

### 13. [垃圾邮件智能处理系统](./SpamMail)

实现了简单的邮件系统，同时支持贝叶斯分类识别垃圾邮件，用了一个不怎么成功的模糊匹配工具

### 14. [基于机器学习的 WebShell 检测](./WebShellDetect)

运用了 Bayes、MLP、CNN 三种模型，对 php 脚本进行恶意检测，脚本来源 github 和一些开源项目

### 15. [基于机器的剧本生成方法研究与实现](./ScriptWriter)

RNN（LSTM）文本生成，创作剧本

### 16. [基于 SVM 算法的垃圾邮件过滤系统设计](./SVMSpam)

使用 SVM 模型对邮件分类，SMO 学习算法，PyQt5 简单界面

### 17.  [基于Python 的网络爬虫](./WebSpider)

HTTP 获取新浪新闻，python 简单多线程，支持关键词检索

### 18. [基于贝叶斯的短信过滤系统](./SpamMessage)

全新的界面设计，没有其他改进