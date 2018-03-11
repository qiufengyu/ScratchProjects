package com.example.spam.offline;

public class MakeFeature {
    int features;
    String[] featureWords;

    // 不特地写个函数读取文件了，直接把 54 个特征词放进去
    public MakeFeature() {
        features = 57;
        featureWords = new String[54];
        featureWords[0] = "make";
        featureWords[1] = "address";
        featureWords[2] = "all";
        featureWords[3] = "3d";
        featureWords[4] = "our";
        featureWords[5] = "over";
        featureWords[6] = "remove";
        featureWords[7] = "internet";
        featureWords[8] = "order";
        featureWords[9] = "mail";
        featureWords[10] = "receive";
        featureWords[11] = "will";
        featureWords[12] = "people";
        featureWords[13] = "report";
        featureWords[14] = "addresses";
        featureWords[15] = "free";
        featureWords[16] = "business";
        featureWords[17] = "email";
        featureWords[18] = "you";
        featureWords[19] = "credit";
        featureWords[20] = "your";
        featureWords[21] = "font";
        featureWords[22] = "000";
        featureWords[23] = "money";
        featureWords[24] = "hp";
        featureWords[25] = "hpl";
        featureWords[26] = "george";
        featureWords[27] = "650";
        featureWords[28] = "lab";
        featureWords[29] = "labs";
        featureWords[30] = "telnet";
        featureWords[31] = "857";
        featureWords[32] = "data";
        featureWords[33] = "415";
        featureWords[34] = "85";
        featureWords[35] = "technology";
        featureWords[36] = "1999";
        featureWords[37] = "parts";
        featureWords[38] = "pm";
        featureWords[39] = "direct";
        featureWords[40] = "cs";
        featureWords[41] = "meeting";
        featureWords[42] = "original";
        featureWords[43] = "project";
        featureWords[44] = "re";
        featureWords[45] = "edu";
        featureWords[46] = "table";
        featureWords[47] = "conference";
        featureWords[48] = ";";
        featureWords[49] = "(";
        featureWords[50] = "[";
        featureWords[51] = "!";
        featureWords[52] = "$";
        featureWords[53] = "#";
    }

    public double[] makeTextFeature(String text) {
        double[] ret = new double[57];
        int textLength = countTextLength(text);
        for(int i = 0; i<featureWords.length; ++i) {
            ret[i] = ((double) countWordFrequency(featureWords[i], text)) / textLength * 100.0;
        }
        ret[54] = capitalRunLengthAverage(text);
        ret[55] = (double) capitalRunLengthLongest(text);
        ret[56] = (double) capitalRunLengthTotal(text);
        return ret;
    }

    // 计算句子中的词数
    private int countTextLength(String text) {
        String[] dummy = text.split("\\s+");
        return dummy.length;
    }

    // 计算一个句子中某个单词出现的次数
    private int countWordFrequency(String word, String text) {
        String textLowercase = text.toLowerCase();
        int lastIndex = 0;
        int count = 0;
        while(lastIndex != -1) {
            lastIndex =textLowercase.indexOf(word, lastIndex);
            // 能找到这个单词
            if (lastIndex != -1) {
                count ++;
                // 继续往后查找
                lastIndex += word.length();
            }
        }
        return count;
    }

    // 计算平均句长
    private double capitalRunLengthAverage(String text) {
        // 按照大写字符切分句子
        String[] x = text.split("[A-Z]");
        int sumSentenceLength = 0;
        for(String xx: x) {
            String[] y = xx.split("\\s+");
            sumSentenceLength += y.length;
        }
        return (double) sumSentenceLength / x.length;
    }

    // 计算最长的句子长度
    private int capitalRunLengthLongest(String text) {
        // 按照大写字符切分句子
        String[] x = text.split("[A-Z]");
        int maxSentenceLength = 0;
        for(String xx: x) {
            String[] y = xx.split("\\s+");
            if (y.length > maxSentenceLength) {
                maxSentenceLength = y.length;
            }
        }
        return maxSentenceLength;
    }

    // 计算总的句子数
    private int capitalRunLengthTotal(String text) {
        return text.split("[A-Z]").length;
    }
}
