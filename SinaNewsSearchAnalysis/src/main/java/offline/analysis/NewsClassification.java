package offline.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NewsClassification {
    // 日志记录
    static final Logger logger = LogManager.getLogger(NewsClassification.class.getName());

    // 模型的随机种子
    int seed;
    // 模型的学习率
    double learningRate;
    // 每一次迭代的样本数
    int batchSize;
    // 最大迭代轮数
    int nEpochs;

    int numInputs;
    int numOutputs;
    int numHiddenNodes;

    final String filenameTrain  = "train.txt";
    final String modelName = "model.zip";

    // Word2Vec 模型
    Word2Vec vec;
    MultiLayerNetwork model;

    public NewsClassification() {
        seed = 123;
        learningRate = 0.01;
        batchSize = 100;
        nEpochs = 2000;
        this.numInputs = 100;
        this.numOutputs = 10;
        this.numHiddenNodes = 25;
        vec = null;
        model = null;

    }

    public NewsClassification(int numInputs, int numOutputs, int numHiddenNodes) {
        seed = 123;
        learningRate = 0.01;
        batchSize = 100;
        nEpochs = 2000;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numHiddenNodes = numHiddenNodes;
        vec = null;
    }

    public void train() throws IOException, InterruptedException {
        // First: get the dataset using the record reader.
        // CSVRecordReader handles loading/parsing
        logger.info("加载训练文件...");
        int numLinesToSkip = 0;
        char delimiter = ',';
        RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
        recordReader.initialize(new FileSplit(new File(filenameTrain)));
        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, numInputs, numOutputs);

        logger.info("构建分类器结构...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .updater(Updater.NESTEROVS)     //To configure: .updater(new Nesterovs(0.9))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX).weightInit(WeightInit.XAVIER)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));  //Print score every 10 parameter updates
        logger.info("开始迭代训练...");
        for ( int n = 1; n < nEpochs+1; n++) {
            model.fit(iterator);
            if ( n % 25 == 0)
                logger.info("第" + n + "轮完成。");
        }

        //Save the model
        logger.info("训练结束，保存文件...");
        File locationToSave = new File(modelName);      //Where to save the network. Note: the file is in .zip format - can be opened externally
        boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
        ModelSerializer.writeModel(model, locationToSave, saveUpdater);

    }

    public void eval(String testFile) throws IOException, InterruptedException {
        int numLinesToSkip = 0;
        char delimiter = ',';
        if (model == null) {
            model = ModelSerializer.restoreMultiLayerNetwork(modelName);
        }
        RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
        recordReader.initialize(new FileSplit(new File(testFile)));
        DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, batchSize, numInputs, numOutputs);
        Evaluation eval = new Evaluation(numOutputs);
        while(testIter.hasNext()){
            DataSet t = testIter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray lables = t.getLabels();
            INDArray predicted = model.output(features,false);
            eval.eval(lables, predicted);
        }

        //Print the evaluation statistics
        System.out.println(eval.stats());

    }

    // 单个样例测试，
    // 仅给出结果，不与正确结果 l 比较，此时 l 可以随便设置
    // 如果与正确样本进行比较，那么 l 设置为对应的标签，注释的代码需要取消
    public int test(String s, int l) throws IOException {
        //Load the model
        logger.info("加载模型...");
        if (vec == null)
            vec = WordVectorSerializer.readWord2VecModel("word2vec_model.txt");
        if (model == null)
            model = ModelSerializer.restoreMultiLayerNetwork(modelName);
        logger.info("模型测试...");
        INDArray features = getVector(s, vec);
        /*
        Evaluation eval = new Evaluation(numOutputs);
        double[] temp = new double[numOutputs];
        for(int i = 0; i<numOutputs; ++i) {
            temp[i] = 0.0;
        }
        temp[l] = 1.0;
        INDArray lables = Nd4j.create(temp);
        logger.info(lables);
        */
        INDArray predicted = model.output(features,false);
        return Nd4j.getExecutioner().execAndReturn(new IMax(predicted)).getFinalResult();
        /*
        eval.eval(lables, predicted);
        System.out.println(eval.stats());
        */
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
