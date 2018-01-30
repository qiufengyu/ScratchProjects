package com.segment;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.*;

/**
 * Probabilistic Language Model: 概率语言模型
 * 参考：http://spaces.ac.cn/archives/3956/
 */
public class PLM {
    static Map<String, Integer> char2Index;
    // static Map<Integer, String> index2Char;

    static Map<String, Integer> label2Index;
    static Map<Integer, String> index2Label;

    static Vector<Vector<Double>> transitionA;
    static Vector<Vector<Double>> emissionB;
    static Vector<Double> startPi;
    static Set<String> dictionary;

    static final int numOfLabels = 4;
    static int numOfWords;

    String corpusPath;
    String trainingFileString;

    public void setTrainingFileString(String trainingFileString) {
        this.trainingFileString = trainingFileString;
    }

    public void setTestFileString(String testFileString) {
        this.testFileString = testFileString;
    }

    public void setGoldFileString(String goldFileString) {
        this.goldFileString = goldFileString;
    }

    public void setOutputFileString(String outputFileString) {
        this.outputFileString = outputFileString;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public void setDictionaryPath(String dictionaryPath) {
        this.dictionaryPath = dictionaryPath;
    }

    String testFileString;
    String goldFileString;
    String outputFileString;
    String modelPath;
    String dictionaryPath;

    // 默认构造函数，采用默认配置
    public PLM() {
        // 下标转换
        char2Index = new HashMap<String, Integer>();
        label2Index = new HashMap<String, Integer>();
        index2Label = new HashMap<Integer, String>();
        // 模型参数
        transitionA = new Vector<Vector<Double>>();
        emissionB = new Vector<Vector<Double>>();
        startPi = new Vector<Double>();
        // 加载用户词典
        dictionary = new HashSet<String>();

        /**
         *  使用 4-gram 的语言模型，因为中文词一般最长为 4
         *  标签只有 4 种，分别是：
         *  b：单字词或者多字词的首字
         *  c：多字词的第二字
         *  d：多字词的第三字
         *  e：多字词的其余部分
         */
        label2Index.put("b", 0);
        label2Index.put("c", 1);
        label2Index.put("d", 2);
        label2Index.put("e", 3);
        index2Label.put(-1, "b");
        index2Label.put(0, "b");
        index2Label.put(1, "c");
        index2Label.put(2, "d");
        index2Label.put(3, "e");

        /**
         * 统计语料中的字符
         * 需要增加一个 UNK 标识符，表示未登录词
         */
        char2Index.put("UNK", 0);

        // 事实上，必定以 'b' 为开始
        // 为了对数计算的方便，赋予如下初始值，只要保证 'b' 开头的概率远大于其他即可
        startPi.add(Double.MAX_VALUE);
        startPi.add(1.0);
        startPi.add(1.0);
        startPi.add(1.0);

        this.corpusPath = "./corpus/";
        this.trainingFileString = "./corpus/pku_training.utf8";
        this.testFileString = "./corpus/pku_test.utf8";
        this.goldFileString = "./corpus/pku_test_gold.utf8";
        this.outputFileString = "results.txt";
        this.modelPath = "./model/";
        this.dictionaryPath = "./dictionary/";
    }


    public String getTrainingFileString() {
        return trainingFileString;
    }

    public String getTestFileString() {
        return testFileString;
    }

    public String getGoldFileString() {
        return goldFileString;
    }

    public String getOutputFileString() {
        return outputFileString;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getDictionaryPath() {
        return dictionaryPath;
    }

    // corpusPath, trainingFile, testFile, goldFile, outputFile, modelPath, dictionaryPath
    public PLM(String corpusPath,
               String trainingFileString,
               String testFileString,
               String goldFileString,
               String outputFileString,
               String modelPath,
               String dictionaryPath
    ) {
        // 下标转换
        char2Index = new HashMap<String, Integer>();
        label2Index = new HashMap<String, Integer>();
        index2Label = new HashMap<Integer, String>();
        // 模型参数
        transitionA = new Vector<Vector<Double>>();
        emissionB = new Vector<Vector<Double>>();
        startPi = new Vector<Double>();
        // 加载用户词典
        dictionary = new HashSet<String>();

        /**
         *  使用 4-gram 的语言模型，因为中文词一般最长为 4
         *  标签只有 4 种，分别是：
         *  b：单字词或者多字词的首字
         *  c：多字词的第二字
         *  d：多字词的第三字
         *  e：多字词的其余部分
         */
        label2Index.put("b", 0);
        label2Index.put("c", 1);
        label2Index.put("d", 2);
        label2Index.put("e", 3);
        index2Label.put(-1, "b");
        index2Label.put(0, "b");
        index2Label.put(1, "c");
        index2Label.put(2, "d");
        index2Label.put(3, "e");

        /**
         * 统计语料中的字符
         * 需要增加一个 UNK 标识符，表示未登录词
         */
        char2Index.put("UNK", 0);

        // 事实上，必定以 'b' 为开始
        // 为了对数计算的方便，赋予如下初始值，只要保证 'b' 开头的概率远大于其他即可
        startPi.add(Double.MAX_VALUE);
        startPi.add(1.0);
        startPi.add(1.0);
        startPi.add(1.0);

        this.corpusPath = corpusPath;
        this.trainingFileString = trainingFileString;
        this.testFileString = testFileString;
        this.goldFileString = goldFileString;
        this.outputFileString = outputFileString;
        this.modelPath = modelPath;
        this.dictionaryPath = dictionaryPath;
    }

    public void train() throws IOException {
        /**
         *
         */
        File trainFile = new File(trainingFileString);
        System.out.println("第一次处理文件，获取字表...");
        numOfWords = countCharacters(trainFile);

        // 准备工作，在矩阵中全填上 0.0
        for(int i = 0; i<numOfLabels; ++i) {
            Vector<Double> tempA = new Vector<Double>();
            for(int j = 0; j<numOfLabels; ++j)
                tempA.add(0.0);
            transitionA.add(tempA);
            Vector<Double> tempB = new Vector<Double>();
            for(int j = 0; j<numOfWords; ++j)
                tempB.add(0.0);
            emissionB.add(tempB);
        }

        /**
         *
         */
        System.out.println("第二次处理文件，获取转移概率、发射概率矩阵和起始状态矩阵...");
        countMatrix(trainFile);

        saveModel();
    }

    /**
     * 从文件 (corpustest) 中输入句子，输出分词结果
     * @throws IOException
     */
    public void testFile() throws IOException {
        File inputFile = new File(testFileString);
        File outputFile = new File(outputFileString);
        if (emissionB.size() < 1 || transitionA.size() < 1)
            loadModel();
        System.out.println("对测试文件 " + inputFile.getName() + " 进行分词");
        if (inputFile.exists()) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
            while (true) {
                String input = br.readLine();
                if (input == null) {
                    break;
                }
                input = input.trim();
                int inputLength = input.length();
                if (inputLength < 1) {
                    break;
                }
                String output = testSentence(input);
                // System.out.println(output);
                bw.write(output+"\n");
                bw.flush();
            }
            br.close();
            // write to results.txt
            bw.close();
        }
    }

    public double evaluate() throws IOException {
        File resultFile = new File(outputFileString);
        File goldFile = new File(goldFileString);
        BufferedReader brGold = new BufferedReader(
                new InputStreamReader(new FileInputStream(goldFile), "UTF-8")
        );
        BufferedReader brResult = new BufferedReader(
                new InputStreamReader(new FileInputStream(resultFile), "UTF-8")
        );
        long correct = 0;
        long precisionSum = 0;
        long recallSum = 0;
        while (true) {
            String goldLine = brGold.readLine();
            String resultLine = brResult.readLine();
            if (goldLine == null || resultLine == null || goldLine.length() < 1 || resultLine.length() < 1)
                break;
            String[] goldSegs = goldLine.split("\\s+");
            String[] resultSegs = resultLine.split("\\s+");
            precisionSum += goldSegs.length;
            recallSum += resultSegs.length;
            HashSet<String> goldSets = new HashSet<String>(Arrays.asList(goldSegs));
            HashSet<String> resultSets = new HashSet<String>(Arrays.asList(resultSegs));
            HashSet<String> intersection = new HashSet<String>(goldSets);
            intersection.retainAll(resultSets);
            correct += intersection.size();
        }
        double precision = ((double) correct) / precisionSum;
        double recall = ((double) correct) / recallSum;
        double f1 = 2 * precision * recall / (precision + recall);
        System.out.println(String.format("Precision = %.4f%%", precision*100));
        System.out.println(String.format("Recall = %.4f%%", recall*100));
        System.out.println(String.format("F1-Score = %.4f%%", f1*100));
        return f1;
    }

    /**
     * 输入句子，输出分词结果
     * @param input
     * @throws IOException
     */
    public String testSentence(String input) throws IOException {
        if (emissionB.size() < 1 || transitionA.size() < 1)
            loadModel();
        input = input.trim();
        // Viterbi 算法，提高搜索计算的性能
        // 参考：https://en.wikipedia.org/wiki/Viterbi_algorithm （需要科学上网）
        int inputLength = input.length();
        double[][] T1 = new double[numOfLabels][inputLength];
        int[][] T2 = new int[numOfLabels][inputLength];
        int startIndex = 0;
        if (char2Index.get(input.substring(0,1)) != null) {
            startIndex = char2Index.get(input.substring(0,1));
        }
        else {
            // System.out.println(input.substring(0,1));
            // System.out.println("No index of " + input.charAt(0));
        }
        for (int i = 0; i < numOfLabels; ++i) {
            T1[i][0] = Math.log10(startPi.get(i)) + Math.log10(emissionB.get(i).get(startIndex));
            T2[i][0] = 0;
        }
        for (int i = 1; i < inputLength; ++i) {
            for (int j = 0; j < numOfLabels; ++j) {
                double max = Double.NEGATIVE_INFINITY;
                int maxK = -1;
                double temp = 0.0;
                int charIndex = 0;
                if (char2Index.get(input.substring(i, i+1)) != null) {
                    charIndex = char2Index.get(input.substring(i, i+1));
                }
                else {
                    // System.out.println("No index of " + input.charAt(i));
                }
                for (int k = 0; k < numOfLabels; ++k) {
                    temp = T1[k][i - 1]
                            + Math.log10(transitionA.get(k).get(j))
                            + Math.log10(emissionB.get(j).get(charIndex));
                    if (temp >= max) {
                        max = temp;
                        maxK = k;
                    }
                }
                T1[j][i] = max;
                T2[j][i] = maxK;
            }
        }
        String inversedState = "";
        // 从最后一个最大概率状态，通过 T2 往前回溯
        double maxLast = Double.NEGATIVE_INFINITY;
        int maxKLast = -1;
        for (int k = 0; k < numOfLabels; ++k) {
            if (T1[k][inputLength - 1] >= maxLast) {
                maxKLast = k;
                maxLast = T1[k][inputLength - 1];
            }
        }
        inversedState += (index2Label.get(maxKLast));
        for (int i = inputLength - 1; i > 0; i--) {
            maxKLast = T2[maxKLast][i];
            inversedState += (index2Label.get(maxKLast));
        }
        String labels = new StringBuffer(inversedState).reverse().toString();
        String[] labelArray = labels.split("");
        String inputWhile = new String(input);
        // 与字典文件比较，优化分词结果
        for(String x: dictionary) {
            int lengthX = x.length();
            int lastFirstIndex = 0;
            while (inputWhile.contains(x) && input.contains(x)) {
                // System.out.println(inputWhile);
                // System.out.println("匹配到词: "+x);
                int firstIndex = input.indexOf(x, lastFirstIndex);
                lastFirstIndex = firstIndex + lengthX;
                if (firstIndex < 0) {
                    break;
                }
                if (lengthX <= 4) {
                    for (int j = 0; j < lengthX; j++) {
                        if (j+firstIndex < inputLength)
                            labelArray[j+firstIndex] = Character.toString((char)('b' + j));
                    }
                }
                else {
                    for (int j = 0; j < 4; j++) {
                        if (j+firstIndex < inputLength)
                            labelArray[j+firstIndex] = Character.toString((char)('b' + j));
                    }
                    for (int j = 4; j < lengthX; j++) {
                        if (j+firstIndex < inputLength)
                            labelArray[j+firstIndex] = Character.toString('e');
                    }
                }
                if (firstIndex+lengthX < inputLength)
                    labelArray[firstIndex+lengthX] = "b";
                inputWhile = inputWhile.replaceFirst(x, "");
                // System.out.println(inputWhile);
            }
        }
        // 格式化输出为分好词的句子
        String output = ""+input.charAt(0);
        for (int i = 1; i<inputLength; ++i){
            if (labelArray[i].equals("b")) {
                output += (" ");
            }
            output += input.charAt(i);
        }

        return output;
    }

    private void saveModel() throws IOException {
        File model = new File(modelPath);
        if (model.exists()) {
            model.delete();
        }
        model.mkdirs();

        // 存储字表
        BufferedWriter bwCharIndex = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(model.getName() + "/char.dict"), "UTF-8"));
        for(Map.Entry<String, Integer> entry: char2Index.entrySet()) {
            bwCharIndex.write(entry.getKey() + " " + entry.getValue() + "\n");
        }
        bwCharIndex.close();

