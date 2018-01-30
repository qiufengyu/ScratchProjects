package com.apriori;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

public class AprioriPrepare {

    // 日志记录
    private static final Logger logger = LogManager.getLogger(AprioriPrepare.class);

    Map<String, Integer> wordCount;
    Map<String, Integer> word2Index;
    Map<Integer, String> index2Word;


    int lines = 0;

    public AprioriPrepare(String fileName) throws IOException {
        wordCount = new HashMap<>();
        word2Index = new HashMap<>();
        index2Word = new HashMap<>();
        // 为了加快程序运行的速度，所以只用了 jsyw 新闻摘要文本做分析
        // 数据规模大概是 1000 条
        // 为进一步减小规模，词汇表只保留出现频率大于等于 3 的词
        // 为什么可以这么做？本身频繁项集就又一定的频率要求，所以这里先过滤掉那些频率太低的词，也可以减小规模
        generateWordVoc(fileName);
        // 否则数据量过大，程序运行太慢了
        // 如果要分析其他的新闻语料，请修改程序中对应的文件名 'jsyw_seg.txt' (不含词性标注的分词结果文件，并删除
        loadVocabulary();
        // 之后只运行 Apriori 算法的话，可以注释掉下面一行
        preProcessData(fileName); // 将文本文件转化为 0，1 矩阵的输入形式
    }

    public int getWordVocabluary() {
        return word2Index.size();
    }

    public int getLines() {
        return lines;
    }


    private void loadVocabulary() throws IOException {
        logger.info("加载字典文件...");
        word2Index.clear();
        index2Word.clear();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("vocabulary.txt"), "utf-8"));
        int id = 0;
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            line = line.trim();
            if (line.length() > 0) {
                word2Index.put(line, id);
                index2Word.put(id, line);
                id++;
            }
        }
        logger.info("字典文件加载完毕！");
    }

    private void preProcessData(String inputFile) throws IOException {
        logger.info("生成输入文件...");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("apriori.txt"), "utf-8"));
        ArrayList<Integer> temp = new ArrayList<>();
        BufferedReader br3 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
        while(true) {
            String line = br3.readLine();
            if (line == null)
                break;
            line = line.trim();
            lines++;
            String[] tokens = line.split(" ");
            temp.clear();
            for(int i = 0; i<word2Index.size(); ++i) {
                temp.add(0);
            }
            for (String x: tokens) {
                if (x.length() > 0) {
                    if (word2Index.get(x) != null) {
                        temp.set(word2Index.get(x), 1);
                    }
                }
            }
            for(int j = 0; j<temp.size()-1; ++j) {
                bw.write(temp.get(j)+" ");
                bw.flush();
            }
            bw.write(temp.get(temp.size()-1)+"\n");
        }
        br3.close();
        bw.close();
        logger.info("已生成文件：apriori.txt，共有 " + lines + " 条记录。");
    }

    private void generateWordVoc(String inputFile) throws IOException {
        logger.info("生成字典文件...");
        BufferedReader br3 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
        while(true) {
            String line = br3.readLine();
            if (line == null)
                break;
            line = line.trim();
            String[] tokens = line.split(" ");
            for (String x: tokens) {
                if (x.length() > 0) {
                    int count = 0;
                    if (wordCount.get(x.trim()) != null)
                        count = wordCount.get(x.trim())+1;
                    wordCount.put(x.trim(), count);
                }
            }
        }
        br3.close();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("vocabulary.txt"), "utf-8"));
        int i = 0;
        for(Map.Entry<String, Integer> entry: wordCount.entrySet()) {
            // 太低频的词和太高频的（常用汉字）不进行统计
            if (entry.getValue() > 4 && entry.getValue() <= 50) {
                bw.write(entry.getKey() + "\n");
                bw.flush();
                i++;
            }
        }
        bw.close();
        logger.info("已生成字典文件 vocabulary.txt，共有单词 " + i + " 个。");
    }
}
