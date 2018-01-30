import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Set;

/**
 * Created by Godfray on 2017/5/5.
 */
public class UI extends JFrame implements Common {
    // 组件
    private JButton picOpenButton;
    private JButton picExtractButton;
    private JButton picAboutButton;
    private JButton picExitButton;
    private JButton openButton;
    private JButton extractButton;
    private JButton exitButton;
    private JLabel zzLabel;
    private JLabel ypLabel;
    private JLabel jbLabel;
    private JLabel jcLabel;
    private JTextArea textArea = new JTextArea();
    private JTextPane textPane = new JTextPane();
    private JTextArea resultArea = new JTextArea();
    // 命名实体功能
    NER ner;
    String currentFile;

    public UI() {
        ner = new NER();
        currentFile = "";
        // 整体采用 borderlayout 布局
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(2);
        setLayout(borderLayout);
        setTitle("电子病历命名实体识别");
        setSize(720, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 生成菜单选项和功能按键
        openButton = new JButton();
        extractButton = new JButton();
        exitButton = new JButton();
        JToolBar toolbar = new JToolBar();
        picExitButton = new JButton();
        picExtractButton = new JButton();
        picAboutButton = new JButton();
        picOpenButton = new JButton();
        JPanel p0 = new JPanel();
        p0.setLayout(new GridLayout(3,1));
        JPanel p01 = new JPanel();
        FlowLayout layout01 = new FlowLayout();
        layout01.setHgap(5);
        layout01.setVgap(5);
        p01.setLayout(layout01);
        openButton.setFont(fontc);
        extractButton.setFont(fontc);
        exitButton.setFont(fontc);
        // 工具栏图标和按钮的功能一致
        openButton.setText("打开文件");
        extractButton.setText("实体识别");
        exitButton.setText(" 退出 ");
        picOpenButton.setIcon(new ImageIcon("open.png"));
        picOpenButton.setPreferredSize(new Dimension(32, 32));
        picExtractButton.setIcon(new ImageIcon("run.png"));
        picAboutButton.setIcon(new ImageIcon("about.png"));
        picExitButton.setIcon(new ImageIcon("quit.png"));
        picOpenButton.setToolTipText("打开文件");
        picExtractButton.setToolTipText("识别命名实体");
        picAboutButton.setToolTipText("程序信息");
        picExitButton.setToolTipText("退出");
        toolbar.setPreferredSize(new Dimension(720, 40));
        toolbar.add(picOpenButton);
        toolbar.add(picExtractButton);
        toolbar.add(picAboutButton);
        toolbar.add(picExitButton);
        p0.add(toolbar, 0, 0);
        p01.add(openButton);
        p01.add(extractButton);
        p01.add(exitButton);
        p0.add(p01, 0, 1);
        zzLabel = new JLabel();
        ypLabel = new JLabel();
        jbLabel = new JLabel();
        jcLabel = new JLabel();
        zzLabel.setIcon(new ImageIcon("zz.png"));
        ypLabel.setIcon(new ImageIcon("yp.png"));
        jbLabel.setIcon(new ImageIcon("jb.png"));
        jcLabel.setIcon(new ImageIcon("jc.png"));
        JPanel p02 = new JPanel();
        FlowLayout layout02 = new FlowLayout();
        layout02.setHgap(15);
        layout02.setVgap(5);
        p02.setLayout(layout02);
        p02.add(zzLabel);
        p02.add(ypLabel);
        p02.add(jbLabel);
        p02.add(jcLabel);
        p0.add(p02, 0, 2);
        add(p0, BorderLayout.NORTH);

        // 显示病历文本
        JPanel p1 = new JPanel();
        GridLayout layout1 = new GridLayout(3, 1);
        layout1.setVgap(5);
        layout1.setHgap(5);
        p1.setLayout(layout1);
        textArea.setFont(fontc1);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        p1.add(new JScrollPane(textArea), 0, 0);
        textPane.setEditable(false);
        textPane.setContentType("text/html;charset=utf-8");
        textPane.putClientProperty("charset", "utf-8");
        p1.add(new JScrollPane(textPane), 0, 1);
        // 显示抽取结果
        resultArea.setFont(fontc1);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        p1.add(new JScrollPane(resultArea), 0, 2);
        add(p1, BorderLayout.CENTER);
        setVisible(true);

        // 监听器，响应用户操作
        picOpenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 先清空文本区域
                textArea.setText("");
                textPane.setText("");
                resultArea.setText("");
                // 选择病历文件
                JFileChooser jfc = new JFileChooser(".");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
                jfc.setFileFilter(filter);
                jfc.showDialog(new JLabel(), "打开");
                jfc.setVisible(true);
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                File f = jfc.getSelectedFile();
                try {
                    if(f.isFile() && f.getName().endsWith("txt")) {
                        currentFile = f.getName();
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
                        String line;
                        while(true) {
                            line = br.readLine();
                            if(line == null)
                                break;
                            textArea.append(originText(line) + "\n");
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(p1,
                                "请选择正确的文件！",
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 先清空文本区域
                textArea.setText("");
                textPane.setText("");
                resultArea.setText("");
                // 选择病历文件
                JFileChooser jfc = new JFileChooser(".");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
                jfc.setFileFilter(filter);
                jfc.showDialog(new JLabel(), "打开");
                jfc.setVisible(true);
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                File f = jfc.getSelectedFile();
                try {
                    if(f.isFile() && f.getName().endsWith("txt")) {
                        currentFile = f.getName();
                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
                        String line;
                        while(true) {
                            line = br.readLine();
                            if(line == null)
                                break;
                            textArea.append(originText(line) + "\n");
                        }
                        br.close();
                    }
                    else {
                        JOptionPane.showMessageDialog(p1,
                                "请选择正确的文件！",
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        // 命名实体识别
        picExtractButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    textPane.setText("");
                    resultArea.setText("");
                    try {
                        if (currentFile.endsWith("txt")) {
                            File f = new File(currentFile);
                            java.util.List<Set<String>> resultList = ner.getAll(currentFile);
                            Set<String> set0 = resultList.get(0); // 症状
                            Set<String> set1 = resultList.get(1); // 药品
                            Set<String> set2 = resultList.get(2); // 疾病
                            Set<String> set3 = resultList.get(3); // 检查
                            HTMLDocument doc = (HTMLDocument)textPane.getDocument();
                            HTMLEditorKit editorKit = (HTMLEditorKit)textPane.getEditorKit();
                            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
                            String line;
                            while (true) {
                                line = br.readLine();
                                if (line == null)
                                    break;
                                String newText = originText(line);
                                for(String y: set1) { // 标记药品的颜色，重叠最少
                                    newText = newText.replaceAll(y, "<span style=\"background:#FF6A6A\">"+y+"</span>");
                                }
                                for(String z: set3) {
                                    newText = newText.replaceAll(z, "<span style=\"background:#BDF7C8\">"+z+"</span>");
                                }
                                for(String z: set2) {
                                    newText = newText.replaceAll(z, "<span style=\"background:#DDBDF7\">"+z+"</span>");
                                }

                                for(String x: set0) { // 标记症状的颜色，因为与其他部分重叠较多，所以在最后显示
                                    newText = newText.replaceAll(x, "<span style=\"background:#87CEFA\">"+x+"</span>");
                                }
                                editorKit.insertHTML(doc, doc.getLength(), newText, 0, 0, null);
                            }
                            br.close();
                            resultArea.append("====================================================================\n");
                            resultArea.append("识别出的\"症状\"实体有：\n");
                            for(String x: resultList.get(0)) {
                                resultArea.append(x+", ");
                            }
                            resultArea.append("\n====================================================================\n");
                            resultArea.append("识别出的\"药品\"实体有：\n");
                            for(String x: resultList.get(1)) {
                                resultArea.append(x+", ");
                            }
                            resultArea.append("\n====================================================================\n");
                            resultArea.append("识别出的\"疾病\"实体有：");
                            for(String x: resultList.get(2)) {
                                resultArea.append(x+", ");
                            }
                            resultArea.append("\n====================================================================\n");
                            resultArea.append("识别出的\"检查\"实体有：");
                            for(String x: resultList.get(3)) {
                                resultArea.append(x+", ");
                            }
                            resultArea.append("\n====================================================================\n");
                        } else {
                            JOptionPane.showMessageDialog(p1,
                                    "请选择正确的文件！",
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        );
        extractButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    textPane.setText("");
                    resultArea.setText("");
                    try {
                        if (currentFile.endsWith("txt")) {
                            File f = new File(currentFile);
                            java.util.List<Set<String>> resultList = ner.getAll(currentFile);
                            Set<String> set0 = resultList.get(0); // 症状
                            Set<String> set1 = resultList.get(1); // 药品
                            Set<String> set2 = resultList.get(2); // 疾病
                            Set<String> set3 = resultList.get(3); // 检查
                            HTMLDocument doc = (HTMLDocument)textPane.getDocument();
                            HTMLEditorKit editorKit = (HTMLEditorKit)textPane.getEditorKit();
                            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
                            String line;
                            while (true) {
                                line = br.readLine();
                                if (line == null)
                                    break;
                                String newText = originText(line);
                                for(String y: set1) { // 标记药品的颜色，重叠最少
                                    newText = newText.replaceAll(y, "<span style=\"background:#FF6A6A\">"+y+"</span>");
                                }
                                for(String z: set3) {
                                    newText = newText.replaceAll(z, "<span style=\"background:#BDF7C8\">"+z+"</span>");
                                }
                                for(String z: set2) {
                                    newText = newText.replaceAll(z, "<span style=\"background:#DDBDF7\">"+z+"</span>");
                                }

                                for(String x: set0) { // 标记症状的颜色，因为与其他部分重叠较多，所以在最后显示
                                    newText = newText.replaceAll(x, "<span style=\"background:#87CEFA\">"+x+"</span>");
                                }
                                editorKit.insertHTML(doc, doc.getLength(), newText, 0, 0, null);
                            }
                            br.close();
                            resultArea.append("====================================================================\n");
                            resultArea.append("识别出的\"症状\"实体有：\n");
                            for(String x: resultList.get(0)) {
                                resultArea.append(x+", ");
                            }
                            resultArea.append("\n====================================================================\n");
                            resultArea.append("识别出的\"药品\"实体有：\n");
                            for(String x: resultList.get(1)) {
                                resultArea.append(x+", ");
                            }
                            resultArea.append("\n====================================================================\n");
                            resultArea.append("识别出的\"疾病\"实体有：");
                            for(String x: resultList.get(2)) {
                                resultArea.append(x+", ");
                            }
                            resultArea.append("\n====================================================================\n");
                            resultArea.append("识别出的\"检查\"实体有：");
                            for(String x: resultList.get(3)) {
                                resultArea.append(x+", ");
                            }
                            resultArea.append("\n====================================================================\n");
                        } else {
                            JOptionPane.showMessageDialog(p1,
                                    "请选择正确的文件！",
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        );

        // 显示信息
        picAboutButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(p1,
                            "电子病历命名实体识别\n版本：1.0.0 ® xxx",
                            "关于",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        );

        // 退出
        picExitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(2);
                }
            }
        );
        exitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                        System.exit(2);
                }
            }
        );


    }

    public void run() {

    }

    private String originText(String t) {
        StringBuilder sb = new StringBuilder();
        String[] parts = t.split(" ");
        for(String x: parts) {
            String[] p = x.split("/");
            if(p.length == 2) {
                sb.append(p[0]);
            }
            else {
                sb.append("/");
            }
        }
        return sb.toString();

    }



}
