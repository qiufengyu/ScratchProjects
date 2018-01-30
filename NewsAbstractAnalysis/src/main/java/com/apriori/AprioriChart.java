package com.apriori;

import com.analysis.WordWeight;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AprioriChart extends JFrame {

    final static Font defaultFont = new Font("微软雅黑", Font.PLAIN,14);
    public AprioriChart(String applicationTitle, String chartTitle, ArrayList<AprioriResult> arList) {
        super(applicationTitle);

        this.setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(1080 , 960) );

        JFreeChart barChart = makeBarChart(chartTitle, arList);

        ChartPanel barPanel = new ChartPanel(barChart);
        JPanel p1 = new JPanel();
        // p1.setSize(new java.awt.Dimension(720 , 475));
        p1.add(barPanel);
        this.add(p1, BorderLayout.CENTER);
        this.setVisible(true);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

    }

    private JFreeChart makeBarChart(String chartTitle, ArrayList<AprioriResult> arList) {
        JFreeChart barChart = ChartFactory.createBarChart(
                chartTitle,
                "共现词语",
                "支持度",
                createCategoryDataset(arList),
                PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = barChart.getCategoryPlot(); // 获取图表区域对象
        CategoryAxis domainAxis = plot.getDomainAxis(); // 水平底部列表
        domainAxis.setLabelFont(defaultFont); // 水平底部标题
        domainAxis.setTickLabelFont(defaultFont); // 垂直标题
        ValueAxis rangeAxis = plot.getRangeAxis(); // 获取柱状
        rangeAxis.setLabelFont(defaultFont);
        barChart.getLegend().setItemFont(defaultFont);
        barChart.getTitle().setFont(new Font("微软雅黑", Font.PLAIN,20)); // 设置标题字体

        try {
            ChartUtils.saveChartAsPNG(new File("apriori.png"), barChart, 720, 720);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return barChart;

    }

    private CategoryDataset createCategoryDataset(ArrayList<AprioriResult> arList) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(AprioriResult ar: arList) {
            dataset.addValue(ar.getSup(), ar.getItemSets(), ar.getItemSets());
        }
        return dataset;
    }
}
