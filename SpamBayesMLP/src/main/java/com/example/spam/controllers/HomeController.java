package com.example.spam.controllers;

import com.example.spam.offline.Bayes;
import com.example.spam.offline.MLP;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;


@Controller
public class HomeController {

    private Bayes bayes = new Bayes();
    private MLP mlp = new MLP(3, new int[]{114, 50, 25});

    // 访问首页，显示 index.html 内容
    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView homeModelAndView = new ModelAndView("index");
        return homeModelAndView;
    }

    // 首页上点击提交，此处接受请求，返回数据
    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.POST)
    public ModelAndView home(@RequestParam(name = "algSelect") String algSelect) throws IOException, InterruptedException {
        ModelAndView homeModelAndView = new ModelAndView("index");
        int algSelInt = Integer.valueOf(algSelect);
        if(algSelInt == 0) {
            homeModelAndView.addObject("algTitle", "朴素贝叶斯分类结果");
            File f = new File("bayes_model.txt");
            if(!f.exists())
                bayes.calculate("bayes_model.txt");
            double[] results = bayes.testTrainingData();
            homeModelAndView.addObject("averageAcc", String.format("平均准确率：%.4f%%", results[0]));
            homeModelAndView.addObject("spamAcc", String.format("识别出垃圾邮件的准确率：%.4f%%", results[1]));
            homeModelAndView.addObject("hamAcc", String.format("识别出正常邮件的准确率：%.4f%%", results[2]));
        }
        else {
            homeModelAndView.addObject("algTitle", "多层感知器分类结果");
            File f = new File("model.zip");
            if(!f.exists())
                mlp.train();
            // mlp.eval("spamdata/spambase.data");
            double[] results = mlp.testTrainingData();
            homeModelAndView.addObject("averageAcc", String.format("平均准确率：%.4f%%", results[0]));
            homeModelAndView.addObject("spamAcc", String.format("识别出垃圾邮件的准确率：%.4f%%", results[1]));
            homeModelAndView.addObject("hamAcc", String.format("识别出正常邮件的准确率：%.4f%%", results[2]));
        }
        return homeModelAndView;
    }
}
