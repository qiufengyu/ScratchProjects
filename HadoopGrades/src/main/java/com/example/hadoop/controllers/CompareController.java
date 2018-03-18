package com.example.hadoop.controllers;

import com.example.hadoop.db.DBEntity;
import com.example.hadoop.db.DBTools;
import com.example.hadoop.preprocess.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class CompareController {

    DBTools dbTools = new DBTools();

    // 访问首页，显示 index.html 内容
    @RequestMapping(value = {"/compare"}, method = RequestMethod.GET)
    public ModelAndView indexCompare() {
        ModelAndView homeModelAndView = new ModelAndView("compare");
        return homeModelAndView;
    }

    // 首页上点击提交，此处接受请求，返回数据
    @RequestMapping(value = {"/compare"}, method = RequestMethod.POST)
    public ModelAndView homeCompare(@RequestParam(name = "departSelect") String departSelect,
                                    @RequestParam(name = "itemSelect") String itemSelect) throws IOException, InterruptedException, SQLException {
        ModelAndView homeModelAndView = new ModelAndView("compare");
        int departSelInt = Integer.valueOf(departSelect);
        String departName = Utils.int2Department(departSelInt);
        int itemSelInt = Integer.valueOf(itemSelect);

        DBEntity entity2014 = dbTools.select(2014, departName);
        DBEntity entity2015 = dbTools.select(2015, departName);
        DBEntity entity2016 = dbTools.select(2016, departName);
        DBEntity entity2017 = dbTools.select(2017, departName);
        List<String> list2014 = makeList(2014, entity2014);
        List<String> list2015 = makeList(2015, entity2015);
        List<String> list2016 = makeList(2016, entity2016);
        List<String> list2017 = makeList(2017, entity2017);
        if(itemSelInt == 1) {
            homeModelAndView.addObject("showTable", 1);
            // 全部，显示一张表格
            List<List<String>> llist = new ArrayList<List<String>>();
            llist.add(list2014);
            llist.add(list2015);
            llist.add(list2016);
            llist.add(list2017);
            homeModelAndView.addObject("llist", llist);
        }
        else {
            homeModelAndView.addObject("single", 1);
            String singleTitle = departName + " " + Utils.int2Item(itemSelInt) + "对比图";
            // 返回的是整数列表
            if(itemSelInt == 2) {
                int[] values = new int[] {
                        Integer.valueOf(list2014.get(1)),
                        Integer.valueOf(list2015.get(1)),
                        Integer.valueOf(list2016.get(1)),
                        Integer.valueOf(list2017.get(1)) };
                homeModelAndView.addObject("values", values);
            }
            else if (itemSelInt >= 3 && itemSelInt <= 11) {
                double[] values = new double[] {
                        Double.valueOf(list2014.get(itemSelInt-1).replace("%", "")),
                        Double.valueOf(list2015.get(itemSelInt-1).replace("%", "")),
                        Double.valueOf(list2016.get(itemSelInt-1).replace("%", "")),
                        Double.valueOf(list2017.get(itemSelInt-1).replace("%", "")) };
                homeModelAndView.addObject("values", values);
            }
            homeModelAndView.addObject("singleTitle", singleTitle);
        }
        homeModelAndView.addObject("val2", departSelInt);
        homeModelAndView.addObject("val3", itemSelInt);
        return homeModelAndView;
    }

    private List<String> makeList(int year, DBEntity entity) {
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(year));
        list.add(String.format("%d", entity.getTotalCount()));
        list.add(String.format("%.2f", entity.getAverage()));
        list.add(String.format("%.2f%%", entity.getGradeAPercent()));
        list.add(String.format("%.2f%%", entity.getGradeBPercent()));
        list.add(String.format("%.2f%%", entity.getGradeCPercent()));
        list.add(String.format("%.2f%%", entity.getGradeDPercent()));
        list.add(String.format("%.2f%%", entity.getGradeFPercent()));
        list.add(String.format("%.2f%%", entity.getExcellentPercent()));
        list.add(String.format("%.2f%%", entity.getQualifiedPercent()));
        list.add(String.format("%.2f%%", entity.getFailedPercent()));
        return list;
    }
}