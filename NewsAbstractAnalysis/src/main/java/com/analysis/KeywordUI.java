package com.analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class KeywordUI extends JFrame {

    final static Font defaultFont = new Font("微软雅黑", Font.PLAIN,14);


    public KeywordUI(String applicationTitle, String chartTitle, ArrayList<WordWeight> wwList) {
        super(applicationTitle);

        this.setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(1080 , 960) );

        JFreeChart barChart = makeBarChart(chartTitle, wwList);
        JFreeChart pieChart = makePieChart(chartTitle, wwList);

        ChartPanel barPanel = new ChartPanel(barChart);
        ChartPanel piePanel = new ChartPanel(pieChart);
        JPanel p1 = new JPanel();
        // p1.setSize(new java.awt.Dimension(720 , 475));
        JPanel p2 = new JPanel();
        // p2.setSize(new java.awt.Dimension(720 , 475));
        p1.add(barPanel);
        p2.add(piePanel);
        this.add(p1, BorderLayout.NORTH);
        this.add(p2, BorderLayout.SOUTH);
        this.setVisible(true);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

    }

    private JFreeChart makePieChart(String chartTitle, ArrayList<WordWeight> wwList) {
        JFreeChart pieChart = ChartFactory.createPieChart(
                "",
                createPieDataset(wwList),          // data
                true,             // include legend
                true,
                false);

        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setLabelFont(defaultFont);
        pieChart.getLegend().setItemFont(defaultFont);

        try {
            ChartUtils.saveChartAsPNG(new File("pie.png"), pieChart, 720, 720);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pieChart;

    }



    private JFreeChart makeBarChart(String chartTitle, ArrayList<WordWeight> wwList) {
        JFreeChart barChart = ChartFactory.createBarChart(
                chartTitle,
                "词语",
                "重要性",
                createCategoryDataset(wwList),
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
            ChartUtils.saveChartAsPNG(new File("bar.png"), barChart, 720, 720);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return barChart;

    }

    private CategoryDataset createCategoryDataset(ArrayList<WordWeight> wwList) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        double sum = 0.0;
        for(WordWeight ww: wwList) {
            sum += ww.getWeight();
        }
        for(WordWeight ww: wwList) {
            dataset.addValue(ww.getWeight()/sum, ww.getWord(), ww.getWord());
        }
        return dataset;
    }

    private PieDataset createPieDataset(ArrayList<WordWeight> wwList) {
        DefaultPieDataset dataset = new DefaultPieDataset( );
        for(WordWeight ww: wwList) {
            dataset.setValue(ww.getWord(), ww.getWeight()*100.0);
        }
        return dataset;
    }

}