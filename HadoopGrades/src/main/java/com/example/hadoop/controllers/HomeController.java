package com.example.hadoop.controllers;

import com.example.hadoop.db.DBEntity;
import com.example.hadoop.db.DBTools;
import com.example.hadoop.preprocess.Utils;

import org.apache.logging.log4j.message.StringFormattedMessage;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


@Controller
public class HomeController {

    DBTools dbTools = new DBTools();

    // 访问首页，显示 index.html 内容
    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView homeModelAndView = new ModelAndView("index");
        return homeModelAndView;
    }

    // 首页上点击提交，此处接受请求，返回数据
    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.POST)
    public ModelAndView home(@RequestParam(name = "yearSelect") String yearSelect,
                              @RequestParam(name = "departSelect") String departSelect,
                              @RequestParam(name = "itemSelect") String itemSelect ) throws IOException, InterruptedException, SQLException {
        ModelAndView homeModelAndView = new ModelAndView("index");
        int yearSelInt = Integer.valueOf(yearSelect);
        int departSelInt = Integer.valueOf(departSelect);
        int itemSelInt = Integer.valueOf(itemSelect);
        String departName = Utils.int2Department(departSelInt);
        DBEntity entity = dbTools.select(yearSelInt, departName);
        if(itemSelInt == 1) {
            // 查询全部的信息
            homeModelAndView.addObject("tableTitle", departName + " " + String.valueOf(yearSelInt) + " 年考试成绩总体情况");
            // 考试人数等具体信息
            homeModelAndView.addObject("counts", String.valueOf(entity.getTotalCount()));
            homeModelAndView.addObject("average", String.format("%.2f", entity.getAverage()));
            homeModelAndView.addObject("gradeA", String.format("%.2f%%", entity.getGradeAPercent()));
            homeModelAndView.addObject("gradeB", String.format("%.2f%%", entity.getGradeBPercent()));
            homeModelAndView.addObject("gradeC", String.format("%.2f%%", entity.getGradeCPercent()));
            homeModelAndView.addObject("gradeD", String.format("%.2f%%", entity.getGradeDPercent()));
            homeModelAndView.addObject("gradeF", String.format("%.2f%%", entity.getGradeFPercent()));
            homeModelAndView.addObject("excellent", String.format("%.2f%%", entity.getExcellentPercent()));
            homeModelAndView.addObject("quali", String.format("%.2f%%", entity.getQualifiedPercent()));
            homeModelAndView.addObject("failed", String.format("%.2f%%", entity.getFailedPercent()));
            // 成绩等级比例
            homeModelAndView.addObject("gradeACount", (int) Math.ceil(entity.getGradeAPercent() * entity.getTotalCount()) / 100);
            homeModelAndView.addObject("gradeBCount", (int) Math.ceil(entity.getGradeBPercent() * entity.getTotalCount()) / 100);
            homeModelAndView.addObject("gradeCCount", (int) Math.ceil(entity.getGradeCPercent() * entity.getTotalCount()) / 100);
            homeModelAndView.addObject("gradeDCount", (int) Math.ceil(entity.getGradeDPercent() * entity.getTotalCount()) / 100);
            homeModelAndView.addObject("gradeFCount", (int) Math.ceil(entity.getGradeFPercent() * entity.getTotalCount()) / 100);
            // 优秀、合格、不合格比例

            homeModelAndView.addObject("excellentCount", (int) Math.ceil(entity.getExcellentPercent() * entity.getTotalCount()) / 100);
            homeModelAndView.addObject("qualifiedCount", (int) Math.ceil(entity.getQualifiedPercent() * entity.getTotalCount()) / 100);
            homeModelAndView.addObject("failedCount",(int) Math.ceil(entity.getFailedPercent() * entity.getTotalCount()) / 100);
            // homeModelAndView.addObject("qualifieddata", qualifiedData);
        }
        else {
            // 查询单个项目
            String itemTitle = departName + " " + String.valueOf(yearSelInt) + " 年" + Utils.int2Item(itemSelInt);
            homeModelAndView.addObject("itemTitle", itemTitle);
            String itemResult = "";
            switch (itemSelInt) {
                case 2: itemResult = "总考试人数：" + entity.getTotalCount(); break;
                case 3: itemResult = "平均分：" + String.format("%.2f", entity.getAverage()); break;
                case 4: itemResult = ">=90 分占比：" + String.format("%.2f%%", entity.getGradeAPercent()); break;
                case 5: itemResult = "80-89 分占比：" + String.format("%.2f%%",entity.getGradeBPercent()); break;
                case 6: itemResult = "70-79 分占比" + String.format("%.2f%%", entity.getGradeCPercent()); break;
                case 7: itemResult = "60-69 分占比：" + String.format("%.2f%%", entity.getGradeDPercent()); break;
                case 8: itemResult = "<60 分占比：" + String.format("%.2f%%", entity.getGradeFPercent()); break;
                case 9: itemResult = "优秀占比：" + String.format("%.2f%%", entity.getExcellentPercent()); break;
                case 10: itemResult = "合格占比：" + String.format("%.2f%%", entity.getQualifiedPercent()); break;
                case 11: itemResult = "不合格占比：" + String.format("%.2f%%", entity.getFailedPercent()); break;
                default: itemResult = "不支持该选项！"; break;
            }
            homeModelAndView.addObject("itemResult", itemResult);
        }
        // 返回上一次选择的记录
        homeModelAndView.addObject("val1", yearSelInt);
        homeModelAndView.addObject("val2", departSelInt);
        homeModelAndView.addObject("val3", itemSelInt);
        return homeModelAndView;
    }
}