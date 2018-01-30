package com.apriori;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

public class Apriori {

    // 日志记录
    private static final Logger logger = LogManager.getLogger(Apriori.class);

    // 一些基本设置
    private int NUM; // 每一行有多少词
    private int LINES; // 测试记录
    private double MINSUP; // 最小置信值

    private int K = 0;

    Vector<Vector<String>> largeItemset = new Vector<Vector<String>>();
    Vector<CandidateElement> candi = new Vector<CandidateElement>();

    // 存储结果
    Vector<String> itemsetVector = new Vector<String>(); // 共现词的 id
    Vector<Double> numVector = new Vector<Double>(); // 共现的频率
    Map<Vector<String>, Double> resultMap = new TreeMap<Vector<String>, Double>();

    String fullItemset;

    class ItemsetNode {
        String itemset;
        int count;

        public ItemsetNode() {
            itemset = new String();
            count = 0;
        }

        public ItemsetNode(String s, int i) {
            itemset = new String(s);
            count = i;
        }

    }

    // 使用 哈希树 结构来进一步加速 Apriori 算法
    // 只需要知道原理就行，具体实现不用太在意
    // 这种优化的方式不懂也没关系，最基本的 Apriori 算法理解就行了
    class HashTreeNode {
        int nodeAttr; // 1-hashtable, 2-itemset list
        int nodeDepth; // level of the node means the length of the itemset
        Hashtable<String, HashTreeNode> hashTable;
        Vector<ItemsetNode> itemsetList;

        public HashTreeNode() {
            nodeAttr = 1;
            hashTable = new Hashtable<String, HashTreeNode>();
            itemsetList = new Vector<ItemsetNode>();
            nodeDepth = 0;
        }

        public HashTreeNode(int attr) {
            nodeAttr = attr;
            hashTable = new Hashtable<String, HashTreeNode>();
            itemsetList = new Vector<ItemsetNode>();
            nodeDepth = 0;
        }

    }

    class CandidateElement {
        HashTreeNode hashTreeNode;
        Vector candidateList;
    }

    public Apriori(int num, int lines, double minsup) throws IOException {

        NUM = num;
        LINES = lines;
        MINSUP = minsup;

        CandidateElement candiElem;
        K = 0;

        fullItemset = new String();
        fullItemset = fullItemset.concat("1");
        for(int i = 2; i<=NUM; i++) {
            fullItemset = fullItemset.concat(" ");
            fullItemset = fullItemset.concat(Integer.toString(i));
        }

        // 算法正式开始
        logger.info("算法开始...");
        while(true) {
            K++;
            logger.info("计算" + K +"频繁项集");
            candiElem = new CandidateElement();
            candiElem.candidateList = genCandidate(K);

            if(candiElem.candidateList.isEmpty()) {
                logger.warn(K + "频繁项集已不存在，即将退出...");
                break;
            }

            candiElem.hashTreeNode = null;
            candi.addElement(candiElem);

            ((CandidateElement)candi.get(K-1)).hashTreeNode = genCandidateHashTree(K);
            traverse(K);
            genFrequentItemset(K);
        }
        resultWrite();
        logger.info("Apriori 算法结束！");
    }

