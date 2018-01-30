import java.io.IOException;
import java.util.List;

/**
 * 主程序入口
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // 从数据库中生成命名实体词典
        /*
        KeyWord kw = new KeyWord();
        kw.dump();
        */

        UI ui = new UI();
        ui.run();

        /*
        NER ner = new NER();
        // 完整病历示例，第一个病历
        String inputFile = "test.txt";
        ner.extractAll(inputFile);
        */
        /**
         * 单种实体识别示例
         */
        // 识别症状: 输入句子->bosonnlp标注->命名实体识别
        // 1.支气管哮喘：是儿童最常见的慢性呼吸道疾病，其症状以咳嗽、喘息为主，阵发性发作，
        // 以夜间和清晨为重，发作前可有流涕、打喷嚏和胸闷，发作时呼吸困难，呼气相延长伴有喘鸣音，
        // 体格检查可见桶状胸、三凹征、肺部满布哮鸣音。结合肺功能检查及胸部X线检查可确诊。
        // 2.根据该患儿“咳嗽五天伴气喘一天”主诉，查体：略鼻扇，三凹征、肺部满布哮鸣音。
        // 结合肺功能检查及胸部X线检查可确诊。2.根据该患儿“咳嗽五天伴气喘一天”主诉，查体：略鼻扇，
        // 三凹征（±）,咽轻度充血，双扁桃体I度肿大，双肺呼吸音粗，呼气相延长，可闻及细湿罗音及哮鸣音，
        // 可诊断为支气管哮喘。该病应与支气管肺炎、毛细支气管炎相鉴别。
        // ner.extract0("1/m ./wj 支气管/n 哮喘/n ：/wm 是/vshi 儿童/n 最/d 常见/a 的/ude 慢性/b 呼吸道/nl 疾病/n ，/wd 其/r 症状/n 以/p 咳嗽/vi 、/wn 喘息/vi 为主/vi ，/wd 阵发性/n 发作/n ，/wd 以/p 夜间/t 和/c 清晨/t 为重/vi ，/wd 发作/vi 前/f 可/v 有/vyou 流涕/n 、/wn 打喷嚏/vi 和/c 胸闷/n ，/wd 发作/vi 时/n 呼吸/v 困难/an ，/wd 呼/v 气/n 相/d 延长/v 伴/v 有/vyou 喘鸣音/n ，/wd 体格/n 检查/n 可见/v 桶/n 状/n 胸/n 、/wn 三凹征/n 、/wn 肺部/n 满布/v 哮鸣音/n 。/wj 结合/v 肺/n 功能/n 检查/n 及/c 胸部/n X/nx 线/n 检查/v 可/v 确诊/v 。/wj 2/m ./wj 根据/p 该/r 患儿/n “/wyz 咳嗽/vi 五/m 天/q 伴/v 气喘/n 一/m 天/q ”/wyy 主诉/n ，/wd 查体/v ：/wm 略/d 鼻扇/n ，/wd 三凹征/n 、/wn 肺部/n 满布/v 哮鸣音/n 。/wj 结合/v 肺/n 功能/n 检查/n 及/c 胸部/n X/nx 线/n 检查/v 可/v 确诊/v 。/wj 2/m ./wj 根据/p 该/r 患儿/n “/wyz 咳嗽/vi 五/m 天/q 伴/v 气喘/n 一/m 天/q ”/wyy 主诉/n ，/wd 查体/v ：/wm 略/v 鼻扇/n ，/wd 三凹征/n （/wkz ±/w ）/wky ,/wd 咽/v 轻度/d 充血/vi ，/wd 双/m 扁桃体/n I/nx 度/q 肿大/vi ，/wd 双/m 肺/n 呼吸/v 音/n 粗/a ，/wd 呼气/n 相/d 延长/v ，/wd 可/v 闻/v 及/c 细/a 湿罗音/n 及/c 哮鸣音/n ，/wd 可/v 诊断/v 为/v 支气管/n 哮喘/n 。/wj 该病/r 应/v 与/p 支气管/n 肺炎/n 、/wn 毛细支/n 气管炎/n 相/d 鉴别/v 。/wj 西医/n 鉴别/n 诊断/n ：/wm");

        // 识别药品:
        // 1.抗感染：头孢美唑钠0.5g次，2次日；利巴韦林针0.1克，1次日，静点；.止咳化痰：盐酸氨溴索7.5mg次，1次日;
        // 给予青霉素钠抗感染1.抗感染：头孢呋辛钠0.85次，2次日静点；更昔洛韦80mg次，1次日静点；
        // 对金葡菌、大肠埃希菌、肺炎杆菌、奇异变形杆菌有良好作用；对耐头孢菌素类及耐青霉素类抗生素的普通变形杆菌、
        // 摩氏摩根菌、普罗威登斯菌属有很强作用；对消化链球菌、拟杆菌、普雷沃菌属等厌氧菌有很强抗菌作用。
        // ner.extract1("1/m ./wj 抗/v 感染/v ：/wm 头孢/n 美唑钠/nz 0.5/m g/nx //w 次/q ，/wd 2/m 次/q //w 日/q ；/wf 利巴韦林针/nrf 0.1/m 克/q ，/wd 1/m 次/q //w 日/q ，/wd 静/a 点/q ；/wf" + "2/m ./wj 止咳/n 化痰/vi ：/wm 盐酸氨溴/n 索/v 7.5/m mg/q //w 次/q ，/wd 1/m 次/q //w 日/q ;/wf 给予/v 青霉素/n 钠/n 抗/v 感染/n 1/m ./wj 抗/v 感染/v ：/wm 头孢/n 呋辛钠/nz 0.85/m //w 次/q ，/wd 2/m 次/q //w 日/q 静/a 点/n ；/wf 更/d 昔洛韦/nz 80/m mg/q //w 次/q ，/wd 1/m 次/q //w 日/q 静/a 点/n ；/wf 对/p 金葡菌/n 、/wn 大肠/n 埃希菌/n 、/wn 肺炎/n 杆菌/n 、/wn 奇异/a 变形/n 杆/n 菌/n 有/vyou 良好/a 作用/n ；/wf 对/p 耐/v 头孢/n 菌素/n 类/n 及/c 耐/v 青霉素/n 类/n 抗生素/n 的/ude 普通/a 变形/n 杆菌/nr 、/wn 摩氏/nr 摩根菌/nr 、/wn 普罗威登斯菌/nrf 属/v 有/vyou 很/d 强/a 作用/n ；/wf 对/p 消化/v 链球/n 菌/n 、/wn 拟杆菌/n 、/wn 普雷沃菌属/nrf 等/udeng 厌氧菌/n 有/vyou 很/d 强/a 抗/v 菌/n 作用/n 。/wj");

        // 识别疾病
        // 3.中枢神经系统感染：临床特点为发热，头痛，呕吐，1.支气管肺炎是小儿时期最常见的肺炎，
        // 2岁以内儿童多发，新生儿肺炎1.原发性肺结核：该病患儿有午后低热，乏力、盗汗、
        // 2.乳汁吸入性肺炎：乳汁吞咽时吸入呼吸道或在咽部排空时间延长，1．消化性溃疡：指胃和十二直肠的慢性溃疡，
        // ner.extract2("3/m ./wj 中枢神经/nl 系统/n 感染/v ：/wm 临床/n 特点/n 为/v 发热/vi ，/wd 头痛/a ，/wd 呕吐/vi ， 1/m ./wj 支/q 气管/n 肺炎/n 是/vshi 小儿/n 时期/n 最/d 常见/a 的/ude 肺炎/n ，/wd 2/m 岁/q 以内/f 儿童/n 多/ad 发/v ，/wd 新生儿/n 肺炎/n 1/m ./wj 原发性/b 肺结核/n ：/wm 该病/r 患儿/n 有/vyou 午后/t 低/a 热/a ，/wd 乏力/a 、/wn 盗/v 汗/n 、/wn 2/m ./wj 乳汁/n 吸入/v 性/k 肺炎/n ：/wm 乳汁/n 吞咽/v 时/n 吸入/v 呼吸道/nl 或/c 在/p 咽部排空/n 时间/n 延长/v ， 1/m ．/wj 消化/v 性/k 溃疡/n ：/wm 指/v 胃/n 和/c 十二/m 直肠/n 的/ude 慢性/b 溃疡/n ，/wd");

        // 识别检查
        // 母孕期患病及治疗情况：定期做产前检查，孕期体健。
        // 测量餐后血糖为6.7mmol。昨日经皮胆红素测定为181umolL，
        // 生化检查未见异常，体温37.6℃，血压稳定
        // ner.extract3("母/n 孕期/t 患病/vi 及/c 治疗/n 情况/n ：/wm 定期/d 做/v 产前/t 检查/v ，/wd 孕期/t 体/n 健/a 。/wj 测量/v 餐/n 后/n 血糖/n 为/v 6.7/m mmol/nx //w /L 。/wj 昨日/t 经/p 皮/n 胆红素/n 测定/v 为/v 181/m umol/nx //w L/nx ，/wd 生化/n 检查/n 未/d 见/v 异常/n ，/wd 体温/n 37.6/m ℃/nx ，/wd 血压/n 稳定/a");

    }
}
