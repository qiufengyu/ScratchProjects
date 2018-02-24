package offline.analysis;

import offline.utils.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewsFeature {
    // 日志记录
    static final Logger logger = LogManager.getLogger(NewsFeature.class.getName());

    private DatabaseUtil db;

    public NewsFeature() {
        db = new DatabaseUtil();
    }

    // 从数据库中提取所有的数据，得到一个语料文件，
    // 借助 DeepLearning4j 训练 word2vec
    public void generatRawTextFile(String corpus) throws SQLException, IOException {
        ResultSet rs = db.selectAllNews();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(corpus), "utf-8"));
        while(rs.next()) {
            String title = rs.getString("title");
            java.sql.Blob blob = rs.getBlob("content");
            String content = new String(blob.getBytes(1l, (int) blob.length()));
            String[] titleSplit = title.split("");
            bw.write(String.join(" ", titleSplit));
            bw.write("\n");
            // bw.write(content);
            content = content.replaceAll("。", "。\n");
            content = content.replaceAll("\n+", "\n");
            String lines = String.join(" ", content.split(""));
            bw.write(lines.replaceAll("\n ", "\n"));
            bw.write('\n');
            bw.flush();
        }

    }

    public void trainWord2Vec(String corpus) {
        logger.info("加载语料...");
        SentenceIterator iter = new LineSentenceIterator(new File("corpus.txt"));
        iter.setPreProcessor(new SentencePreProcessor() {
            @Override
            public String preProcess(String sentence) {
                return sentence.toLowerCase();
            }
        });
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        logger.info("建立 Word2Vec 模型...");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(5)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        logger.info("训练 Word2Vec 模型...");
        vec.fit();

        logger.info("保存 Word2Vec 模型...");
        WordVectorSerializer.writeWord2VecModel(vec, "word2vec_model.txt");
        /*
        logger.info("Closest Words:");
        Collection<String> lst = vec.wordsNearestSum("日", 10);
        logger.info("10 Words closest to 'day': {}", lst);
        System.out.println(vec.hasWord("国"));
        double[] nian = vec.getWordVector("年");
        for(double d: nian)
            System.out.print(d+",");
        System.out.println("\n");
        */
    }

    public void generateTrainData(String trainFile) throws IOException, SQLException {
        Word2Vec vec = WordVectorSerializer.readWord2VecModel("word2vec_model.txt");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile)));
        ResultSet rs = db.selectAllNews();
        while(rs.next()) {
            INDArray ar = getVector(rs.getString("title"), vec);
            List<String> arString = new ArrayList<>();
            for (int i = 0; i < ar.shape()[1]; ++i)
                arString.add(String.format("%.6f", ar.getDouble(i)));
            bw.write(String.join(",", arString));
            bw.write(",");
            bw.write(rs.getInt("category")+"\n");
            bw.flush();
        }
        bw.close();

    }

    private INDArray getVector(String s, Word2Vec vec) {
        String[] tokens = s.split("");
        List<String> validTokens = new ArrayList<String>();
        for (String t: tokens) {
            if(vec.hasWord(t)) {
                validTokens.add(t);
            }
        }
        INDArray meanVector = vec.getWordVectorsMean(validTokens);
        return  meanVector;
    }



}
