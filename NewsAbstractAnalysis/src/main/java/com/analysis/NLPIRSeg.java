package com.analysis;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * 参考官方给出的样例：http://ictclas.nlpir.org/downloads
 * 删掉了一些无关代码
 */

public class NLPIRSeg {

    // 日志记录
    private static final Logger logger = LogManager.getLogger(NLPIRSeg.class);

    // 定义接口CLibrary，继承自com.sun.jna.Library
    public interface CLibrary extends Library {
        CLibrary Instance = (CLibrary) Native.loadLibrary("NLPIR", CLibrary.class);

        // printf函数声明
        public int NLPIR_Init(byte[] sDataPath, int encoding,
                              byte[] sLicenceCode);

        public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

        public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
                                        boolean bWeightOut);

        public void NLPIR_Exit();
    }

    public static String transString(String aidString, String ori_encoding,
                                     String new_encoding) {
        try {
            return new String(aidString.getBytes(ori_encoding), new_encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public NLPIRSeg() {
        try {
            initNLPIR();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void initNLPIR() throws UnsupportedEncodingException {
        String argu = "";
        // String system_charset = "GBK";//GBK----0
        String system_charset = "UTF-8";
        int charset_type = 1;
        // int charset_type = 0;
        // 调用printf打印信息
        int init_flag = CLibrary.Instance.NLPIR_Init(argu
                .getBytes(system_charset), charset_type, "0"
                .getBytes(system_charset));

        if (0 == init_flag) {
            logger.error("NLPIR 初始化失败！");
            return;
        }
        else {
            logger.info("成功初始化 NLPIR 分词工具！");
        }
    }


    public String segment(String sInput, int pos) {
        String nativeBytes = null;
        try {
            nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, pos);
            // String nativeStr = new String(nativeBytes, 0,
            // nativeBytes.length,"utf-8");
            // System.out.println("分词结果为： " + nativeBytes);
            // System.out.println("分词结果为： "
            // + transString(nativeBytes, system_charset, "UTF-8"));
            //
            // System.out.println("分词结果为： "
            // + transString(nativeBytes, "gb2312", "utf-8"));
            // CLibrary.Instance.NLPIR_Exit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return nativeBytes;
    }

    /**
     * 自定义对文件分词，输出到文件
     */
    public void segmentFile(String inputFileName, String outputFileName, int pos) throws IOException {
        logger.info("开始对文件 " + inputFileName +" 分词...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "utf-8"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName),"utf-8"));
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            if (line.length() > 1) {
                // System.out.println(line);
                String segLine = segment(line.trim(), pos);
                bw.write(segLine + "\n");
                bw.flush();
            }
        }
        bw.close();
        br.close();
        logger.info("分词结束！");
        // 在处理完后退出
        // CLibrary.Instance.NLPIR_Exit();
    }
}
