package offline.crawler;

import offline.utils.Category;
import offline.utils.DatabaseUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;


public class NewsCrawler {

    // 日志记录
    static final Logger logger = LogManager.getLogger(NewsCrawler.class.getName());

    // 请求地址
    final String newsUrl = "http://roll.news.sina.com.cn/interface/rollnews_ch_out_interface.php";

    // 爬虫连接
    CloseableHttpClient httpClient;

    // 存储一些公共的请求头信息
    List<NameValuePair> nvps;

    // 数据库操作相关
    DatabaseUtil db;

    public NewsCrawler() {
        logger.info("爬虫初始化...");
        httpClient = HttpClients.createDefault();
        nvps = new ArrayList<NameValuePair>();
        // 请求参数
        //col=89&spec=&type=&ch=01&k=&offset_page=0&offset_num=0&num=60&asc=&page=1
        nvps.add(new BasicNameValuePair("col", "89"));
        nvps.add(new BasicNameValuePair("spec", ""));
        nvps.add(new BasicNameValuePair("type", ""));
        nvps.add(new BasicNameValuePair("ch", "01"));
        nvps.add(new BasicNameValuePair("k", ""));
        nvps.add(new BasicNameValuePair("offset_page", "0"));
        nvps.add(new BasicNameValuePair("offset_num", "0"));
        nvps.add(new BasicNameValuePair("num", "60"));
        nvps.add(new BasicNameValuePair("asc", ""));
        // page 需要动态调整，因为要爬取多页的内容

        // 初始化一个Database 对象
        db = new DatabaseUtil();
        logger.info("数据连接完毕！");

    }

    public void run(int start, int end) throws URISyntaxException, IOException, SQLException {
        for (int i = start; i <= end; ++i) {
            HttpGet httpGet = new HttpGet(newsUrl);
            URIBuilder uriBuilder = new URIBuilder(httpGet.getURI()).addParameters(nvps);
            uriBuilder.addParameter("page", String.valueOf(i));
            httpGet.setURI(uriBuilder.build());
            // System.out.println(uriBuilder);
            httpGet.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
            httpGet.setHeader("Referer", "http://roll.news.sina.com.cn/s/channel.php?ch=01");
            httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate");
            httpGet.setHeader("Host", "roll.news.sina.com.cn");

            CloseableHttpResponse response = httpClient.execute(httpGet);
            response.setHeader("Content-Type", "text/html");

            HttpEntity entity = response.getEntity();
            // System.out.println(response.getStatusLine());
            if (entity != null) {
                // 日志记录
                logger.info("爬取第 " + i + " 页的滚动新闻...");
                // 中文乱码解决方案：http://blog.csdn.net/pkuyjxu/article/details/8712220
                String gbk = new String(EntityUtils.toString(entity).getBytes("8859_1"), "gb2312");
                // System.out.println("response content: " + gbk2utf8);
                // System.out.println(gbk);
                gbk = gbk.replaceAll("\\\\\"", "");
                gbk = gbk.replaceAll("\\\\", "");
                gbk = gbk.substring(gbk.indexOf('{'));
                // System.out.println(gbk);
                String gbk2utf8 = new String(gbk.getBytes("utf-8"), "utf-8");
                JSONObject obj = new JSONObject(gbk2utf8);
                JSONArray array = obj.getJSONArray("list");
                //System.out.println(array);
                for(int j = 0; j<array.length(); ++j) {
                    NewsModel nm = new NewsModel();
                    JSONObject jobj = array.getJSONObject(j);
                    // System.out.println(jobj);
                    String category =jobj.getJSONObject("channel").getString("title");
                    nm.setCategory(Category.cate2Int(category));
                    String title = jobj.getString("title");
                    nm.setTitle(title);
                    String url = jobj.getString("url");
                    if (!db.checkUrl(url)) {
                        nm.setLink(url);
                        Integer time = jobj.getInt("time");
                        nm.setTime(time);
                        // System.out.println(nm);
                        TitleResult tr = getContent(url);
                        if (tr.getContent().length() > 1) {
                            nm.setContent(tr.getContent());
                            if (tr.getTitle().length() > 1) {
                                title = tr.getTitle();
                                nm.setTitle(title);
                            }
                            logger.info("成功获取 《" + title + "》新闻内容！");
                            db.insertNews(nm);
                        }
                        try {
                            Thread.sleep(new Random().nextInt(10000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        logger.warn("新闻已存在于数据库中，跳过");
                    }
                }
            }
            // 释放资源，避免卡死
            httpGet.releaseConnection();
            response.close();
        }
        // 程序结束后关闭连接
        closeHttpClient();

    }

    private void closeHttpClient() throws IOException {
        httpClient.close();
    }

    public TitleResult getContent(String url) throws IOException {
        //CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        httpGet.setHeader("Referer", "http://roll.news.sina.com.cn/s/channel.php?ch=01");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Connection", "keep-alive");

        CloseableHttpResponse response = httpClient.execute(httpGet);

        // System.out.println(response.getStatusLine());
        TitleResult tr = new TitleResult();
        String result = "";
        String html = "html";
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            // logger.info(url);
            html = EntityUtils.toString(entity, "utf-8");
            Document doc = Jsoup.parse(html);
            if (doc.select("h1.main-title").first() != null) {
                tr.setTitle(doc.select("h1.main-title").first().text());
            }
            else {
                tr.setTitle("");
            }
            Element article = doc.select("div.article").first();
            if (article == null) {
                article = doc.selectFirst("div#artibody");
            }
            if (article != null) {
                Elements paras = article.select("p");
                if (paras != null) {
                    ArrayList<String> paraList = new ArrayList<String>();
                    for (Element e : paras) {
                        String line = e.text().trim().replace("　", "");
                        if (line.length() > 1)
                            paraList.add(line);
                    }
                    result = String.join("\n", paraList.toArray(new String[paraList.size()]));
                    tr.setContent(result);
                }
            }
        }
        // 释放资源，避免卡死
        response.close();
        httpGet.releaseConnection();
        return tr;
    }

    class TitleResult {
        private String title;
        private String content;

        public TitleResult() {
            title = new String();
            content = new String();
        }

        public TitleResult(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
