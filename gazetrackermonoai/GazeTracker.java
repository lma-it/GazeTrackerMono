import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

// !!!ВАЖНО!!!
// С этими же парметрами прогнать dataset1 полностью. Затем посмотреть стоит ли менять скорость обучения в более высокую
// оставить такую же или наоборот уменьшить... Все остается в силе.


public class GazeTracker {

    // Параметры для размера слоев
    private final int INPUT_NEURONS = 120 * 160;
    private final int HIDDEN_NEURONS = 5000; //(240 * 320) / 100;
    private final int OUTPUT_NEURONS = 2;
    private float[] grayImage;
    private static final int SCREEN_WIDTH  = 1080; //ScreenSizeGetter.getScreenWidth();
    private static final int SCREEN_HEIGHT = 2200; //ScreenSizeGetter.getScreenHeight();
    public static float LEARNING_RATE = 0.0033f;
    final float SCALE_COEFFICIENT = 2200.0f / 1080.0f;
    static float x;
    static float y;
    static float errorX;
    static float errorY;
    static float lambda = 0.001f;
    public float[] loss, newLoss;
    

    // Веса для слоев
    public float[][] inputToHiddenWeights = new float[INPUT_NEURONS][HIDDEN_NEURONS];
    public float[][] hiddenToOutputWeights = new float[HIDDEN_NEURONS][OUTPUT_NEURONS];

    // Слои нейронов
    public float[] inputLayer = new float[INPUT_NEURONS];
    public float[] hiddenLayer = new float[HIDDEN_NEURONS];
    public float[] outputLayer = new float[OUTPUT_NEURONS];
    public int count = 1;
    static String filename = "/home/michael/Desktop/weights/weights.txt";
    static boolean isTested = true; 
    static Random rand = new Random();
    static GazeTracker network = new GazeTracker();
    private final LossGraph lGraph = new LossGraph();

    private final AdamOptimizer optimizerOutputToHidden = new AdamOptimizer(0.9f, 0.999f, 1e-8f);
    private final AdamOptimizer optimizerHiddenToInput = new AdamOptimizer(0.9f, 0.999f, 1e-8f);


    private float[][] accumulatedInputToHiddenGradients;
    private float[] accumulatedHiddenToOutputGradients;

    public void initializeGradients() {
        // Инициализация массивов для накопления градиентов
        accumulatedInputToHiddenGradients = new float[INPUT_NEURONS][HIDDEN_NEURONS];
        accumulatedHiddenToOutputGradients = new float[HIDDEN_NEURONS];
    }


    // Конструктор
    public GazeTracker() {
        // Инициализация весов из файла или случайными значениями
        if(isTested){
            System.out.println("Create a NN...");
        }

    }

    public void accumulateGradients(float[][] inputToHiddenGradients, float[] hiddenGradients, float[] outputGradients) {
        // Накопление градиентов для весов вход-средний слой
        for (int i = 0; i < INPUT_NEURONS; i++) {
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                accumulatedInputToHiddenGradients[i][j] += inputToHiddenGradients[i][j];
            }
        }

        // Накопление градиентов для весов средний-выходной слой
        for (int i = 0; i < HIDDEN_NEURONS; i++) {
            for (int j = 0; j < OUTPUT_NEURONS; j++) {
                accumulatedHiddenToOutputGradients[i] += hiddenGradients[i] * outputGradients[j];
            }
        }
    }

    // Инициализация весов
    static void initializeWeights() {
        if(isTested){
            System.out.println("initialize input-hidden weights");
        }
        for (int i = 0; i < network.INPUT_NEURONS; i++) {
            for (int j = 0; j < network.HIDDEN_NEURONS; j++) {
                //network.inputToHiddenWeights[i][j] = (float) (rand.nextGaussian() * Math.sqrt(2.0 / (network.INPUT_NEURONS + network.HIDDEN_NEURONS)));
                network.inputToHiddenWeights[i][j] = (float) (rand.nextGaussian() * Math.sqrt(1.0 / network.INPUT_NEURONS));
            }
        }
        if(isTested){
            System.out.println("initialize hidden-output weights");
        }
        for (int i = 0; i < network.HIDDEN_NEURONS; i++) {
            for (int j = 0; j < network.OUTPUT_NEURONS; j++) {
                //network.hiddenToOutputWeights[i][j] = (float) (rand.nextGaussian() * Math.sqrt(2.0 / (network.HIDDEN_NEURONS + network.OUTPUT_NEURONS)));
                network.hiddenToOutputWeights[i][j] = (float) (rand.nextGaussian() * Math.sqrt(1.0 / network.HIDDEN_NEURONS));
            }
        }
    }

    // Прямое распространение
    public void feedForward(float[] grayImage) {
        
        // Преобразование RGB изображения в одномерный массив
        System.arraycopy(grayImage, 0, inputLayer, 0, grayImage.length);

        // Вычисление значений скрытого слоя
        for (int i = 0; i < HIDDEN_NEURONS; i++) {
           hiddenLayer[i] = 0;
            for (int j = 0; j < INPUT_NEURONS; j++) {
                hiddenLayer[i] += inputLayer[j] * inputToHiddenWeights[j][i];
            }
            hiddenLayer[i] = activationFunction(hiddenLayer[i]);
        }

        // Вычисление значений выходного слоя
        for (int i = 0; i < OUTPUT_NEURONS; i++) {
            outputLayer[i] = 0;
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                outputLayer[i] += hiddenLayer[j] * hiddenToOutputWeights[j][i];
            }
            outputLayer[i] = activationFunction(outputLayer[i]);
        }

