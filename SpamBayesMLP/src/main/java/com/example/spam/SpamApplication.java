package com.example.spam;

import com.example.spam.offline.Bayes;
import com.example.spam.offline.MLP;
import com.example.spam.offline.MakeFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SpamApplication {

	public static void main(String[] args) {
		// 启动网页应用
		// 如果使用下面的各个分部分测试的代码，需要注释下一行代码
		SpringApplication.run(SpamApplication.class, args);

		/*
		//  贝叶斯模型
		Bayes bayes = null;
		try {
			bayes = new Bayes();
			bayes.calculate("bayes_model.txt");
			bayes.testTrainingData();
			System.out.println(bayes.testBayes("For me the love should start with attraction.i should feel that I need her every time around me.she should be the first thing which comes in my thoughts.I would start the day and end it with her.she should be there every time I dream.love will be then when my every breath has her name.my life should happen around her.my life will be named to her.I would cry for her.will give all my happiness and take all her sorrows.I will be ready to fight with anyone for her.I will be in love when I will be doing the craziest things for her.love will be when I don't have to proove anyone that my girl is the most beautiful lady on the whole planet.I will always be singing praises for her.love will be when I start up making chicken curry and end up makiing sambar.life will be the most beautiful then.will get every morning and thank god for the day because she is with me.I would like to say a lot..will tell later.."));
			System.out.println(bayes.testBayes("Had your contract mobile 11 Mnths? Latest Motorola, Nokia etc. all FREE! Double Mins & Text on Orange tariffs. TEXT YES for callback, no to remove from records."));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/


		/* 测试 多层感知机模型的学习过程
		MLP mlp = new MLP(3, new int[]{100, 50, 25});
		// 训练部分
		try {
			mlp.train();
			mlp.eval("spamdata/spambase.data");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		/*
		MLP mlp = new MLP();
		try {
			System.out.println(mlp.test("For me the love should start with attraction.i should feel that I need her every time around me.she should be the first thing which comes in my thoughts.I would start the day and end it with her.she should be there every time I dream.love will be then when my every breath has her name.my life should happen around her.my life will be named to her.I would cry for her.will give all my happiness and take all her sorrows.I will be ready to fight with anyone for her.I will be in love when I will be doing the craziest things for her.love will be when I don't have to proove anyone that my girl is the most beautiful lady on the whole planet.I will always be singing praises for her.love will be when I start up making chicken curry and end up makiing sambar.life will be the most beautiful then.will get every morning and thank god for the day because she is with me.I would like to say a lot..will tell later.."));
			System.out.println(mlp.test("Had your contract mobile 11 Mnths? Latest Motorola, Nokia etc. all FREE! Double Mins & Text on Orange tariffs. TEXT YES for callback, no to remove from records."));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

		/* 子功能测试：将文本转换成一个向量
		MakeFeature mf = new MakeFeature();
		double[] a = mf.makeTextFeature("For me the love should start with attraction.i should feel that I need her every time around me.she should be the first thing which comes in my thoughts.I would start the day and end it with her.she should be there every time I dream.love will be then when my every breath has her name.my life should happen around her.my life will be named to her.I would cry for her.will give all my happiness and take all her sorrows.I will be ready to fight with anyone for her.I will be in love when I will be doing the craziest things for her.love will be when I don't have to proove anyone that my girl is the most beautiful lady on the whole planet.I will always be singing praises for her.love will be when I start up making chicken curry and end up makiing sambar.life will be the most beautiful then.will get every morning and thank god for the day because she is with me.I would like to say a lot..will tell later..,,,");
		for(int i = 0; i<a.length; ++i) {
			System.out.print(a[i] + ",");
		}
		*/
	}
}
