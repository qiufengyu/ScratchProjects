package com.segment;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class SegUI {

    // Model
    PLM plm = new PLM();
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JLabel label4;
    private JLabel label5;
    private JLabel label6;

    private JTextField textField1; // trainingFile
    private JTextField textField2; // dictionaryFile
    private JTextField textField3; // testFile
    private JTextField textField4; // goldFile
    private JTextField textField5; // results.txt
    private JTextField textField6; // modelFile

    private JButton chooseButton1;
    private JButton chooseButton2;
    private JButton chooseButton3;
    private JButton chooseButton4;
    private JButton chooseButton5;
    private JButton chooseButton6;

    private JButton trainButton;
    private JButton testButton;
    private JButton emptyButton;
    private JTabbedPane tabbedPane1;
    private JTabbedPane tabbedPane2;
    private JPanel fileTestPanel;
    private JTextArea textArea1;
    private JButton singleSegButton;
    private JButton singleClearButton;
    private JTabbedPane tabbedPane3;
    private JTextArea textArea2;
    private JButton exitButton;
    private JPanel mainPanel;
    private JLabel infoLabel;

    public SegUI() {
        // 清空各种文件路径
        emptyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textField1.setText("");
                textField2.setText("");
                textField3.setText("");
                textField4.setText("");
                textField5.setText("");
                textField6.setText("");
            }
        });
        chooseButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser("./");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setDialogTitle("选择训练文件");
                jfc.showDialog(new JLabel(), "确认");
                jfc.setVisible(true);
                File f = jfc.getSelectedFile();
                String absolutePath = f.getAbsolutePath();
                textField1.setText(absolutePath);
            }
        });
        chooseButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser("./");
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("选择字典文件路径");
                jfc.showDialog(new JLabel(), "确认");
                jfc.setVisible(true);
                File f = jfc.getSelectedFile();
                String absolutePath = f.getAbsolutePath();
                textField2.setText(absolutePath);
            }
        });
        chooseButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser("./");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setDialogTitle("选择测试文件");
                jfc.showDialog(new JLabel(), "确认");
                jfc.setVisible(true);
                File f = jfc.getSelectedFile();
                String absolutePath = f.getAbsolutePath();
                textField3.setText(absolutePath);
            }
        });
        chooseButton4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser("./");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setDialogTitle("选择标准测试文件");
                jfc.showDialog(new JLabel(), "确认");
                jfc.setVisible(true);
                File f = jfc.getSelectedFile();
                String absolutePath = f.getAbsolutePath();
                textField4.setText(absolutePath);
            }
        });
        chooseButton5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser("./");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setDialogTitle("选择分词结果文件");
                jfc.showDialog(new JLabel(), "确认");
                jfc.setVisible(true);
                File f = jfc.getSelectedFile();
                String absolutePath = f.getAbsolutePath();
                textField5.setText(absolutePath);
            }
        });
        chooseButton6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser("./");
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("选择模型文件路径");
                jfc.showDialog(new JLabel(), "确认");
                jfc.setVisible(true);
                File f = jfc.getSelectedFile();
                String absolutePath = f.getAbsolutePath();
                textField6.setText(absolutePath);
            }
        });

        trainButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                infoLabel.setText("训练中...");
                String trainingFile = textField1.getText().trim();
                String modelPath = textField6.getText().trim();
                if (trainingFile.length() > 1) {
                    plm.setTrainingFileString(trainingFile);
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "请指定训练文件！否则将使用默认配置：\n" + plm.getTrainingFileString());
                }
                if (modelPath.length() > 1) {
                    plm.setModelPath(modelPath);
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "请指定模型存储位置！否则将使用默认配置：\n" + plm.getModelPath());
                }
                try {
                    plm.train();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                infoLabel.setText("欢迎使用！");

            }
        });
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                infoLabel.setText("正在分词...");
                try {
                    plm.loadModel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                String modelPath = textField6.getText().trim();
                String dictionaryPath = textField2.getText().trim();
                String testFile = textField3.getText().trim();
                String goldFile = textField4.getText().trim();
                String resultFile = textField5.getText().trim();
                if (modelPath.length() > 1) {
                    plm.setModelPath(modelPath);
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "请指定加载模型位置！否则将使用默认配置：\n" + plm.getModelPath());
                }
                if (dictionaryPath.length() > 1) {
                    plm.setDictionaryPath(dictionaryPath);
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "请指定词典文件位置！否则将使用默认配置：\n" + plm.getDictionaryPath());
                }
                if (testFile.length() > 1) {
                    plm.setTestFileString(testFile);
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "请指定测试文件！否则将使用默认配置：\n" + plm.getTestFileString());
                }
                if (goldFile.length() > 1) {
                    plm.setGoldFileString(goldFile);
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "请指定标准结果文件！否则将使用默认配置：\n" + plm.getGoldFileString());
                }
                if (resultFile.length() > 1) {
                    plm.setOutputFileString(resultFile);
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "请指定测试文件！否则将使用默认配置：\n" + plm.getOutputFileString());
                }
                double accuracy = 0.0;
                try {
                    plm.testFile();
                    accuracy = plm.evaluate();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(mainPanel,
                        "测试完毕！请查看文件：" + plm.getOutputFileString() +
                                "\n平均准确率：" + String.format("%.4f%%", accuracy * 100.0));

                infoLabel.setText("欢迎使用!");

            }
        });

        singleClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea1.setText("");
                textArea2.setText("");
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });
        singleSegButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String source = textArea1.getText().trim();

                if (source.length() > 1) {
                    try {
                        try {
                            plm.loadModel();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        String target = plm.testSentence(source);
                        textArea2.setText(target);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "请输入一个合法的中文句子！");
                }

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SegUI");
        frame.setContentPane(new SegUI().mainPanel);
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        Font mainPanelFont = UIManager.getFont("Label.font");
        if (mainPanelFont != null) mainPanel.setFont(mainPanelFont);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tabbedPane1 = new JTabbedPane();
        Font tabbedPane1Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, tabbedPane1.getFont());
        if (tabbedPane1Font != null) tabbedPane1.setFont(tabbedPane1Font);
        panel1.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("单句测试", panel2);
        textArea1 = new JTextArea();
        Font textArea1Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, textArea1.getFont());
        if (textArea1Font != null) textArea1.setFont(textArea1Font);
        panel2.add(textArea1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(350, 50), null, 0, false));
        singleSegButton = new JButton();
        Font singleSegButtonFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, singleSegButton.getFont());
        if (singleSegButtonFont != null) singleSegButton.setFont(singleSegButtonFont);
        singleSegButton.setText("分词");
        panel2.add(singleSegButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        singleClearButton = new JButton();
        Font singleClearButtonFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, singleClearButton.getFont());
        if (singleClearButtonFont != null) singleClearButton.setFont(singleClearButtonFont);
        singleClearButton.setText("清除");
        panel2.add(singleClearButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exitButton = new JButton();
        Font exitButtonFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, exitButton.getFont());
        if (exitButtonFont != null) exitButton.setFont(exitButtonFont);
        exitButton.setText("退出");
        panel2.add(exitButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tabbedPane2 = new JTabbedPane();
        Font tabbedPane2Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, tabbedPane2.getFont());
        if (tabbedPane2Font != null) tabbedPane2.setFont(tabbedPane2Font);
        mainPanel.add(tabbedPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        fileTestPanel = new JPanel();
        fileTestPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane2.addTab("文件测试", fileTestPanel);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        fileTestPanel.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(282, 46), null, 0, false));
        label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalAlignment(10);
        label1.setText("训练文件");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        Font textField1Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, textField1.getFont());
        if (textField1Font != null) textField1.setFont(textField1Font);
        panel3.add(textField1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(350, -1), null, 0, false));
        textField2 = new JTextField();
        Font textField2Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, textField2.getFont());
        if (textField2Font != null) textField2.setFont(textField2Font);
        panel3.add(textField2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(350, -1), null, 0, false));
        label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setHorizontalAlignment(10);
        label2.setText("自定义词典（路径）");
        panel3.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField3 = new JTextField();
        Font textField3Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, textField3.getFont());
        if (textField3Font != null) textField3.setFont(textField3Font);
        textField3.setText("");
        panel3.add(textField3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(350, -1), null, 0, false));
        label3 = new JLabel();
        label3.setEnabled(true);
        Font label3Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("测试文件");
        panel3.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField4 = new JTextField();
        Font textField4Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, textField4.getFont());
        if (textField4Font != null) textField4.setFont(textField4Font);
        textField4.setText("");
        panel3.add(textField4, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(350, -1), null, 0, false));
        label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("标准测试文件");
        panel3.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField5 = new JTextField();
        Font textField5Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, textField5.getFont());
        if (textField5Font != null) textField5.setFont(textField5Font);
        textField5.setText("");
        panel3.add(textField5, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(350, -1), null, 0, false));
        label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText("分词结果文件");
        panel3.add(label5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chooseButton1 = new JButton();
        Font chooseButton1Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, chooseButton1.getFont());
        if (chooseButton1Font != null) chooseButton1.setFont(chooseButton1Font);
        chooseButton1.setText("选择");
        panel3.add(chooseButton1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chooseButton2 = new JButton();
        Font chooseButton2Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, chooseButton2.getFont());
        if (chooseButton2Font != null) chooseButton2.setFont(chooseButton2Font);
        chooseButton2.setText("选择");
        panel3.add(chooseButton2, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chooseButton3 = new JButton();
        Font chooseButton3Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, chooseButton3.getFont());
        if (chooseButton3Font != null) chooseButton3.setFont(chooseButton3Font);
        chooseButton3.setText("选择");
        panel3.add(chooseButton3, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chooseButton4 = new JButton();
        Font chooseButton4Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, chooseButton4.getFont());
        if (chooseButton4Font != null) chooseButton4.setFont(chooseButton4Font);
        chooseButton4.setText("选择");
        panel3.add(chooseButton4, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chooseButton5 = new JButton();
        Font chooseButton5Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, chooseButton5.getFont());
        if (chooseButton5Font != null) chooseButton5.setFont(chooseButton5Font);
        chooseButton5.setText("选择");
        panel3.add(chooseButton5, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, label6.getFont());
        if (label6Font != null) label6.setFont(label6Font);
        label6.setText("模型文件（路径）");
        panel3.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField6 = new JTextField();
        Font textField6Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, textField6.getFont());
        if (textField6Font != null) textField6.setFont(textField6Font);
        textField6.setText("");
        panel3.add(textField6, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(150, -1), null, 0, false));
        chooseButton6 = new JButton();
        Font chooseButton6Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, chooseButton6.getFont());
        if (chooseButton6Font != null) chooseButton6.setFont(chooseButton6Font);
        chooseButton6.setText("选择");
        panel3.add(chooseButton6, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        fileTestPanel.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        trainButton = new JButton();
        Font trainButtonFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, trainButton.getFont());
        if (trainButtonFont != null) trainButton.setFont(trainButtonFont);
        trainButton.setText("训练");
        panel4.add(trainButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        testButton = new JButton();
        Font testButtonFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, testButton.getFont());
        if (testButtonFont != null) testButton.setFont(testButtonFont);
        testButton.setText("测试");
        panel4.add(testButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        emptyButton = new JButton();
        Font emptyButtonFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, emptyButton.getFont());
        if (emptyButtonFont != null) emptyButton.setFont(emptyButtonFont);
        emptyButton.setText("清空");
        panel4.add(emptyButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tabbedPane3 = new JTabbedPane();
        Font tabbedPane3Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, tabbedPane3.getFont());
        if (tabbedPane3Font != null) tabbedPane3.setFont(tabbedPane3Font);
        mainPanel.add(tabbedPane3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane3.addTab("分词结果", panel5);
        textArea2 = new JTextArea();
        Font textArea2Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, textArea2.getFont());
        if (textArea2Font != null) textArea2.setFont(textArea2Font);
        panel5.add(textArea2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(350, 50), null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(0, 0));
        mainPanel.add(panel6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        infoLabel = new JLabel();
        Font infoLabelFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 12, infoLabel.getFont());
        if (infoLabelFont != null) infoLabel.setFont(infoLabelFont);
        infoLabel.setText("欢迎使用！");
        panel6.add(infoLabel, BorderLayout.CENTER);
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
        return mainPanel;
    }
}
