import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 具体提取病历中的四种实体
 * 症状、药品、疾病、检查
 */
public class NER {

    private List<Set<String>> nerSetList;

    private List<Set<String>> resultSetList;



    public NER() {
        nerSetList = new ArrayList<Set<String>>();
        resultSetList = new ArrayList<Set<String>>();
        for(int i = 0; i<5; i++) {
            Set<String> nerSet = new HashSet<String>();
            nerSetList.add(nerSet);
            Set<String> resultSet = new HashSet<String>();
            resultSetList.add(resultSet);
        }
        // 加载字典
        try {
            loadNER("0NER", 0);
            loadNER("1NER", 1);
            loadNER("2NER", 2);
            loadNER("3NER", 3);
            loadNER("4NER", 4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 抽取其中的症状
     * 1. 模板格式：单个不及物动词(vi), 名词(n)+动词(vi), 单形容词(a)，名词(n)+形容词(a)
     * 2. 字典匹配，查找0NER中的词
     * @param sent
     */
    public void extract0(String sent) throws IOException {
        String[] tokens = sent.split(" ");
        int len = tokens.length;
        String[] word = new String[len];
        String[] tag = new String[len];
        // 根据词性切分好词+词性
        for(int i = 0; i<len; i++) {
            String[] parts = tokens[i].split("/");
            word[i] = parts[0];
            tag[i] = parts[parts.length-1];
        }
        // 1. 不及物动词, 或者其前有名词
        for(int j = 0; j<len; j++) {
            if(tag[j].equals("vi")) {
                int k = j;
                // 从第 k+1 到第 j 处为一个症状
                for(k = j-1; k>=0; k--) {
                    if(!tag[k].startsWith("n")) {
                        break;
                    }
                }
                StringBuilder out = new StringBuilder();
                for(int l = k+1; l<=j; l++)
                    out.append(word[l]);
                resultSetList.get(0).add(out.toString());
            }
        }
        // 2. 名词 + 形容词
        for(int j = 1; j<len; j++) {
            if(tag[j].startsWith("a")) {
                int k = j-1;
                for(k = j-1; k>=0; k--) {
                    if(!tag[k].startsWith("n"))
                        break;
                }
                // 证明存在名词+形容词的症状实体
                if( k != j-1) {
                    StringBuilder out = new StringBuilder();
                    for(int l = k+1; l<=j; l++) {
                        out.append(word[l]);
                    }
                    resultSetList.get(0).add(out.toString());
                }
            }
        }
        // 3. 症状字典中含有该词
        for(int j = 0; j<len; j++) {
            if(nerSetList.get(0).contains(word[j])) {
                resultSetList.get(0).add(word[j]);
            }
        }
        // 4. 过滤一些不太可能是症状的实体：根据经验一般适中长度，为2-6个字符为佳
        HashSet<String> temp = new HashSet<>();
        temp.addAll(resultSetList.get(0));
        for(String y: temp) {
            if(y.length() < 2 || y.length() > 6) {
                resultSetList.get(0).remove(y);
            }
        }
    }


    /**
     * 抽取其中的药品
     * 1. 模板类
     * 动词v（服用、给予）+名词n+标点（w）
     * 标点（w）+若干词+数词（m）+标点（w）
     * 若干名词+针/片/素/剂/药/囊/林/唑
     * 2. 查找药品字典
     * @param sent
     */
    public void extract1(String sent) throws IOException {
        String[] tokens = sent.split(" ");
        int len = tokens.length;
        String[] word = new String[len];
        String[] tag = new String[len];
        // 根据词性切分好词+词性
        for(int i = 0; i<len; i++) {
            String[] parts = tokens[i].split("/");
            word[i] = parts[0];
            tag[i] = parts[parts.length-1];
        }
        // 1. 服用（用），给
        for(int j = 0; j<len; j++) {
            if(word[j].contains("用") || word[j].contains("给")) {
                int k = j+1;
                for(; k<len; k++) {
                    if(!tag[k].startsWith("n"))
                        break;
                }
                // 从j+1到k-1位置可能为一种药品
                if(j+1 != k) {
                    StringBuilder out = new StringBuilder();
                    for(int l = j+1; l<k; l++) {
                        out.append(word[l]);
                    }
                    resultSetList.get(1).add(out.toString());
                }
            }
        }
        // 2. 带有药品实体特色的后缀：针/片/素/剂/药/囊/林/唑
        for(int j = 0; j<len; j++) {
            if(word[j].contains("针") || word[j].contains("片") || word[j].contains("素") || word[j].contains("剂") || word[j].contains("囊") || word[j].contains("林") || word[j].contains("唑")) {
                // 从此处往前定位至第一个名词、形容词
                int s = j-1;
                int e = j+1;
                for(; s >= 0; s--) {
                    if( !(tag[s].startsWith("n") || tag[s].startsWith("a") || tag[s].startsWith("d") || tag[s].startsWith("b")) )
                        break;
                }
                for(; e < len; e++) {
                    if( !(tag[e].startsWith("n") || tag[e].startsWith("a") || tag[e].startsWith("d") || tag[e].startsWith("b")) )
                        break;
                }

                // 从s+1到e-1位置可能为一种药品
                if(s+1 != e-1) {
                    StringBuilder out = new StringBuilder();
                    for(int l = s+1; l<e; l++) {
                        out.append(word[l]);
                    }
                    resultSetList.get(1).add(out.toString());
                }
            }
        }
        // 3. 带有数词
        for(int j = 0; j<len; j++) {
            if(tag[j].equals("m")) {
                // 从此处往前定位至标点符号
                int s = j-1;
                for(; s >= 0; s--) {
                    if(tag[s].startsWith("w"))
                        break;
                }
                // 从s+1到e-1位置可能为一种药品，结合了药品命名规则的词性，筛选
                if(s+1 != j) {
                    int k = s+1;
                    for(; k<=j; k++) {
                        if(!(tag[k].startsWith("n") || tag[k].startsWith("a") || tag[k].startsWith("d") || tag[k].startsWith("b") || tag[k].startsWith("v") ) || tag[k].startsWith("q"))
                            break;
                    }
                    if(k == j) {
                        StringBuilder out = new StringBuilder();
                        for (int l = s + 1; l < j; l++) {
                            out.append(word[l]);
                        }
                        resultSetList.get(1).add(out.toString());
                    }
                }
            }
        }
        // 4. 查找词典中的药品名
        for(int j = 0; j<len; j++) {
            if(nerSetList.get(1).contains(word[j])) {
                resultSetList.get(1).add(word[j]);
            }
        }
        // 5. 过滤一些不太可能是药品的实体：根据经验一般药品名称都较长，这里设置为大于等于3个字符
        HashSet<String> temp = new HashSet<>();
        temp.addAll(resultSetList.get(1));
        for(String y: temp) {
            if(y.length() <= 2) {
                resultSetList.get(1).remove(y);
            }
        }
    }


    /**
     * 抽取疾病实体
     * 1. 模板：带有标志性的后缀：炎/病/癌/瘤/喘/压/症/征/气 等
     * 2. 字典
     * @param sent
     * @throws IOException
     */
    public void extract2(String sent) throws IOException {
        String[] tokens = sent.split(" ");
        int len = tokens.length;
        String[] word = new String[len];
        String[] tag = new String[len];
        // 根据词性切分好词+词性
        for(int i = 0; i<len; i++) {
            String[] parts = tokens[i].split("/");
            word[i] = parts[0];
            tag[i] = parts[parts.length-1];
        }
        // 1. 特征模板匹配
        for(int j = 0; j<len; j++) {
            if(word[j].endsWith("炎") || word[j].endsWith("病") || word[j].endsWith("癌") || word[j].endsWith("瘤") || word[j].contains("喘")
                    || word[j].contains("压") || word[j].endsWith("症") || word[j].endsWith("征") || word[j].contains("气")) {
                // 从此处往前、后分别定位至第一个标点
                int s = j-1;
                for(; s >= 0; s--) {
                    if(tag[s].startsWith("w") || tag[s].startsWith("m") || tag[s].startsWith("c") || tag[s].startsWith("vs") || word[s].contains("为"))
                        break;
                }
                // 从s+1到j位置可能为一种疾病
                if(s+1 != j) {
                    StringBuilder out = new StringBuilder();
                    for(int l = s+1; l<=j; l++) {
                        out.append(word[l]);
                    }
                    resultSetList.get(2).add(out.toString());
                }
            }
        }
        // 2. 查找词典中的疾病名称
        for(int j = 0; j<len; j++) {
            if(nerSetList.get(2).contains(word[j])) {
                resultSetList.get(2).add(word[j]);
            }
        }
        // 3. 过滤一些不太可能是疾病的实体：根据经验一般疾病名称都较长，这里设置为大于等于3个字符
        HashSet<String> temp = new HashSet<>();
        temp.addAll(resultSetList.get(2));
        for(String y: temp) {
            if(y.length() <= 2) {
                resultSetList.get(2).remove(y);
            }
        }
    }

    /**
     * 抽取检查实体：
     * 1. 模板：含有压/查/糖/镜/能/图/血/验
     * 2. 字典匹配
     * @param sent
     */
    public void extract3(String sent) throws IOException {
        String[] tokens = sent.split(" ");
        int len = tokens.length;
        String[] word = new String[len];
        String[] tag = new String[len];
        // 根据词性切分好词+词性
        for (int i = 0; i < len; i++) {
            String[] parts = tokens[i].split("/");
            word[i] = parts[0];
            tag[i] = parts[parts.length - 1];
        }
        // 1. 特征模板匹配
        for (int j = 0; j < len; j++) {
            if (word[j].contains("查") || word[j].contains("糖") || word[j].contains("镜") || word[j].contains("图") || word[j].contains("血")
                    || word[j].contains("压") || word[j].contains("验")) {
                int s = j-1;
                int e = j+1;
                for(; s >= 0; s--) {
                    if(tag[s].startsWith("w") || tag[s].startsWith("v") || tag[s].startsWith("c") || tag[s].startsWith("d"))
                        break;
                }
                for(; e < len; e++) {
                    if( tag[e].startsWith("w") || tag[e].startsWith("v") || tag[e].startsWith("c") || tag[e].startsWith("d"))
                        break;
                }
                // 从s+1到e-1位置可能为一种检查
                if(s+1 != e-1) {
                    StringBuilder out = new StringBuilder();
                    for(int l = s+1; l<e; l++) {
                        out.append(word[l]);
                    }
                    resultSetList.get(3).add(out.toString());
                }
            }
        }
        // 2. 查找词典中的检查名称
        for(int j = 0; j<len; j++) {
            if(nerSetList.get(3).contains(word[j])) {
                resultSetList.get(3).add(word[j]);
            }
        }
        // 3. 过滤一些不太可能是检查的实体：根据经验一般长度不会太短，这里设置为大于等于3字符
        HashSet<String> temp = new HashSet<>();
        temp.addAll(resultSetList.get(3));
        for(String y: temp) {
            if(y.length() <= 2) {
                resultSetList.get(3).remove(y);
            }
        }
    }

    // 4NER为一些辅助医疗相关实体，未经较好的整理，所以可以考虑是否并入词典
    // 这里面主要是一些症状、疾病相关部位的名词，不好分类，暂时不考虑这些
    private void loadNER(String file, int type) throws IOException {
        Set<String> nerSet = nerSetList.get(type);
        nerSet.clear();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        while(true) {
            line = br.readLine();
            if(line == null)
                break;
            nerSet.add(line.trim());
        }
        br.close();
        if(type == 4) {
            BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream("4NER"), "utf-8"));
            while (true) {
                line = br2.readLine();
                if (line == null)
                    break;
                nerSet.add(line.trim());
            }
            br2.close();
        }
    }

    public void extractAll(String inputFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
        String line;
        while(true) {
            line = br.readLine();
            if(line == null)
                break;
            line = line.trim();
            extract0(line);
            extract1(line);
            extract2(line);
            extract3(line);
        }
        // dumpAll();
    }

    public List getAll(String f) throws IOException {
        extractAll(f);
        return resultSetList;
    }

    private void dumpAll() {
        System.out.println("=====================================================================================================================");
        System.out.println("识别出的\"症状\"实体有：");
        for(String x: resultSetList.get(0)) {
            System.out.print(x+", ");
        }
        System.out.println("\n=====================================================================================================================");
        System.out.println("识别出的\"药品\"实体有：");
        for(String x: resultSetList.get(1)) {
            System.out.print(x+", ");
        }
        System.out.println("\n=====================================================================================================================");
        System.out.println("识别出的\"疾病\"实体有：");
        for(String x: resultSetList.get(2)) {
            System.out.print(x+", ");
        }
        System.out.println("\n=====================================================================================================================");
        System.out.println("识别出的\"检查\"实体有：");
        for(String x: resultSetList.get(3)) {
            System.out.print(x+", ");
        }
        System.out.println("\n=====================================================================================================================");
    }
}
