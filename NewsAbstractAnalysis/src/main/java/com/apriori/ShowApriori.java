package com.apriori;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 这个类通过图表展示 Apriori 算法分析的结果
 * 显示经常共现的词
 */
public class ShowApriori {

    // 日志记录
    private static final Logger logger = LogManager.getLogger(Apriori.class);

    Map<String, Integer> word2Index;
    Map<Integer, String> index2Word;

    public ShowApriori() throws IOException {
        word2Index = new HashMap<>();
        index2Word = new HashMap<>();
        loadVocabulary();
    }

    public ArrayList<String> show() throws IOException {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> tempList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("result.txt"), "utf-8"));
        while (true) {
            tempList.clear();
            String line = br.readLine();
            if (line == null) {
                break;
            }
            String[] tokens = line.split(" ");
            int len = tokens.length-1;

            // 只关注 >= 2 的频繁项集
            if (len > 1) {
                for(int i = 0; i < len; ++i) {
                    tempList.add(index2Word.get(Integer.valueOf(tokens[i])-1));
                }
                tempList.add(tokens[tokens.length-1]);
                result.add(String.join(" ", tempList));
            }
        }
        return result;
    }

    public ArrayList<AprioriResult> showk(int k) throws IOException {
        ArrayList<AprioriResult> result = new ArrayList<>();
        ArrayList<String> tempList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("result.txt"), "utf-8"));
        while (true) {
            tempList.clear();
            String line = br.readLine();
            if (line == null) {
                break;
            }
            String[] tokens = line.split(" ");
            int len = tokens.length-1;

            // 只关注为 k 的频繁项集
            if (len == k) {
                for(int i = 0; i < len; ++i) {
                    tempList.add(index2Word.get(Integer.valueOf(tokens[i])-1));
                }
                result.add(new AprioriResult(String.join(" ", tempList), Double.valueOf(tokens[tokens.length-1])));
            }
        }
        return result;
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

}
