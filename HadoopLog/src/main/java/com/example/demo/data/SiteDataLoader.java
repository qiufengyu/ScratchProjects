package com.example.demo.data;

import com.example.demo.entity.SiteIP;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

// 抽象了两个数据加载模块，使 controller 中的代码更清晰
public class SiteDataLoader {
    Map<String, Map<String, Integer>> map;
    Map<String, Integer> siteCountMap;
    List<SiteIP> siteCountList;

    private final String siteFile = "./output/site/part-r-00000";

    public SiteDataLoader() throws IOException {
        map = new HashMap<>();
        // 从结果文件中读取数据
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(siteFile)));
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
        System.out.println("site 信息读取完毕！");
    }

    public Map<String, Map<String, Integer>> getMap() {
        return map;
    }

    public List<SiteIP> getSiteIPCountList(String site) {
        List<SiteIP> resList = new ArrayList<>();
        Map<String, Integer> m = map.get(site);
        if(m != null) {
            for(Map.Entry<String, Integer> entry: m.entrySet()) {
                SiteIP sip = new SiteIP(site, entry.getKey(), entry.getValue());
                resList.add(sip);
            }
            // 降序排序，用了 lambda 函数
            resList.sort((o1, o2) -> Integer.compare(o2.getCount(), o1.getCount()));
        }
        return resList;
    }

    public List<SiteIP> getSiteCountList() {
        siteCountMap = new HashMap<>();
        siteCountList = new ArrayList<>();
        for(Map.Entry<String, Map<String, Integer>> entry: map.entrySet()) {
            String k = entry.getKey();
            for(Map.Entry<String, Integer> entry2: entry.getValue().entrySet()) {
                if(siteCountMap.containsKey(k)) {
                    int oldValue = siteCountMap.get(k);
                    oldValue += entry2.getValue();
                    siteCountMap.put(k, oldValue);
                }
                else {
                    siteCountMap.put(k, entry2.getValue());
                }
            }
            SiteIP sip = new SiteIP(k, "", siteCountMap.get(k));
            siteCountList.add(sip);
        }
        siteCountList.sort(new Comparator<SiteIP>() {
            // 降序排序
            public int compare(SiteIP o1,
                               SiteIP o2) {
                return -Integer.compare(o1.getCount(), o2.getCount());
            }
        });
        return siteCountList;
    }
}