        // 存储模型参数
        // 稀疏矩阵存储，减少模型文件的大小
        // 概率使用对数的方式存储，避免概率连乘太接近 0 ，难以比较大小

        // 转移概率矩阵
        BufferedWriter bwA = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(model.getName() + "/transitionA.matrix"), "UTF-8"));
        for(int i = 0; i<transitionA.size(); ++i) {
            Vector<Double> v = transitionA.get(i);
            double sum = 0.0;
            for (int j = 0; j<v.size(); ++j) {
                // +1 平滑
                sum += (1+v.get(j));
            }
            for (int j = 0; j<v.size(); ++j) {
                double prob = (1.0+v.get(j))/ sum;
                bwA.write(i +" " + j + " " + prob+"\n");
            }
        }
        bwA.close();
        // 发射概率矩阵
        BufferedWriter bwB = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(model.getName() + "/emissionB.matrix"), "UTF-8"));
        for(int i = 0; i<emissionB.size(); ++i) {
            Vector<Double> v = emissionB.get(i);
            double sum = 0.0;
            for (int j = 0; j<v.size(); ++j) {
                sum += (1.0 + v.get(j));
            }
            for (int j = 0; j<v.size(); ++j) {
                double prob = (1.0 + v.get(j)) / sum;
                bwB.write(i +" " + j + " " + prob+"\n");
            }
            bwB.flush();
        }
        bwB.close();
    }

    public void loadModel() throws IOException {
        File model = new File(modelPath);
        if (!model.exists()) {
            System.out.println("没有模型，请先训练！");
            System.exit(-2);
        }
        BufferedReader brCharIndex = new BufferedReader(new InputStreamReader(
                new FileInputStream(modelPath+"/char.dict"), "UTF-8"));
        while(true) {
            String line = brCharIndex.readLine();
            if (line == null)
                break;
            String[] tuple = line.trim().split(" ");
            if (tuple.length == 2) {
                int index = Integer.parseInt(tuple[1]);
                char2Index.put(tuple[0], index);
            }
        }
        brCharIndex.close();
        numOfWords = char2Index.size();
        System.out.println("总单字数：" + numOfWords + ".");

        /**
         * 读取模型参数，预处理
         */
        transitionA.clear();
        emissionB.clear();
        for(int i = 0; i<numOfLabels; ++i) {
            Vector<Double> tempA = new Vector<Double>();
            for(int j = 0; j<numOfLabels; ++j)
                tempA.add(0.0);
            transitionA.add(tempA);
            Vector<Double> tempB = new Vector<Double>();
            for(int j = 0; j<numOfWords; ++j)
                tempB.add(0.0);
            emissionB.add(tempB);
        }

        BufferedReader brA = new BufferedReader(new InputStreamReader(
                new FileInputStream(modelPath+"/transitionA.matrix"), "UTF-8"));
        while(true) {
            String line = brA.readLine();
            if (line == null)
                break;
            String[] tuple = line.trim().split(" ");
            if (tuple.length == 3) {
                int label1Index = Integer.parseInt(tuple[0]);
                int label2Index = Integer.parseInt(tuple[1]);
                double prob = Double.parseDouble(tuple[2]);
                transitionA.get(label1Index).set(label2Index, prob);
            }
        }
        brA.close();
        BufferedReader brB = new BufferedReader(new InputStreamReader(
                new FileInputStream(modelPath+"/emissionB.matrix"), "UTF-8"));
        while(true) {
            String line = brB.readLine();
            if (line == null)
                break;
            String[] tuple = line.trim().split(" ");
            try {
                if (tuple.length == 3) {
                    int labelIndex = Integer.parseInt(tuple[0]);
                    int charIndex = Integer.parseInt(tuple[1]);
                    double prob = Double.parseDouble(tuple[2]);
                    emissionB.get(labelIndex).set(charIndex, prob);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                for(String t : tuple)
                    System.out.println(t);
                System.err.println("模型文件不匹配，请重新训练！");
            }
        }
        brB.close();

        // 加载字典文件
        System.out.println("加载用户词典文件");
        File dictFile = new File(dictionaryPath);
        if (dictFile.exists()) {
            File[] files = dictFile.listFiles();
            for (File f: files) {
                if (f.getName().endsWith("txt") || f.getName().endsWith("utf8")) {
                    BufferedReader brDict = new BufferedReader(new InputStreamReader(
                            new FileInputStream(f), "UTF-8"));
                    while (true) {
                        String line = brDict.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.length() > 1) {
                            dictionary.add(line.trim());
                        }
                    }
                }
            }
        }
    }

    private int countCharacters(File trainFile) throws IOException {
        // HashSet<String> wordSet = new HashSet<>();
        if (trainFile.exists()) {
            ArrayList<File> fileList = new ArrayList<File>();
            if (trainFile.isFile()) {
                fileList.add(trainFile);
            } else if (trainFile.isDirectory()) {
                for (File f : trainFile.listFiles()) {
                    if (f.getName().endsWith("training.utf8")) {
                        fileList.add(f);
                    }
                }
            }
            for (File f : fileList) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(f), "UTF-8"));
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.length() < 2) {
                        continue;
                    }
                    String[] tokens = line.split("\\s+");
                    for (String t : tokens) {
                        String[] tuple = t.split("/");
                        if (!char2Index.containsKey(tuple[0])) {
                            char2Index.put(tuple[0], char2Index.size());
                        }
                    }
                }
                br.close();
            }
        }
        System.out.println("共有单字："+char2Index.size()+".");
        return char2Index.size();
    }

    private void countMatrix(File trainFile) throws IOException {
        // 开始统计
        double oldValue = 0.0;
        if (trainFile.exists()) {
            ArrayList<File> fileList = new ArrayList<File>();
            if (trainFile.isFile()) {
                fileList.add(trainFile);
            }
            else if (trainFile.isDirectory()) {
                for (File f: trainFile.listFiles()) {
                    if (f.getName().endsWith("training.utf8")) {
                        fileList.add(f);
                    }
                }
            }
            for (File f: fileList) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(f), "UTF-8"));
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.length() > 2) {
                        String[] tokens = line.split("\\s+");
                        String firstToken = tokens[0];
                        if (firstToken.length() > 2) {
                            String[] tuple = firstToken.split("/");
                            if (tuple.length == 2) {
                                int labelIndex = label2Index.get(tuple[1]);
                                // 起始概率：startPi -> 其实标记必定为 'b'
                                // double oldValue = startPi.get(labelIndex);
                                // startPi.set(labelIndex, oldValue + 1.0);
                                // 发射概率：emissionB
                                if (char2Index.get(tuple[0]) != null) {
                                    int charIndex = char2Index.get(tuple[0]);
                                    oldValue = emissionB.get(labelIndex).get(charIndex);
                                    emissionB.get(labelIndex).set(charIndex, oldValue + 1.0);
                                }
                            }
                        }
                        for (int i = 1; i < tokens.length; ++i) {
                            String[] currentTuple = tokens[i].split("/");
                            String[] previousTuple = tokens[i - 1].split("/");
                            // 一些错误格式的数据先忽略
                            if (currentTuple.length == 2 && label2Index.get(currentTuple[1]) != null) {
                                int labelIndex = label2Index.get(currentTuple[1]);
                                int charIndex = 0;
                                if (char2Index.get(currentTuple[0]) != null) {
                                    charIndex = char2Index.get(currentTuple[0]);
                                }
                                oldValue = emissionB.get(labelIndex).get(charIndex);
                                emissionB.get(labelIndex).set(charIndex, oldValue + 1.0);
                                if (previousTuple.length == 2 && label2Index.get(previousTuple[1]) != null) {
                                    int previousLableIndex = label2Index.get(previousTuple[1]);
                                    oldValue = transitionA.get(previousLableIndex).get(labelIndex);
                                    transitionA.get(previousLableIndex).set(labelIndex, 1.0 + oldValue);
                                }
                            }
                        }
                    }
                }
                br.close();
                System.out.println("文件 " + f.getName() + " 已经处理！");
            }
        }
    }
}
