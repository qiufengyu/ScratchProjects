package com.example.hadoop.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Grade {

    public static int gradeRunner(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "grade analysis");
        job.setJarByClass(Grade.class);
        job.setMapperClass(GradeMapper.class);
        job.setReducerClass(GradeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        MultipleOutputs.addNamedOutput(job, "2014", TextOutputFormat.class, Text.class, Text.class);
        MultipleOutputs.addNamedOutput(job, "2015", TextOutputFormat.class, Text.class, Text.class);
        MultipleOutputs.addNamedOutput(job, "2016", TextOutputFormat.class, Text.class, Text.class);
        MultipleOutputs.addNamedOutput(job, "2017", TextOutputFormat.class, Text.class, Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        // 如果 output 目录已存在，则删除
        Path outputPath = new Path(args[1]);
        outputPath.getFileSystem(conf).delete(outputPath, true);

        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        return job.waitForCompletion(true) ? 0 : 1;
    }
}
