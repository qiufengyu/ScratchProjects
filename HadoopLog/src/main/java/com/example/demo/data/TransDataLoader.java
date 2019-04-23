package com.example.demo.data;

import com.example.demo.entity.TransIP;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TransDataLoader {
    Map<String, Map<String, Integer>> map;
    Map<String, Integer> transCountMap;
    List<TransIP> transCountList;

    private final String transFile = "./output/trans/part-r-00000";

    public TransDataLoader() throws IOException {
        map = new HashMap<>();
        // 从结果文件中读取数据
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(transFile), StandardCharsets.UTF_8));
        String line;
        while(true) {
            line = br.readLine();
            if(line == null)
                break;
            String[] parts = line.split("\t");
            String[] keyparts = parts[0].split("#");
            // 网站：keyparts[0]，用户 IP：keyparts[1]，次数：parts[1]
            if(map.containsKey(keyparts[0])) {
                Map<String, Integer> innerMap = map.get(keyparts[0]);
                innerMap.put(keyparts[1], Integer.valueOf(parts[1]));
            }
            else {
                // 此时当前关键字没有数据，新建一个内层 map 后加入
                Map<String, Integer> innerMap = new HashMap<>();
                innerMap.put(keyparts[1], Integer.valueOf(parts[1]));
                map.put(keyparts[0], innerMap);
            }
        }
        System.out.println("trans 信息读取完毕！");
    }

    public Map<String, Map<String, Integer>> getMap() {
        return map;
    }

    public List<TransIP> getTransIPCountList(String trans) {
        List<TransIP> resList = new ArrayList<>();
        Map<String, Integer> m = map.get(trans);
        if(m != null) {
            for(Map.Entry<String, Integer> entry: m.entrySet()) {
                TransIP sip = new TransIP(trans, entry.getKey(), entry.getValue());
                resList.add(sip);
            }
            // 降序排序，用了 lambda 函数
            resList.sort((o1, o2) -> Integer.compare(o2.getCount(), o1.getCount()));
        }
        return resList;
    }

    public List<TransIP> getTransCountList() {
        transCountMap = new HashMap<>();
        transCountList = new ArrayList<>();
        for(Map.Entry<String, Map<String, Integer>> entry: map.entrySet()) {
            String k = entry.getKey();
            for(Map.Entry<String, Integer> entry2: entry.getValue().entrySet()) {
                if(transCountMap.containsKey(k)) {
                    int oldValue = transCountMap.get(k);
                    oldValue += entry2.getValue();
                    transCountMap.put(k, oldValue);
                }
                else {
                    transCountMap.put(k, entry2.getValue());
                }
            }
            TransIP sip = new TransIP(k, "", transCountMap.get(k));
            transCountList.add(sip);
        }
        transCountList.sort(new Comparator<TransIP>() {
            // 降序排序
            public int compare(TransIP o1,
                               TransIP o2) {
                return -Integer.compare(o1.getCount(), o2.getCount());
            }
        });
        return transCountList;
    }
}
