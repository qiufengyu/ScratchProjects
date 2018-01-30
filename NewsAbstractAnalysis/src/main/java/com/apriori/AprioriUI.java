package com.apriori;

import com.analysis.NLPIRSeg;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AprioriUI {

    // 日志记录
    private static final Logger logger = LogManager.getLogger(AprioriUI.class);

    private JPanel root;
    private JLabel label1;
    private JTextField textField1;
    private JButton button1;
    private JLabel label2;
    private JButton button2;
    private JLabel label3;
    private JLabel label4;
    private JTextField textField2;
    private JLabel label5;
    private JTextField textField3;
    private JLabel label6;
    private JButton button3;
    private JTextArea textArea1;
    private JCheckBox checkBox;

    NLPIRSeg nlpirSeg;

    public AprioriUI() {

        nlpirSeg = new NLPIRSeg();

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser("./");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setDialogTitle("选择新闻文件");
                jfc.showDialog(new JLabel(), "确认");
                jfc.setVisible(true);
                File f = jfc.getSelectedFile();
                String absolutePath = f.getAbsolutePath();
                textField1.setText(absolutePath);
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = textField1.getText();
                File f = new File(fileName);
                File trueFile;
                if (f.exists()) {
                    trueFile = f;
                } else {
                    logger.info("使用默认文件分词！");
                    trueFile = new File("jsyw.txt");
                }
                fileName = trueFile.getName();
                int indexOfDot = fileName.indexOf('.');
                String resultFileName = "";
                if (checkBox.isSelected()) {
                    resultFileName = fileName.substring(0, indexOfDot) + "_seg_pos.txt";
                    try {
                        nlpirSeg.segmentFile(trueFile.getAbsolutePath(), resultFileName, 3);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    resultFileName = fileName.substring(0, indexOfDot) + "_seg.txt";
                    try {
                        nlpirSeg.segmentFile(trueFile.getAbsolutePath(), resultFileName, 0);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String minsup = textField2.getText();
                double minsupDouble = 0.007;
                try {
                    minsupDouble = Double.valueOf(minsup);
                    logger.info("最小支持度为 " + minsupDouble);
                } catch (NumberFormatException nfe) {
                    logger.warn("不支持的浮点数，采用默认配置 0.005");
                }
                // 获取一些基本信息，用来初始化 Apriori 算法
                String fileName = textField1.getText();
                File f = new File(fileName);
                File trueFile;
                if (f.exists()) {
                    trueFile = f;
                } else {
                    logger.info("使用默认分词文件 jsyw_seg.txt！");
                    trueFile = new File("jsyw.txt");
                }
                int indexOfDot = trueFile.getName().indexOf(".");
                String aprioriFileName = trueFile.getName().substring(0, indexOfDot) + "_seg.txt";
                File apf = new File(aprioriFileName);
                if (!apf.exists()) {
                    JOptionPane.showMessageDialog(root,
                            "没有分词文件结果，请先分词！\n");
                    return;
                } else {
                    try {
                        AprioriPrepare aprioriPrepare = new AprioriPrepare(aprioriFileName);
                        int num = aprioriPrepare.getWordVocabluary();
                        int lines = aprioriPrepare.getLines();
                        Apriori ap = new Apriori(num, lines, minsupDouble);
                        try {
                            ShowApriori sa = new ShowApriori();
                            ArrayList<String> al = sa.show();
                            for (String s : al) {
                                textArea1.append(s + "\n");
                            }
                            String k = textField3.getText();
                            try {
                                int kk = Integer.valueOf(k);
                                if (ap.getMaxK() < kk) {
                                    JOptionPane.showMessageDialog(root,
                                            "最大的频繁项集大小为" + ap.getMaxK() + "! 请重新指定，一般建议为 3！\n下面显示 " + ap.getMaxK() + "-频繁项集的结果！");
                                    ArrayList<AprioriResult> allList = sa.showk(ap.getMaxK());
                                    Collections.sort(allList);
                                    int maxItems = Math.min(16, allList.size());
                                    ArrayList<AprioriResult> temp = new ArrayList<>();
                                    for (int i = 0; i < maxItems; ++i) {
                                        temp.add(allList.get(i));
                                    }
                                    AprioriChart apChart = new AprioriChart("频繁项集挖掘", "常见共现词", temp);
                                    return;
                                } else {
                                    // 默认显示 16 个最高的频繁项集
                                    ArrayList<AprioriResult> allList = sa.showk(kk);
                                    Collections.sort(allList);
                                    ArrayList<AprioriResult> temp = new ArrayList<>();
                                    int maxItems = Math.min(16, allList.size());
                                    for (int i = 0; i < maxItems; ++i) {
                                        temp.add(allList.get(i));
                                    }
                                    AprioriChart apChart = new AprioriChart("频繁项集挖掘", "常见共现词", temp);
                                }
                            } catch (NumberFormatException nfe2) {
                                logger.warn("非整数输入，不显示统计图表");
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }


                }
            }
        });
    }


    public void show() {
        JFrame frame = new JFrame("新闻摘要文本分析");
        frame.setContentPane(root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        root = new JPanel();
        root.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        Font rootFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, root.getFont());
        if (rootFont != null) root.setFont(rootFont);
        root.setPreferredSize(new Dimension(560, 400));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText(" 待分词文件");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        Font textField1Font = this.$$$getFont$$$("Microsoft JhengHei UI", Font.PLAIN, 12, textField1.getFont());
        if (textField1Font != null) textField1.setFont(textField1Font);
        textField1.setText("");
        panel1.add(textField1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        button1 = new JButton();
        Font button1Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, button1.getFont());
        if (button1Font != null) button1.setFont(button1Font);
        button1.setText("选择");
        panel1.add(button1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText(" 分词选项");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button2 = new JButton();
        Font button2Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, button2.getFont());
        if (button2Font != null) button2.setFont(button2Font);
        button2.setText("分词");
        panel1.add(button2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBox = new JCheckBox();
        Font checkBoxFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, checkBox.getFont());
        if (checkBoxFont != null) checkBox.setFont(checkBoxFont);
        checkBox.setText("词性标注");
        panel1.add(checkBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 14, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText(" 频繁项集");
        panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText(" 最小支持度");
        panel2.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField2 = new JTextField();
        panel2.add(textField2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText(" 频繁项集选择");
        panel2.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField3 = new JTextField();
        Font textField3Font = this.$$$getFont$$$("Microsoft JhengHei UI", Font.ITALIC, 12, textField3.getFont());
        if (textField3Font != null) textField3.setFont(textField3Font);
        textField3.setText(" 选择频繁项集的规模，可以显示图表！");
        panel2.add(textField3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label6.getFont());
        if (label6Font != null) label6.setFont(label6Font);
        label6.setText(" 默认设置为0.0075 ");
        panel2.add(label6, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button3 = new JButton();
        Font button3Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, button3.getFont());
        if (button3Font != null) button3.setFont(button3Font);
        button3.setText("显示");
        panel2.add(button3, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        root.add(scrollPane1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 120), null, null, 0, false));
        textArea1 = new JTextArea();
        textArea1.setMargin(new Insets(5, 5, 0, 0));
        scrollPane1.setViewportView(textArea1);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }
}
