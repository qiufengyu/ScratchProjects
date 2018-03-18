package com.example.hadoop;

import com.example.hadoop.mapreduce.Grade;
import com.example.hadoop.preprocess.PreProcess;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class HadoopApplication {

	public static void main(String[] args) throws Exception {

		System.out.println("Hello World!");

		// 1. 数据预处理，从 xls 表格文件转化为 csv 文本文件
		// 从 raw 中读取，结果写入 input 文件夹中
		// new PreProcess().excelReader();

		// 2. 使用 Hadoop 对成绩进行统计和分析
		// input 为输入文件，output 输出的是文字报告结果
		// 同时会在数据库中写入数据，方便 Web 提取查询数据
		// new Grade().gradeRunner(args);

		// 3. Web 界面展示统计结果
		SpringApplication.run(HadoopApplication.class, args);
	}
}
