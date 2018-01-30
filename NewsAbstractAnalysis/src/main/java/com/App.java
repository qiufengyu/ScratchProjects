package com;

import com.analysis.*;
import com.apriori.Apriori;
import com.apriori.AprioriPrepare;
import com.apriori.AprioriUI;
import com.apriori.ShowApriori;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        System.out.println( "Hello World!" );

        // 一、使用 GUI 进行操作，单句句子分析
        // AnalysisUI aui = new AnalysisUI();
        // aui.show();

        // 二、使用 GUI，对文本进行分词、Apriori 算法挖掘。
        AprioriUI apui = new AprioriUI();
        apui.show();

        /**
         * 一下是各个小功能的测试，都集成在上面的 GUI 操作中了
         */

        // 1. 这是获取数据用的
        // 分别设置 df.run() 的参数为0,1,2，获取三个来源的数据
        // 为了使项目配置简单，请使用参数 = 0，即 df.run(0)
        // DataFetch df = new DataFetch();
        // df.run(0);

        // 2. 使用 NLPIR 分词工具进行分词
        // NLPIRSeg nlpirSeg = new NLPIRSeg();
        // 第三个参数指定是否要带上词性标注，0 代表仅分词，1,2,3 表示词性标注的粒度，一般选3
        // nlpirSeg.segmentFile("jsyw.txt", "jsyw_seg.txt", 0);

        // 3. 分析新闻中的关键词（名词、动词、形容词），同样借助 NLPIR 分词工具
        // 问题：当前的 NLPIR 的关键词提取模块的证书似乎还没有更新，暂时没法用，自己简单实现了一个
        // KeyExtract ke = new KeyExtract();
        // int maxKeyLimit = 10; // 选择显示多少个词
        // String sentence = "法新社援引埃及国家电视台消息称，埃及北西奈省清真寺发生的袭击事件造成184人死亡、至少125人受伤。";
        // ArrayList<WordWeight> wwList = ke.keyword(sentence, 10);
        // 可视化结果
        // ArrayList<WordCount> wcList = ke.getWordCount("法新社援引埃及国家电视台消息称，埃及北西奈省清真寺发生的袭击事件造成184人死亡、至少125人受伤。");
        // KeywordUI keywordUI = new KeywordUI("新闻关键词分析", sentence+"\n词语指数 TOP "+maxKeyLimit, wwList);
        // keywordUI.display();

        // 4. Apriori 算法，文本关系挖掘
        // 算法的准备与运行
        // AprioriPrepare aprioriPrpare = new AprioriPrepare("jsyw_seg.txt");
        // Apriori apriori = new Apriori(aprioriPrpare.getWordVocabluary(), aprioriPrpare.getLines(), 0.0075);
        // 算法结果
        // ShowApriori showApriori = new ShowApriori();
        // System.out.println(showApriori.show());


    }
}
