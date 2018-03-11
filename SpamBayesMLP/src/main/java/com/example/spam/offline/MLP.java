package com.example.spam.offline;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class MLP {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    // 模型的随机种子
    int seed;
    // 模型的学习率
    double learningRate;
    // 每一次迭代的样本数
    int batchSize;
    // 最大迭代轮数
    int nEpochs;

    // 邮件样本的特征维度
    int numInputs;
    // 二分类问题，= 2
    int numOutputs;
    // 总的样本数
    int examples;
    // 模型超参，隐藏层数量
    int numHiddenLayers;
    // 中间隐藏层的节点数
    int[] numHiddenNodes;

    final String filenameTrain  = "spamdata/spambase.data";
    final String modelName = "model.zip";

    // MakeFeature 对象，帮助从文本转换为特征向量
    MakeFeature mf;

    // MLP 模型
    MultiLayerNetwork model;

    public MLP() {
        model = null;
        mf = new MakeFeature();
    }

    public MLP(int numHiddenLayers, int[] numHiddenNodes) {
        seed = 123;
        learningRate = 0.0001;
        batchSize = 500;
        nEpochs = 2000;
        numInputs = 57;
        numOutputs = 2;
        examples = 4601;
        this.numHiddenLayers = numHiddenLayers; // 一般 2-3 层即可取得较好的效果
        int[] temp = new int[numHiddenLayers+2];
        temp[0] = numInputs;
        int i = 1;
        for(; i<Math.min(numHiddenLayers+1, numHiddenNodes.length+1); ++i)
            temp[i] = numHiddenNodes[i-1];
        while ( i < numHiddenLayers + 1) {
            temp[i++] = numHiddenNodes[numHiddenNodes.length-1];
        }
        temp[i] = numOutputs;
        this.numHiddenNodes = temp;
        model = null;
        String structure = "";
        for(int j = 0; j<numHiddenLayers+1; ++j) {
            structure += (temp[j] + "-");
        }
        structure += String.valueOf(numOutputs);
        logger.info("MLP 模型结构：" + structure);
        mf = new MakeFeature();
    }


    public void train() throws IOException, InterruptedException {
        // First: get the dataset using the record reader.

        logger.info("加载训练文件...");
        int numLinesToSkip = 0;
        char delimiter = ',';
        // DL4J 中的数据模块，支持从文件中读取文件
        RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
        // 加载训练文件
        recordReader.initialize(new FileSplit(new File(filenameTrain)));
        // 训练数据的迭代器，每次选择一部分数据更新模型
        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, numInputs, numOutputs);

        logger.info("构建多层感知机的分类器结构...");
        NeuralNetConfiguration.ListBuilder listBuilder = new NeuralNetConfiguration.Builder()
                                                                                    .seed(seed) // 随机数种子
                                                                                    .iterations(2) // 每一个 batch 该轮训练过程迭代 2 次
                                                                                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT) // 选择优化器
                                                                                    .learningRate(learningRate) // 学习率
                                                                                    .updater(Updater.NESTEROVS)     // 优化相关的设置
                                                                                    .layer(new RBM())
                                                                                    .list();
        int l = 0;
        for(; l<numHiddenLayers; ++l) {
            listBuilder.layer(l, new DenseLayer.Builder().nIn(numHiddenNodes[l]).nOut(numHiddenNodes[l+1])
                    .weightInit(WeightInit.XAVIER)
                    .activation(Activation.TANH)
                    .build());
            logger.info("第 " + (l+1) + "层，" + numHiddenNodes[l] + "-" + numHiddenNodes[l+1]);
        }
        // 最后的输出层
        MultiLayerConfiguration conf = listBuilder.layer(l, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX).weightInit(WeightInit.XAVIER)
                        .nIn(numHiddenNodes[l]).nOut(numOutputs).build())
                        .pretrain(false).backprop(true).build();
        logger.info("输出层，" +numHiddenNodes[l] + "-" + numHiddenNodes[l+1]);

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));  // 每 100 轮输出一下训练过程中的信息
        logger.info("开始迭代训练...");
        for ( int n = 1; n < nEpochs+1; n++) {
            model.fit(iterator);
            if ( n % 25 == 0)
                logger.info("第" + n + "轮完成。");
        }

        // 把模型保存在本地 model.zip 中，
        // 以后不用重复训练，直接读取
        logger.info("训练结束，保存文件...");
        File locationToSave = new File(modelName);
        // 可以设置是否支持增量训练
        boolean saveUpdater = true;
        ModelSerializer.writeModel(model, locationToSave, saveUpdater);
    }

    // 训练集上的测评，使用 deeplearning4j 提供的测试接口
    public void eval(String testFile) throws IOException, InterruptedException {
        int numLinesToSkip = 0;
        char delimiter = ',';
        if (model == null) {
            model = ModelSerializer.restoreMultiLayerNetwork(modelName);
        }
        // 读文件
        RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
        recordReader.initialize(new FileSplit(new File(testFile)));
        DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, batchSize, numInputs, numOutputs);
        Evaluation eval = new Evaluation(numOutputs);
        // 按照 batchSize 读取数据，分别测试
        while(testIter.hasNext()){
            DataSet t = testIter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray lables = t.getLabels();
            INDArray predicted = model.output(features,false);
            // 记录下该次测试的结果
            eval.eval(lables, predicted);
        }
        // 输出结果
        System.out.println(eval.stats());
    }

    // 自己实现一个验证训练数据集的结果
    public double[] testTrainingData() throws IOException {
        if (model == null) {
            logger.info("加载模型...");
            model = ModelSerializer.restoreMultiLayerNetwork(modelName);
        }
        logger.info("模型测试...");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("spamdata/spambase.data")));
        int correctSpam = 0;
        int correctHam = 0;
        int totalSpam = 0;
        int totalHam = 0;
        int correct = 0;
        String line;
        double[] tempFeatures = new double[numInputs];
        while(true) {
            line = br.readLine();
            if (line == null)
                break;
            String[] features = line.split(",");
            int i = 0;
            for(; i<numInputs; ++i) {
                tempFeatures[i] = Double.valueOf(features[i]);
            }
            //
            int ll = Integer.valueOf(features[i]);
            INDArray featuresINDArray = Nd4j.create(tempFeatures);
            INDArray predicted = model.output(featuresINDArray, false);
            int label = Nd4j.getExecutioner().execAndReturn(new IMax(predicted)).getFinalResult();
            if(ll == 0) {
                totalHam++;
                if(label == 0) {
                    correctHam++;
                }
            } else {
                totalSpam++;
                if(label == 1)
                    correctSpam++;
            }
        }
        correct = correctHam + correctSpam;
        double acc = (double) correct / examples * 100;
        logger.warn(String.format("平均准确率：%.4f%%", acc));
        double accSpam = (double) correctSpam / totalSpam * 100;
        double accHam = (double) correctHam / totalHam * 100;
        logger.warn(String.format("识别出垃圾邮件的准确率：%.4f%%", accSpam));
        logger.warn(String.format("识别出正确邮件的准确率：%.4f%%", accHam));
        double[] values = new double[3];
        values[0] = acc;
        values[1] = accSpam;
        values[2] = accHam;
        return values;
    }

    // 单个样例测试，
    // 仅给出结果，不与正确结果 l 比较，此时 l 可以随便设置
    // 如果与正确样本进行比较，那么 l 设置为对应的标签，注释的代码需要取消
    public int test(String s) throws IOException {
        if (model == null) {
            logger.info("加载模型...");
            model = ModelSerializer.restoreMultiLayerNetwork(modelName);
        }
        logger.info("模型测试...");
        INDArray features = getVector(s);
        INDArray predicted = model.output(features,false);
        // System.out.println(predicted);
        return Nd4j.getExecutioner().execAndReturn(new IMax(predicted)).getFinalResult();
    }

    private INDArray getVector(String s) {
        double[] temp = mf.makeTextFeature(s);
        INDArray featureVector = Nd4j.create(temp);
        return featureVector;
    }
}
