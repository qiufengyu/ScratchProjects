package com.example.hadoop.preprocess;

public class Utils {
    /*
    * 根据选择的数字，返回所选专业的文本内容
    * 具体对应如下：
    *   体育学院,1
        全校,2
        地理信息与旅游学院,3
        外国语学院,4
        教育科学学院,5
        数学与金融学院,6
        文学与传媒学院,7
        机械与汽车工程学院,8
        材料与化学工程学院,9
        生物与食品工程学院,10
        电子与电气工程学院,11
        经济与管理学院,12
        美术与设计学院,13
        音乐学院,14
    * */
    public static String int2Department(int i) {
        switch (i) {
            case 1:
                return new String("体育学院");
            case 3:
                return new String("地理信息与旅游学院");
            case 4:
                return new String("外国语学院");
            case 5:
                return new String("教育科学学院");
            case 6:
                return new String("数学与金融学院");
            case 7:
                return new String("文学与传媒学院");
            case 8:
                return new String("机械与汽车工程学院");
            case 9:
                return new String("材料与化学工程学院");
            case 10:
                return new String("生物与食品工程学院");
            case 11:
                return new String("电子与电气工程学院");
            case 12:
                return new String("经济与管理学院");
            case 13:
                return new String("美术与设计学院");
            case 14:
                return new String("音乐学院");
            default:
                return new String("全校");
        }
    }

    public static String int2Item(int i) {
        switch (i) {
            case 1:
                return new String("全部");
            case 2:
                return new String("考试人数");
            case 3:
                return new String("平均分");
            case 4:
                return new String(">=90 比例");
            case 5:
                return new String("80-89 比例");
            case 6:
                return new String("70-79 比例");
            case 7:
                return new String("60-69 比例");
            case 8:
                return new String("<60 比例");
            case 9:
                return new String("优秀比例");
            case 10:
                return new String("合格比例");
            case 11:
                return new String("不合格比例");
            default:
                return new String("全部");
        }
    }
}
