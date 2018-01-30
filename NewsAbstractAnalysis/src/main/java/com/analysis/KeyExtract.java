package com.analysis;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.util.*;


public class KeyExtract {

    // 日志记录
    private static final Logger logger = LogManager.getLogger(KeyExtract.class);

    private boolean success;
    NLPIRSeg nlpirSeg;
    Map<String, Integer> wordCount;

    // 定义接口CLibrary，继承自com.sun.jna.Library
    public interface CLibraryKeyExtractor extends Library {
        public CLibraryKeyExtractor instance = (CLibraryKeyExtractor) Native.loadLibrary("KeyExtract", CLibraryKeyExtractor.class);

        public boolean KeyExtract_Init( String sDataPath, int encode, String sLicenceCode);

        public boolean KeyExtract_Exit();

        public String KeyExtract_GetKeyWords(String sLine, int nMaxKeyLimit, boolean bWeightOut);

        public String KeyExtract_GetFileKeyWords(String sFilename, int nMaxKeyLimit, boolean bWeightOut);

    }

    public KeyExtract() throws IOException {
        success = initKeyExtract();
        //
        nlpirSeg = new NLPIRSeg();

        if (!success) {
            wordCount = new HashMap<String, Integer>();
            loadMyKeyExtract();
        }


    }

