package com;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Random;

// 获取新闻摘要数据
public class DataFetch {

    // 日志记录
    private static final Logger logger = LogManager.getLogger(DataFetch.class);

    // 请求数据地址
    final String[] url_prefix = {
            "http://news.cnr.cn/news/index_%d.html", // 即时要闻
            "http://news.cnr.cn/native/gd/index_%d.html", // 国内新闻
            "http://news.cnr.cn/gjxw/gnews/index_%d.html" // 国际新闻
    };

    final String[] corpus = { "jsyw.txt", "gnxw.txt", "gjxw.txt" };


    // 爬虫连接
    CloseableHttpClient httpclient;


    public DataFetch() {
        httpclient = HttpClients.createDefault();
    }

    public void run(int type) throws IOException {
        logger.info("获取站点并写入文件" + corpus[type] +"中...");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(corpus[type]), "utf-8"));
        for (int i = 1; i<100; ++i) {
            HttpGet httpGet = new HttpGet(String.format(url_prefix[type], i));
            // 设置请求头，防止爬虫被屏蔽
            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
            httpGet.setHeader("Referer", "http://roll.news.sina.com.cn/s/channel.php?ch=01");
            httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate");

            CloseableHttpResponse response = httpclient.execute(httpGet);
            response.setHeader("Content-Type", "text/html");
            HttpEntity entity = response.getEntity();
            // System.out.println(response.getStatusLine());
            String html = "html";
            if (entity != null) {
                html = EntityUtils.toString(entity, "gb2312");
                String htmlutf8 = new String(html.getBytes("utf-8"));
                Document doc = Jsoup.parse(htmlutf8);
                Element articleList = doc.select("div.articlelist").first();
                Elements articles = articleList.select("div.text");
                for (Element article: articles) {
                    String line = article.select("p").first().text();
                    // System.out.println(line);
                    if (line.length() > 1) {
                        bw.write(line + "\n");
                    }
                }
                bw.flush();
            }
            logger.info("已获取第 " + i + " 页数据 / 共 99 页.");
            try {
                // 爬虫暂停一会儿，避免被屏蔽
                Thread.sleep(new Random().nextInt(3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response.close();
            httpGet.releaseConnection();
        }
        bw.close();
        this.httpclient.close();
    }
}
