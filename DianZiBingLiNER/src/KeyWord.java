import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 读取数据库中的专有名词及其对应的名词类别
 * 作为特征使用
 * 抽取的名词实体分为：症状0，药品1，疾病2，检查3，其他实体4
 */
public class KeyWord {
    Pattern pattern;
    Pattern pattern2;
    public Map<Integer, Set<String>> dict;

    public KeyWord() {
        dict = new HashMap<Integer, Set<String>>();
        pattern = Pattern.compile("8086/(.*?)>.*8086/(.*?)>");
        pattern2 = Pattern.compile("8086/(.*?)>.*8086/(.*?)>.*8086/(.*?)>");
        for(int i = 0; i<5; ++i) {
            HashSet<String> a = new HashSet<String>();
            dict.put(i, a);
        }
    }

    /**
     *  综合所有数据库
     *  生成五种实体名词
     *  写入对应的文件中
     */
    public void dump() throws IOException {
        generate1();
        generate2();
        // generate3();
        for(Map.Entry<Integer, Set<String>> entry : dict.entrySet()) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(entry.getKey())+"NER")));
            for(String x: entry.getValue()) {
                bw.write(x+"\n");
                bw.flush();
            }
            bw.close();
        }
    }

    /** 从 category 文件中提取实体
     *  @throws IOException
     */
    private void generate1() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("category")));
        String line;
        while(true) {
            line = br.readLine();
            if(line == null)
                break;
            Matcher m = pattern.matcher(line);
            String s1;
            String s2;
            if(m.find()) {
                s1 = m.group(1);
                s2 = m.group(2);
                String k = filter(s1);
                // System.out.println(s1+"\t"+s2);
                Set<String> s = new HashSet<String>();
                if(k.contains(",") || k.contains(".") || k.contains(";")) {
                    String[] kg = k.split("[,|.|;]");
                    for (String x : kg) {
                        if(x.length()>1)
                        s.add(x);
                    }
                }
                else {
                    if(k.length() >1)
                    s.add(k);
                }
                if(s2.contains("症")) {
                    Set<String> ss = dict.get(0);
                    ss.addAll(s);
                }
                else if(s2.contains("药")) {
                    Set<String> ss = dict.get(1);
                    ss.addAll(s);
                }
                else if(s2.contains("病")) {
                    Set<String> ss = dict.get(2);
                    ss.addAll(s);
                }
                else if(s2.contains("检")) {
                    Set<String> ss = dict.get(3);
                    ss.addAll(s);
                }
                else {
                    Set<String> ss = dict.get(4);
                    ss.addAll(s);
                }
            }
        }
        br.close();
    }

    private void generate2() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("relation")));
        String line;
        while(true) {
            line = br.readLine();
            if (line == null)
                break;
            Matcher m = pattern2.matcher(line);
            String s1;
            Set<String> set1 = new HashSet<String>();
            String s2;
            String s3;
            Set<String> set3 = new HashSet<String>();
            if(m.find()) {
                s1 = m.group(1);
                s1 = filter(s1);
                if(s1.contains(",") || s1.contains(".")) {
                    String[] s1groups = s1.split("[,|.|;]");
                    for(String x: s1groups)
                        if(x.length() > 1)
                            set1.add(x);
                }
                else {
                    if(s1.length() > 1)
                        set1.add(s1);
                }
                s2 = m.group(2);
                s3 = m.group(3);
                s3 = filter(s3);
                if(s3.contains(",") || s3.contains(".")) {
                    String[] s3groups = s3.split("[,|.|;]");
                    for(String x: s3groups)
                        if(x.length()>1)
                            set3.add(x);
                }
                else {
                    if(s3.length()>1)
                    set3.add(s3);
                }
                String[] s2group = s2.split("相关");
                if(s2group.length == 2) {
                    String k1 = s2group[0];
                    String k2 = s2group[1];
                    // 第一部分实体
                    if(k1.contains("症")) {
                        Set<String> ss = dict.get(0);
                        ss.addAll(set1);
                    }
                    else if(k1.contains("药")) {
                        Set<String> ss = dict.get(1);
                        ss.addAll(set1);
                    }
                    else if(k1.contains("病")) {
                        Set<String> ss = dict.get(2);
                        ss.addAll(set1);
                    }
                    else if(k1.contains("检")) {
                        Set<String> ss = dict.get(3);
                        ss.addAll(set1);
                    }
                    else {
                        Set<String> ss = dict.get(4);
                        ss.addAll(set1);
                    }
                    // 检查第二部分名词实体
                    if(k2.contains("症")) {
                        Set<String> ss = dict.get(0);
                        ss.addAll(set3);
                    }
                    else if(k2.contains("药")) {
                        Set<String> ss = dict.get(1);
                        ss.addAll(set3);
                    }
                    else if(k2.contains("病")) {
                        Set<String> ss = dict.get(2);
                        ss.addAll(set3);
                    }
                    else if(k2.contains("检")) {
                        Set<String> ss = dict.get(3);
                        ss.addAll(set3);
                    }
                    else {
                        Set<String> ss = dict.get(4);
                        ss.addAll(set1);
                    }
                }
            }
        }
    }

    /**
     * 过滤掉原本文本中的特殊字符，修正一些小错误等
     * @param input
     * @return
     */
    private String filter(String input) {
        String x = input.replace("等", "");
        x = x.replaceAll("\\(.*$", "");
        return x;
    }


}