//        // Масштабируем значение для y
//        outputLayer[1] /= SCALE_COEFFICIENT;
    }

    // Функция активации (например, сигмоид)
    private static float activationFunction(float x) {
        return (float)(1 / (1 + Math.exp(-x)));
    }

    // Геттеры для выходных значений
    public float getXCoordinate(int width) {
        return (outputLayer[0] * width);
    }

    public float getYCoordinate(int height) { return (outputLayer[1] * height); }


    // Основная функция для тестирования
    public static void main(String[] args) throws IOException {
        int epoch;
        for(epoch = 21; epoch <= 40; epoch++){

            for(int i = 1; i <= 1; i++) {

                SaveAndLoadNN.loadWeights(filename, network);

                if (isTested) {
                    System.out.println("Hello, I'm in main method...");
                }

                // Загрузка DataFiles
                File folder = new File("/home/michael/Desktop/batch/batch" + i);
                File[] listOfFiles = folder.listFiles();


                if (isTested) {
                    System.out.println("I start to prepare data, and learn NN...");
                }

                int iteration = 0;
//                network.initializeGradients();

                for (File file : listOfFiles) {

                    if (file.isFile()) {
                        // Тестовое RGB изображение (здесь должен быть ваш одномерный массив)
                        BufferedImage originalImage = ImageIO.read(file);
                        network.grayImage = ImageProcessor.convertToNormalizedArray(originalImage, 120, 160);

                        String fileName = file.getName();
                        fileName = fileName.replace("files_", ""); // Удаление "files_"
                        String[] splitName = fileName.split("_");
                        x = Float.parseFloat(splitName[0]);
                        y = Float.parseFloat(splitName[1].split("\\.")[0] + "." + splitName[1].split("\\.")[1]);

                        isTested = network.count % 10 == 0 ? true : false;
                        if (isTested & network.count % 10 == 0) {
                            System.out.println("Start to learn of NN " + network.count + " times.");
                        }

                        network.feedForward(network.grayImage);
                        network.backpropagation(network.grayImage, x, y);
                        network.lGraph.fillLosses(iteration, network.newLoss);
                        iteration++;

//                    if (isTested & network.count % 200 == 0) {
//                        System.out.println("Save weights to file after " + network.count + " learning iterations...");
//                        SaveAndLoadNN.saveWeights(filename, network);
//                        System.out.println("Save is successful!");
//                    }

                        network.count++;

                    }
                }
                //network.updateWeightsWithAdam();
                SaveAndLoadNN.saveWeights(filename, network);
                network.lGraph.createAndSaveChart(i, epoch);
                network.lGraph.refreshLoss();
            }
        }

    }

    public void updateWeightsWithAdam() {
        // Обновление весов для скрытого к выходному слою
        //optimizerOutputToHidden.updateOutputToHidden(hiddenToOutputWeights, accumulatedHiddenToOutputGradients);

        // Обновление весов для входного к скрытому слою
        optimizerHiddenToInput.updateHiddenToInput(inputToHiddenWeights, accumulatedInputToHiddenGradients);

        // Обнуление накопленных градиентов после обновления весов
        initializeGradients();
    }

    // Метод обратного распространения ошибки
    public void backpropagation(float[] grayImage, float expectedX, float expectedY) {

        float[] predicted = new float[]{outputLayer[0], outputLayer[1]};
        float[] actual = new float[]{expectedX, expectedY};
        loss = mseLosses(predicted, actual);
        if(isTested & network.count % 10 == 0){
            System.out.println("Values before backpropagation: ");
            System.out.println("NN says that x is " + network.getXCoordinate(SCREEN_WIDTH) + ", but the x is " + x);
            System.out.println("NN says that y is " + network.getYCoordinate(SCREEN_HEIGHT) + ", but the y is " + y);
            System.out.println("Loss for X is: " + loss[0] + ", and loss for Y is: " + loss[1]);
            System.out.println("Learning Rate is: " + LEARNING_RATE);

        }
    
        // Вычисление ошибки для выходного слоя (предполагаем, что у нас 2 выходных нейрона)
        errorX = outputLayer[0] - expectedX / 1080;
        errorY = outputLayer[1] - expectedY/ 2200;

        // Градиенты для выходного слоя
        float[] outputGradients = new float[OUTPUT_NEURONS];
        outputGradients[0] = errorX * derivativeActivationFunction(outputLayer[0]);
        outputGradients[1] = errorY * derivativeActivationFunction(outputLayer[1]);

        // Градиенты для скрытого слоя
        float[][] hiddenGradients = new float[HIDDEN_NEURONS][OUTPUT_NEURONS];

        for (int i = 0; i < HIDDEN_NEURONS; i++) {
            for (int j = 0; j < OUTPUT_NEURONS; j++) {
                hiddenGradients[i][j] = 0;
                hiddenGradients[i][j] = outputGradients[j] * hiddenToOutputWeights[i][j];
            }

            hiddenGradients[i][0] *= derivativeActivationFunction(hiddenLayer[i]);
            hiddenGradients[i][1] *= derivativeActivationFunction(hiddenLayer[i]);

        }

        // Градиенты для входного слоя
        float[][] inputToHiddenGradients = new float[INPUT_NEURONS][HIDDEN_NEURONS];
        for (int i = 0; i < INPUT_NEURONS; i++) {
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                inputToHiddenGradients[i][j] = (hiddenGradients[j][0] + hiddenGradients[j][1]) * inputLayer[i];
            }
        }

        // Накопление градиентов для текущего экземпляра
        //accumulateGradients(inputToHiddenGradients, hiddenGradients, outputGradients);

        optimizerOutputToHidden.updateOutputToHidden(hiddenToOutputWeights, hiddenGradients);
        // Обновление весов для входного к скрытому слою
        optimizerHiddenToInput.updateHiddenToInput(inputToHiddenWeights, inputToHiddenGradients);

        network.feedForward(grayImage);
        //scaledExpectedY = expectedY / SCALE_COEFFICIENT;
        float[] newPredicted = new float[] {outputLayer[0], outputLayer[1]};
        //float[] newActual = new float[] {expectedX, scaledExpectedY};
        newLoss = mseLosses(newPredicted, actual);
        AdaptiveLearningRate(loss, newLoss);
        if(isTested & network.count % 10 == 0){
            System.out.println("Values after backpropagation: ");
            System.out.println("NN says that x is " + network.getXCoordinate(SCREEN_WIDTH) + ", but the x is " + x);
            System.out.println("NN says that y is " + network.getYCoordinate(SCREEN_HEIGHT) + ", but the y is " + y);
            System.out.println("Loss for X is: " + newLoss[0] + ", and loss for Y is: " + newLoss[1]);
            System.out.println("Learning Rate is: " + LEARNING_RATE + "\n");

        }

    }

    public void AdaptiveLearningRate(float[] loss, float[] newloss){
        if (loss[0] <= newloss[0] & loss[1] <= newloss[1]) {
            LEARNING_RATE += LEARNING_RATE * 0.0001f;
        }else if((loss[0] > newloss[0] & loss[1] > newloss[1]) & ((LEARNING_RATE - LEARNING_RATE * 0.0001f) > 0)){
            LEARNING_RATE -= LEARNING_RATE * 0.0001f;
        }
    }

    public float[] mseLosses(float[] predicted, float[] actual){
        return new float[]{Math.abs(predicted[0] - (actual[0] / 1080)), Math.abs(predicted[1] - (actual[1] / 2200))};
    }

    // Производная функции активации (например, сигмоид)
    private static float derivativeActivationFunction(float x) {
        float sigmoid = activationFunction(x);
        return sigmoid * (1 - sigmoid);
    }

    // L1 регуляризация
    public static float l1Regularization(float weight) {
        return lambda * Math.signum(weight);
    }

    // L2 регуляризация
    public static float l2Regularization(float weight) {
        return lambda * weight;
    }
}



