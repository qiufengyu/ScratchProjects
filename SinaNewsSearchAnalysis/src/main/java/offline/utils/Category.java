package offline.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Category {
    // 将类别标签从字符串转化为整数
    private static Map<String, Integer> category2Integer = new HashMap<String, Integer>();
    // 将整数转化为文字
    private static Map<Integer, String> integer2Category = new HashMap<Integer, String>();;

    static {
        category2Integer.put("国内", 0);
        category2Integer.put("国际", 1);
        category2Integer.put("社会", 2);
        category2Integer.put("体育", 3);
        category2Integer.put("娱乐", 4);
        category2Integer.put("军事", 5);
        category2Integer.put("科技", 6);
        category2Integer.put("财经", 7);
        category2Integer.put("股市", 8);
        category2Integer.put("美股", 9);
        integer2Category.put(0, "国内");
        integer2Category.put(1, "国际");
        integer2Category.put(2, "社会");
        integer2Category.put(3, "体育");
        integer2Category.put(4, "娱乐");
        integer2Category.put(5, "军事");
        integer2Category.put(6, "科技");
        integer2Category.put(7, "财经");
        integer2Category.put(8, "股市");
        integer2Category.put(9, "美股");
    }

    public static String int2Cate(int i) {
        if (i >=0 && i <=9)
            return integer2Category.get(i);
        else
            return "";
    }

    public static int cate2Int(String c) {
        if (category2Integer.get(c) != null)
            return category2Integer.get(c);
        else
            return -1;
    }

    public static Set<String> getCategories() {
        return category2Integer.keySet();
    }


}