    private void resultWrite() throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("result.txt"),"UTF-8"));
        for(int aa = 0; aa<numVector.size(); aa++) {
            bw.write(itemsetVector.get(aa)+" "+String.format("%.6f",numVector.get(aa))+"\n");
            bw.flush();
        }
        bw.close();
    }

    public int getMaxK() {
        return K-1;
    }

    // 优化：从 k 频繁项集生成 k+1 频繁项集
    public Vector genCandidate(int k) {
        Vector<String> tempCandidateList = new Vector<String>();
        Vector<String> temp_v = new Vector<String>();
        int length;
        String candidate1 = new String();
        String candidate2 = new String();
        String newCandidate = new String();
        // 1 频繁项集
        if( 1 == k ) {
            for(int i = 1; i<=NUM; i++) {
                tempCandidateList.addElement(Integer.toString(i));
            }
        }
        // k >= 2
        else {
            temp_v = (Vector)largeItemset.get(k-2); //-2, not 1
            length = temp_v.size();
            for(int j = 0; j<length; j++) {
                // get 1 itemset element
                candidate1 = temp_v.get(j);
                // newCandidate = new String();
                for(int jj=j+1; jj<length; jj++) {
                    candidate2 = temp_v.get(jj);
                    newCandidate = new String();
                    // attention: if k == 2, no rule, just join
                    if( 2 == k) {
                        newCandidate = candidate1.concat(" ");
                        newCandidate = newCandidate.concat(candidate2);
                        tempCandidateList.addElement(newCandidate.trim());
                    }
                    else { //only first k-2 element same ->downward closure property!
                        String s1, s2;
                        boolean same = true;
                        for(int pos = 1; pos<k-1; pos++) {
                            s1 = getItemAt(candidate1, pos);
                            s2 = getItemAt(candidate2, pos);
                            if(s1.compareToIgnoreCase(s2) != 0) {
                                same = false;
                                break;
                            }
                            else {
                                newCandidate = newCandidate.concat(" ");
                                newCandidate = newCandidate.concat(s1);
                            }
                        }
                        // a legal C(k+1) element we've got
                        if(same) {
                            s1 = getItemAt(candidate1, k-1);
                            s2 = getItemAt(candidate2, k-1);
                            newCandidate = newCandidate.concat(" ");
                            newCandidate = newCandidate.concat(s1);
                            newCandidate = newCandidate.concat(" ");
                            newCandidate = newCandidate.concat(s2);
                            tempCandidateList.addElement(newCandidate.trim());
                        }
                    }
                }
            }
        }

        return tempCandidateList;
    }

    // generate hash tree for candidates，为候选生成哈希树索引
    public HashTreeNode genCandidateHashTree(int k) {

        HashTreeNode htn = new HashTreeNode();

        if( 1 == k ) {
            htn.nodeAttr = 2;
        }
        else {
            htn.nodeAttr = 1;
        }

        int length = ((CandidateElement)candi.get(k-1)).candidateList.size();
        for(int i = 1; i<=length; i++) {
            String candidate1 = new String();
            candidate1 = (String) ((CandidateElement)candi.get(k-1)).candidateList.get(i-1);
            genHash(1, htn, candidate1);
        }
        return htn;
    }

    // Recursively create node in hash tree for candidates
    private void genHash(int i, HashTreeNode htn, String s) {
        // i is the recursive depth
        int n = itemsetSize(s);
        if( i == n) {
            htn.nodeAttr = 2;
            htn.nodeDepth = n;
            ItemsetNode isn = new ItemsetNode(s, 0);
            if(htn.itemsetList == null) {
                htn.itemsetList = new Vector<ItemsetNode>();
            }
            htn.itemsetList.addElement(isn);
        }
        else {
            if(htn.hashTable==null) {
                htn.hashTable = new Hashtable<String, HashTreeNode>(1);
            }
            if(htn.hashTable.containsKey(getItemAt(s, i))) {
                htn = (HashTreeNode)htn.hashTable.get(getItemAt(s, i));
                genHash(i+1, htn, s);
            }
            else {
                HashTreeNode nhtn = new HashTreeNode();
                htn.hashTable.put(getItemAt(s, i), nhtn);
                if( i == n-1 ) {
                    nhtn.nodeAttr = 2;
                    genHash(i+1, nhtn, s);
                }
                else {
                    nhtn.nodeAttr = 1;
                    genHash(i+1, nhtn, s);
                }
            }
        }
    }


    // Find frequent intemset
    public void genFrequentItemset(int k) {
        Vector candList = new Vector();
        Vector<String> largeis = new Vector<String>(); //is short for itemset!
        HashTreeNode htn = new HashTreeNode();

        candList = ((CandidateElement)candi.get(k-1)).candidateList;
        htn = ((CandidateElement)candi.get(k-1)).hashTreeNode;
        getFrequentHash(0, htn, fullItemset, largeis);

        largeItemset.addElement(largeis);
    }

    // Recursively traverse the candidate hash tree
    // if count > MINSUP * num of lines
    public void getFrequentHash(int i, HashTreeNode htn, String s, Vector<String> lis) {
        Vector tempv = new Vector();
        if(htn.nodeAttr == 2) {
            tempv = htn.itemsetList;
            for(int j = 1; j<=tempv.size(); j++) {
                if (((ItemsetNode)tempv.get(j-1)).count >= MINSUP * LINES) {
                    String frequentString = ((ItemsetNode)tempv.get(j-1)).itemset;
                    lis.addElement(frequentString);
                    itemsetVector.addElement(frequentString);
                    numVector.addElement(((ItemsetNode)tempv.get(j-1)).count/(double)LINES);
                }
            }
        }
        else { // HashTable node
            if(htn.hashTable == null)
                return;
            for(int t = i+1; t<=NUM; t++) {
                if(htn.hashTable.containsKey(getItemAt(s, t))) {
                    getFrequentHash(t, (HashTreeNode)htn.hashTable.get((getItemAt(s, t))), s, lis);
                }
            }
        }
    }

    // read file and traverse the hash tree
    // count + 1 when find the itemset in all transactions
    public void traverse(int n) throws IOException {
        File file = new File("apriori.txt");
        String lines = new String();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
        lines = br.readLine(); //first line ignored
        int i = 0, j = 0;
        String transaction;
        HashTreeNode htn = new HashTreeNode();
        StringTokenizer st;
        String s0;

        htn=((CandidateElement)candi.get(n-1)).hashTreeNode;

        while(true) {
            transaction = new String();
            String line = br.readLine();
            if (line == null)
                break;
            st = new StringTokenizer(line.trim());
            j = 0;
            while(st.hasMoreTokens() && j<NUM) {
                j++;
                s0 = st.nextToken();
                i = Integer.valueOf(s0).intValue();
                if( i != 0) {
                    transaction = transaction.concat(" ");
                    transaction = transaction.concat(Integer.toString(j));
                }
            }
            transaction = transaction.trim();
            transactionHash(0, htn, transaction);
        }
    }

    // recursively traverse hash tree
    private void transactionHash(int i, HashTreeNode htn, String transaction) {
        Vector itemsetList = new Vector();
        int length;
        String temp;
        ItemsetNode tempNode = new ItemsetNode();
        StringTokenizer st;

        if(htn.nodeAttr == 2) {
            itemsetList = (Vector)htn.itemsetList;
            length = itemsetList.size();
            for(int j = 0; j<length; j++) {
                st = new StringTokenizer(transaction);
                tempNode = (ItemsetNode)itemsetList.get(j);
                temp = getItemAt(tempNode.itemset, htn.nodeDepth);
                while(st.hasMoreTokens()) {
                    if(st.nextToken().compareToIgnoreCase(temp) == 0) {
                        ((ItemsetNode)itemsetList.get(j)).count++;
                    }
                }
            }
            return;
        }
        else { //HashTable node
            for(int t = i+1; t<=itemsetSize(transaction); t++) {
                if(htn.hashTable.containsKey((getItemAt(transaction, t)))){
                    transactionHash(i, (HashTreeNode)htn.hashTable.get(getItemAt(transaction, t)), transaction);
                }
            }
        }
    }

    // get the index-th Item of an itemset
    private String getItemAt(String s, int index) {
        String temp = new String();
        StringTokenizer st = new StringTokenizer(s);

        if(index > st.countTokens()) {
            System.exit(-1);
        }

        for(int j = 1; j<=index; j++) {
            temp = st.nextToken();
        }

        return temp;
    }

    // get the size of a certain itemset
    public int itemsetSize(String s) {
        StringTokenizer st=new StringTokenizer(s);
        return st.countTokens();
    }

}