    private void loadMyKeyExtract() throws IOException {
        File f = new File("mykeyextract.txt");
        if (f.exists()) {
            wordCount.clear();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("mykeyextract.txt"), "utf-8"));
            while(true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                else {
                    line = line.trim();
                    String[] pair = line.split("\t");
                    if (pair.length == 2) {
                        wordCount.put(pair[0], Integer.valueOf(pair[1]));
                    }
                }
            }
        }
        else {
            generateMyKeyExtract();
            loadMyKeyExtract();
        }
    }

    private boolean initKeyExtract() throws UnsupportedEncodingException {
        String argu = "";
        // String system_charset = "GBK";//GBK----0
        String system_charset = "UTF-8";
        int charset_type = 1;
        // int charset_type = 0;
        // 调用printf打印信息
        boolean init_flag = CLibraryKeyExtractor.instance.KeyExtract_Init(argu, charset_type, "");
        if (init_flag) {
            logger.info("KeyExtractor 初始化成功！");
        }
        else {
            logger.warn("KeyExtractor 初始化失败，使用自定义的关键词抽取方式！");
        }
        return init_flag;
    }

    public ArrayList<WordWeight> keyword(String sInput, int maxKeyLimit) {
        TreeSet<String> keywords = new TreeSet<>();
        ArrayList<WordWeight> wordWeights = new ArrayList<>();
        if (success) {
            String keyWordsStr = CLibraryKeyExtractor.instance.KeyExtract_GetKeyWords(sInput, maxKeyLimit, true);
            CLibraryKeyExtractor.instance.KeyExtract_Exit();
            String[] tokens = keyWordsStr.split("#");
            for (String x: tokens) {
                String[] parts = x.split("/");
                String word = parts[0];
                double weight = Double.valueOf(parts[2]);
                wordWeights.add(new WordWeight(word, weight));
            }
            return wordWeights;
        }
        else { // 许可文件不可用，所以先用自己统计出的结果
            // 现已更新，可用
            String splitted = nlpirSeg.segment(sInput, 3);
            String[] tokens = splitted.split(" ");
            ArrayList<WordCount> wordCountList = new ArrayList<WordCount>();
            for(String x : tokens) {
                int count = 0;
                String[] wordPosTag = x.split("/");
                // 首先某些专有名词是重要的，词性标记以 n 开头，但还有其他修饰符，强制输出
                // 某些不及物动词 (vi) 也可以认为是关键词
                if (wordPosTag.length == 2) {
                    String tag = wordPosTag[1].substring(wordPosTag[1].indexOf('/')+1);
                    if (tag.length() >= 2) {
                        if (tag.startsWith("n") && wordPosTag[0].length() > 1) {
                            keywords.add(wordPosTag[0]);
                        }
                        else if(tag.startsWith("vi") && wordPosTag[0].length() > 1) {
                            keywords.add(wordPosTag[0]);
                        }
                    }
                }
                // 对于其他词的策略：这个词的长度需要大于等于2，即不考虑一些单字（）
                if(wordPosTag.length == 2 && wordPosTag[0].length() > 1) {
                    if (wordCount.get(x) != null) {
                        count = wordCount.get(x);
                    }
                    WordCount wc = new WordCount(x, count);
                    wordCountList.add(wc);
                }
            }
            // 对词的重要性排序
            Collections.sort(wordCountList);
            for(int i = 0; i<maxKeyLimit; ++i) {
                String rawString = wordCountList.get(i).getWord();
                String[] wordAndPosTag = rawString.split("/");
                if (wordAndPosTag.length == 2) {
                    // 一些修饰性的词语过滤掉
                    if (! (wordAndPosTag[1].startsWith("a") ||
                            wordAndPosTag[1].startsWith("d") ||
                            wordAndPosTag[1].startsWith("s")) ) {
                        String word = rawString.substring(0, rawString.indexOf('/'));
                        keywords.add(word);
                    }
                }
            }
            // 去掉一些其他的词，请自定义
            keywords.remove("记者");
            keywords.remove("今天");
            keywords.remove("今年");
            System.out.println(keywords);
            for(String k: keywords) {
                wordWeights.add(new WordWeight(k, (double)wordCount.get(k)));
            }
            return wordWeights;
        }
    }

    private void generateMyKeyExtract() throws IOException {
        // 提取关键词时，不考虑长度为1的单字
        BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream("gjxw_seg_pos.txt"), "utf-8"));
        while(true) {
            String line = br1.readLine();
            if (line == null)
                break;
            line = line.trim();
            String[] tokens = line.split(" ");
            for (String x: tokens) {
                if (x.length() >= 2) {
                    if (wordCount.get(x) != null) {
                        wordCount.put(x, wordCount.get(x) + 1);
                    } else
                        wordCount.put(x, 1);
                }
            }
        }
        br1.close();
        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream("gnxw_seg_pos.txt"), "utf-8"));
        while(true) {
            String line = br2.readLine();
            if (line == null)
                break;
            line = line.trim();
            String[] tokens = line.split(" ");
            for (String x: tokens) {
                if (x.length() >= 2) {
                    if (wordCount.get(x) != null) {
                        wordCount.put(x, wordCount.get(x) + 1);
                    } else
                        wordCount.put(x, 1);
                }
            }
        }
        br2.close();
        BufferedReader br3 = new BufferedReader(new InputStreamReader(new FileInputStream("jsyw_seg_pos.txt"), "utf-8"));
        while(true) {
            String line = br3.readLine();
            if (line == null)
                break;
            line = line.trim();
            String[] tokens = line.split(" ");
            for (String x: tokens) {
                if (x.length() >= 2) {
                    if (wordCount.get(x) != null) {
                        wordCount.put(x, wordCount.get(x) + 1);
                    } else
                        wordCount.put(x, 1);
                }
            }
        }
        br3.close();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("mykeyextract.txt"), "utf-8"));
        for(Map.Entry<String, Integer> entry: wordCount.entrySet()) {
            bw.write(entry.getKey() + "\t" + entry.getValue() +"\n");
        }
        bw.close();
    }

    public ArrayList<WordCount> getWordCount(String sInput) {
        String splitted = nlpirSeg.segment(sInput, 3);
        String[] tokens = splitted.split(" ");
        ArrayList<WordCount> wordCountList = new ArrayList<WordCount>();
        for(String x : tokens) {
            int count = 0;
            String[] wordPosTag = x.split("/");
            if (wordCount.get(x) != null) {
                count = wordCount.get(x);
            }
            if(wordPosTag[0].length() > 1) {
                if (wordPosTag[1].length() > 1) {
                    WordCount wc = new WordCount(wordPosTag[0], count);
                    wordCountList.add(wc);
                }
                else if (count > 1) {
                    WordCount wc = new WordCount(wordPosTag[0], count);
                    wordCountList.add(wc);
                }
            }
        }
        return wordCountList;
    }


}
