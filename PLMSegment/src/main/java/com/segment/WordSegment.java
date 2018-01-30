package com.segment;

import javax.swing.*;
import java.io.*;

/**
 * Hello world!
 *
 */
public class WordSegment
{
    public static void main( String[] args ) throws IOException {
        System.out.println( "Hello World!" );
        /**
         * 从原始数据集预处理为 corpus 文件夹中的训练语料
         * 增加 b,c,d,e 的标注
         */
        // CorpusPreProcess cpp = new CorpusPreProcess();
        // cpp.cleanData();

        String corpusPath = "./corpus/";
        String trainingFile = "./corpus/pku_training.utf8";
        String testFile = "./corpus/pku_test.utf8";
        String goldFile = "./corpus/pku_test_gold.utf8";
        String outputFile = "results.txt";
        String modelPath = "./model/";
        String dictionaryPath = "./dictionary/";

        SegUI segUI = new SegUI();
        segUI.main(args);


        // PLM plm = new PLM();
        // PLM plm = new PLM(corpusPath, trainingFile, testFile, goldFile, outputFile, modelPath, dictionaryPath);
        // plm.train();
        // plm.testFile();
        // System.out.println(plm.testSentence("这是一句测试样例：共产党像太阳，照到哪里哪里亮。"));
        // plm.evaluate();

        System.out.println("Finished!");
    }
}
