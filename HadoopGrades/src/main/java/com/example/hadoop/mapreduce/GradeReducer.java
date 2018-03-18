package com.example.hadoop.mapreduce;

import com.example.hadoop.db.DBEntity;
import com.example.hadoop.db.DBTools;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TreeMap;

public class GradeReducer extends Reducer<Text, Text, Text, Text> {

    private final String excellentString = "优秀";
    private final String qualifiedString = "合格";
    private final String failedString = "不合格";

    private long[] sumAll;
    private int[] countAll;
    private int[] gradeAAll, gradeBAll, gradeCAll, gradeDAll, gradeFAll;
    private int[] excellentAll, qualifiedAll, failedAll;

    private MultipleOutputs mos;
    private DBTools dbTools;

    protected void setup(Context context) {
        // 初始化这些全局的统计量
        sumAll = new long[4];
        countAll = new int[4];
        gradeAAll = new int[4];
        gradeBAll = new int[4];
        gradeCAll = new int[4];
        gradeDAll = new int[4];
        gradeFAll = new int[4];
        excellentAll = new int[4];
        qualifiedAll = new int[4];
        failedAll = new int[4];

        mos = new MultipleOutputs<Text, Text>(context);
        dbTools = new DBTools();
    }

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        long sum = 0;
        int count = 0;
        int gradeA = 0, gradeB = 0, gradeC = 0, gradeD = 0, gradeF = 0;
        int excellent = 0, qualified = 0, failed = 0;
        String kText = key.toString();
        // 说明是含有年份的主键
        if(kText.contains("#")) {
            String[] keySplit = kText.split("#");
            String y = keySplit[0];
            int yIndex = Integer.valueOf(y) - 2014;
            String major = keySplit[1];
            for(Text t: values) {
                String[] valueSplit = t.toString().split("#");
                // 统计总人数
                count += Integer.valueOf(valueSplit[0]);
                countAll[yIndex] += Integer.valueOf(valueSplit[0]);
                // 统计分数段，等级成绩比例
                int score = Integer.valueOf(valueSplit[1]);
                if(score >= 90) {
                    gradeA += 1;
                    gradeAAll[yIndex] += 1;
                } else if(score >=80) {
                    gradeB += 1;
                    gradeBAll[yIndex] += 1;
                } else if(score >=70) {
                    gradeC += 1;
                    gradeCAll[yIndex] += 1;
                } else if(score >= 60) {
                    gradeD += 1;
                    gradeDAll[yIndex] += 1;
                } else {
                    gradeF += 1;
                    gradeFAll[yIndex] += 1;
                }
                sum += score;
                sumAll[yIndex] += score;
                // 优秀、合格、不合格统计
                String level = valueSplit[2];
                if (level.equals(excellentString)) {
                    excellent += 1;
                    excellentAll[yIndex] += 1;
                } else if (level.equals(qualifiedString)) {
                    qualified += 1;
                    qualifiedAll[yIndex] += 1;
                } else {
                    failed += 1;
                    failedAll[yIndex] += 1;
                }
            }

            // 得到统计结果，进行简单的计算
            // 平均分
            double average = (double) sum / count;
            // 等级比例
            double gradeAPercent = (double) gradeA / count * 100.0;
            double gradeBPercent = (double) gradeB / count * 100.0;
            double gradeCPercent = (double) gradeC / count * 100.0;
            double gradeDPercent = (double) gradeD / count * 100.0;
            double gradeFPercent = (double) gradeF / count * 100.0;
            // 优秀合格不合格比例
            double excellentPercent = (double) excellent / count * 100.0;
            double qualifiedPercent = (double) qualified / count * 100.0;
            double failedPercent = (double) failed / count * 100.0;
            mos.write(y, new Text(major + "总人数："), new Text(String.format("%d", count)));
            mos.write(y, new Text(major + "平均分："), new Text(String.format("%.2f", average)));
            mos.write(y, new Text(major + " >=90 占比："), new Text(String.format("%.2f%%", gradeAPercent)));
            mos.write(y, new Text(major + " >=80, <90 占比："), new Text(String.format("%.2f%%", gradeBPercent)));
            mos.write(y, new Text(major + " >=70, <60 占比："), new Text(String.format("%.2f%%", gradeCPercent)));
            mos.write(y, new Text(major + " >=60, <70 占比："), new Text(String.format("%.2f%%", gradeDPercent)));
            mos.write(y, new Text(major + " <60 占比："), new Text(String.format("%.2f%%", gradeFPercent)));
            mos.write(y, new Text(major + "优秀占比："), new Text(String.format("%.2f%%", excellentPercent)));
            mos.write(y, new Text(major + "合格占比："), new Text(String.format("%.2f%%", qualifiedPercent)));
            mos.write(y, new Text(major + "不合格占比："), new Text(String.format("%.2f%%", failedPercent)));
            // 写入数据库
            DBEntity dbe = new DBEntity(major, count, average,
                    gradeAPercent, gradeBPercent, gradeCPercent, gradeDPercent, gradeFPercent,
                    excellentPercent, qualifiedPercent, failedPercent);
            try {
                dbTools.insert(Integer.valueOf(y), dbe);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("数据库插入异常！");
            }
        }
    }

    protected void cleanup(Context context) throws IOException, InterruptedException {
        String major = "全校";
        for(int i = 0; i<4; ++i) {
            String y = String.valueOf(i+2014);
            // 得到统计结果，进行简单的计算
            // 平均分
            double average = (double) sumAll[i] / countAll[i];
            // 等级比例
            double gradeAPercent = (double) gradeAAll[i] / countAll[i] * 100.0;
            double gradeBPercent = (double) gradeBAll[i] / countAll[i] * 100.0;
            double gradeCPercent = (double) gradeCAll[i] / countAll[i] * 100.0;
            double gradeDPercent = (double) gradeDAll[i] / countAll[i] * 100.0;
            double gradeFPercent = (double) gradeFAll[i] / countAll[i] * 100.0;
            // 优秀合格不合格比例
            double excellentPercent = (double) excellentAll[i] / countAll[i] * 100.0;
            double qualifiedPercent = (double) qualifiedAll[i] / countAll[i] * 100.0;
            double failedPercent = (double) failedAll[i] / countAll[i] * 100.0;

            mos.write(y, new Text(major +"总人数："), new Text(String.format("%d", countAll[i])));
            mos.write(y, new Text(major +"平均分："), new Text(String.format("%.2f", average)));
            mos.write(y, new Text(major +" >=90 占比："), new Text(String.format("%.2f%%", gradeAPercent)));
            mos.write(y, new Text(major +" >=80, <90 占比："), new Text(String.format("%.2f%%", gradeBPercent)));
            mos.write(y, new Text(major +" >=70, <60 占比："), new Text(String.format("%.2f%%", gradeCPercent)));
            mos.write(y, new Text(major +" >=60, <70 占比："), new Text(String.format("%.2f%%", gradeDPercent)));
            mos.write(y, new Text(major + " <60 占比："), new Text(String.format("%.2f%%", gradeFPercent)));
            mos.write(y, new Text(major + "优秀占比："), new Text(String.format("%.2f%%", excellentPercent)));
            mos.write(y, new Text(major + "合格占比："), new Text(String.format("%.2f%%", qualifiedPercent)));
            mos.write(y, new Text(major + "不合格占比："), new Text(String.format("%.2f%%", failedPercent)));
            DBEntity dbe = new DBEntity(major, countAll[i], average,
                    gradeAPercent, gradeBPercent, gradeCPercent, gradeDPercent, gradeFPercent,
                    excellentPercent, qualifiedPercent, failedPercent);
            try {
                dbTools.insert(Integer.valueOf(y), dbe);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("数据库插入异常！");
            }
        }
        mos.close();
    }
}
