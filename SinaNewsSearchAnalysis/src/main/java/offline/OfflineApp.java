package offline;

import offline.analysis.NewsClassification;
import offline.analysis.NewsFeature;
import offline.crawler.NewsCrawler;
import offline.lucene.NewsIndexer;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Hello world!
 *
 */
public class OfflineApp
{
    public static void main( String[] args ) throws URISyntaxException, IOException, SQLException, ParseException, InterruptedException {
        // 1. 新闻爬虫，从新浪滚动新闻获取数据
        // NewsCrawler nc = new NewsCrawler();
        // nc.run(1, 10);
        // nc.getContent("http://finance.sina.com.cn/money/forex/forexfxyc/2018-01-22/doc-ifyquptv8669266.shtml");
        // 2. Lucene 检索
        NewsIndexer ni = new NewsIndexer("./luceneindex");
        ni.writeIndexer();
        // System.out.println(ni.queryIndexer("中国", 10));
        // 3. Word2Vec 与 数据提取
        // NewsFeature nf = new NewsFeature();
        // 下面两行可以只运行一次，因为运行一次要 6-8 分钟或更久
        // nf.generatRawTextFile("corpus.txt");
        // nf.trainWord2Vec("corpus.txt");
        // nf.generateTrainData("train.txt");
        // 4. 分类
        // NewsClassification nc = new NewsClassification(100, 10, 50);
        // 训练一个分类模型
        // nc.train();
        // nc.eval("train.txt");
        // System.out.println(nc.test("克里米亚近百名小学生学造雷排雷 为培养入伍兴趣", 1));

    }
}

