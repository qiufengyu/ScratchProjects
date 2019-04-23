package com.example.demo.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.net.URL;

public class TransAnalyzer {

    // Mapper 部分
    public static class TransMapper
            extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text transAndIP = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] parts = value.toString().trim().split(",");
            if(parts.length == 9) {
                String ip = parts[0];
                // 获取处理的事务
                String siteString = parts[4];
                // key 设置为“域名#IP" 的形式，进行统计
                transAndIP.set(siteString + "#" + ip);
                    // value 部分就是一个统计值，1
                context.write(transAndIP, one);
            }
        }
    }

    // Reducer 部分
    public static class TransReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            // 计数求和
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    // 主函数
    public static void main(String[] args) throws Exception {
        // 指定 hadoop 环境
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "trans analysis");
        job.setJarByClass(TransAnalyzer.class);
        job.setMapperClass(TransMapper.class);
        job.setCombinerClass(TransReducer.class);
        job.setReducerClass(TransReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        // 如果 output 目录已存在，则删除
        Path outputPath = new Path(args[1]);
        outputPath.getFileSystem(conf).delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
