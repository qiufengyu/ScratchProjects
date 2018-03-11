package com.example.spam.offline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Vector;

public class Bayes {
    // 日志记录
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 数据集信息
    int numInputs;
    int examples;
    double[][] data;
    int[] labels;

    // 模型的参数
    double[] spamMean;
    double[] spamStdev;
    double[] hamMean;
    double[] hamStdev;
    double spamProbability;
    double hamProbability;

    boolean modelValid;

    final String filenameTrian = "spamdata/spambase.data";

    // 特征提取
    MakeFeature mf;

    public Bayes() {
        numInputs = 57;
        examples = 4601;
        // 把 spambase.data 数据读到数组中，方便计算
        data = new double[examples][numInputs];
        labels = new int[examples];
        spamMean = new double[numInputs];
        spamStdev = new double[numInputs];
        hamMean = new double[numInputs];
        hamStdev = new double[numInputs];
        spamProbability = 0.0;
        hamProbability = 0.0;
        modelValid = false;
        mf = new MakeFeature();
    }

    private void readData() throws IOException {
        logger.info("读取数据...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filenameTrian), "utf-8"));
        String line;
        int lineCount = 0;
        while(true) {
            line = br.readLine();
            if(line == null)
                break;
            String[] features = line.split(",");
            for(int i = 0; i<numInputs; ++i) {
                data[lineCount][i] = Double.valueOf(features[i]);
            }
            labels[lineCount] = Integer.valueOf(features[numInputs]);
            lineCount++;
        }
        br.close();
        logger.info("读取数据完毕！");
    }

    // Bayes 模型训练过程
    public void calculate(String bayesModelFile) throws IOException {
        // 先读数据
        readData();
        logger.info("开始训练模型...");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bayesModelFile), "utf-8"));
        int spamCount = 0;
        for(int i = 0; i<examples; ++i) {
            spamCount += labels[i];
        }
        double spamProb = (double) spamCount / examples;
        double hamProb = 1.0 - spamProb;
        // System.out.println(spamProb);
        // System.out.println(hamProb);
        // 第一行写两种类型的概率
        bw.write(spamProb + "," + hamProb+"\n");
        // 对每一维特征进行统计，计算平均值与标准差
        for(int i = 0; i<numInputs; ++i) {
            Vector<Double> tempSpam = new Vector<>();
            Vector<Double> tempHam = new Vector<>();
            double tempSpamSum = 0.0;
            double tempSpamMean = 0.0;
            double tempSpamStdev = 0.0;
            double tempHamSum = 0.0;
            double tempHamMean = 0.0;
            double tempHamStdev = 0.0;
            for(int j = 0; j<examples; ++j) {
                // 非垃圾邮件
                if(labels[j] == 0) {
                    tempHam.add(data[j][i]);
                    tempHamSum += data[j][i];
                } else {
                    tempSpam.add(data[j][i]);
                    tempSpamSum += data[j][i];
                }
            }
            // 计算该特征在两类中的平均值
            tempHamMean = tempHamSum / tempHam.size();
            tempSpamMean = tempSpamSum / tempSpam.size();
            bw.write(tempSpamMean + "," + tempHamMean + ",");
            // 计算该特征在两类中的标准差，为了防止之后出现 0 值，所以设置一个比较小的数
            double tempHamSquareSum = 1e-12;
            for(Double d: tempHam) {
                tempHamSquareSum += Math.pow(d-tempHamMean, 2);
            }
            tempHamStdev = Math.sqrt(tempHamSquareSum / (tempHam.size()-1));
            double tempSpamSquareSum = 1e-12;
            for(Double d: tempSpam) {
                tempSpamSquareSum += Math.pow(d-tempSpamMean, 2);
            }
            tempSpamStdev = Math.sqrt(tempSpamSquareSum / (tempSpam.size()-1));
            bw.write(tempSpamStdev + "," + tempHamStdev + "\n");
            bw.flush();
        }
        bw.close();
        logger.info("模型训练完毕！");

    }

    // 从训练好的模型文件（bayes_model.txt）加载
    private void loadModel(String bayesModelFile) throws IOException {
        logger.info("加载模型...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(bayesModelFile), "utf-8"));
        String line = br.readLine();
        String[] probs = line.split(",");
        spamProbability = Double.valueOf(probs[0]);
        hamProbability = Double.valueOf(probs[1]);
        int lineCount = 0;
        while(true) {
            line = br.readLine();
            if (line == null)
                break;
            String[] values = line.split(",");
            spamMean[lineCount] = Double.valueOf(values[0]);
            hamMean[lineCount] = Double.valueOf(values[1]);
            spamStdev[lineCount] = Double.valueOf(values[2]);
            hamStdev[lineCount] = Double.valueOf(values[3]);
            lineCount++;
        }
        modelValid = true;
        br.close();
        logger.info("模型加载完毕！");
    }

    public double[] testTrainingData() throws IOException {
        if(!modelValid)
            loadModel("bayes_model.txt");
        readData();
        logger.info("开始测试...");
        int correctSpam = 0;
        int correctHam = 0;
        int totalSpam = 0;
        int totalHam = 0;
        int correct = 0;
        for(int i = 0; i<examples; ++i) {
            int r = testByFeature(data[i]);
            if (labels[i] == 0) {
                totalHam++;
            }
            else {
                totalSpam++;
            }
            if(r == labels[i]) {
                correct++;
                if(labels[i] == 0)
                    correctHam++;
                else
                    correctSpam++;
            }
        }
        double acc = (double) correct / examples * 100;
        logger.warn(String.format("平均准确率：%.4f%%", acc));
        double accSpam = (double) correctSpam / totalSpam * 100;
        double accHam = (double) correctHam / totalHam * 100;
        logger.warn(String.format("识别出垃圾邮件的准确率：%.4f%%", accSpam));
        logger.warn(String.format("识别出正确邮件的准确率：%.4f%%", accHam));
        double[] values = new double[3];
        values[0] = acc;
        values[1] = accSpam;
        values[2] = accHam;
        return values;
    }

    public int testBayes(String text) throws IOException {
        if (!modelValid) {
            loadModel("bayes_model.txt");
        }
        double[] features = mf.makeTextFeature(text);
        int ret = testByFeature(features);
        return ret;
    }

    private int testByFeature(double[] features) {
        if (spamProbability(features) > hamProbability(features)) {
            return 1;
        } else {
            return 0;
        }
    }

    private double spamProbability(double[] features) {
        // 连续性变量，计算高斯概率，https://baike.baidu.com/item/%E6%AD%A3%E6%80%81%E5%88%86%E5%B8%83/829892?fr=aladdin
        // 为了防止概率连乘，数据过小溢出，改成对数相加的形式
        double spamProb = 0.0;
        double expValue = 0.0;
        double gauss_prob = 0.0;
        for(int i = 0; i<numInputs; ++i) {
            expValue = Math.exp(- Math.pow(features[i] - spamMean[i], 2) / (2.0 * spamStdev[i] * spamStdev[i]));
            gauss_prob = 1.0 / ( Math.sqrt(2.0 * Math.PI) * spamStdev[i]) * expValue + 1e-12;
            // 防止下溢出，加上一个小一点的数，不影响结果
            spamProb += Math.log(gauss_prob);
        }
        spamProb += Math.log(spamProbability);
        return spamProb;
    }

    private double hamProbability(double[] features) {
        // 连续性变量，计算高斯概率，https://baike.baidu.com/item/%E6%AD%A3%E6%80%81%E5%88%86%E5%B8%83/829892?fr=aladdin
        // 为了防止概率连乘，数据过小溢出，改成对数相加的形式
        double hamProb = 0.0;
        double expValue = 0.0;
        double gauss_prob = 0.0;
        for(int i = 0; i<numInputs; ++i) {
            expValue = Math.exp(- Math.pow(features[i] - hamMean[i], 2) / (2.0 * hamStdev[i] * hamStdev[i]));
            gauss_prob = 1.0 / ( Math.sqrt(2.0 * Math.PI) * hamStdev[i]) * expValue + 1e-12;
            // 防止下溢出，加上一个小一点的数，不影响结果
            hamProb += Math.log(gauss_prob);
        }
        hamProb += Math.log(hamProbability);
        return hamProb;
    }
}
