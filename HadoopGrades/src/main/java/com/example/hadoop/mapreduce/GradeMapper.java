package com.example.hadoop.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

public class GradeMapper extends Mapper<Object, Text, Text, Text> {
    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        FileSplit fileSplit = (FileSplit) context.getInputSplit();
        String fileName = fileSplit.getPath().getName();
        String[] parts = new String(value.toString().trim()).split(",");
        if(parts.length == 4) {
            String year = fileName.substring(0, 4);
            // 年份和专业作为 key： year#专业
            // 写三个value，也通过#分割：
            // value1 是分数
            // value2 是合格不合格
            // value3 用来计算人数，就是 1
            Text keyYear = new Text(String.valueOf(year) + "#" + parts[1]);
            Text valueYear = new Text("1#" + parts[2] + "#" + parts[3]);
            context.write(keyYear, valueYear);
        }



    }
}
