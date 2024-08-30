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
    static int width = 1080;
    static int height = 2200;
    private final int INPUT_NEURONS = 120 * 160;
    private final int HIDDEN_NEURONS = 1400; //(240 * 320) / 100;
    private final int OUTPUT_NEURONS = 2;
    private float[] grayImage;
    private static final int SCREEN_WIDTH  = 1080; //ScreenSizeGetter.getScreenWidth();
    private static final int SCREEN_HEIGTH = 2200; //ScreenSizeGetter.getScreenHeight();
    public static float LEARNING_RATE = 0.0015f;
    static float x;
    static float y;
    static float errorX;
    static float errorY;
    static float lambda = 0.01f;
    public float[] loss, newLoss;
    

    // Веса для слоев
    public float[][] inputToHiddenWeights = new float[INPUT_NEURONS][HIDDEN_NEURONS];
    public float[][] hiddenToOutputWeights = new float[HIDDEN_NEURONS][OUTPUT_NEURONS];

    // Слои нейронов
    public float[] inputLayer = new float[INPUT_NEURONS];
    public float[] hiddenLayer = new float[HIDDEN_NEURONS];
    public float[] outputLayer = new float[OUTPUT_NEURONS];
    public int count = 1;
    static String filename = "/home/michael/Рабочий стол/weights/weights.txt";
    static boolean isTested = true; 
    static Random rand = new Random();
    static final GazeTracker network = new GazeTracker();
    private LossGraph lGraph = new LossGraph();
    AdamOptimizer optimizerOutputToHidden = new AdamOptimizer(HIDDEN_NEURONS, OUTPUT_NEURONS, (float) 0.9, (float)0.999, (float)1e-8);
    AdamOptimizer optimizerHiddenToInput = new AdamOptimizer(INPUT_NEURONS, HIDDEN_NEURONS, (float) 0.9, (float)0.999, (float)1e-8);

    // Конструктор
    public GazeTracker() {
        // Инициализация весов из файла или случайными значениями
        if(isTested){
            System.out.println("Create a NN...");
        }

    }

    // Инициализация весов
    static void initializeWeights() {
        if(isTested){
            System.out.println("initialize input-hidden weigths");
        }
        for (int i = 0; i < network.INPUT_NEURONS; i++) {
            for (int j = 0; j < network.HIDDEN_NEURONS; j++) {
                //network.inputToHiddenWeights[i][j] = (float) (rand.nextGaussian() * Math.sqrt(2.0 / (network.INPUT_NEURONS + network.HIDDEN_NEURONS)));
                network.inputToHiddenWeights[i][j] = (float) (rand.nextGaussian() * Math.sqrt(1.0 / network.INPUT_NEURONS));
            }
        }
        if(isTested){
            System.out.println("initialize hidden-output weigths");
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
    }

    // Функция активации (например, сигмоид)
    private static float activationFunction(float x) {
        return (float)(1 / (1 + Math.exp(-x)));
    }

    // Геттеры для выходных значений
    public float getXCoordinate(int width) {
        return (outputLayer[0] * width);
    }

    public float getYCoordinate(int heigth) {
        return (outputLayer[1] * heigth);
    }

    // Основная функция для тестирования
    public static void main(String[] args) throws IOException {

        // for(int i = 0; i < 50; i++) {

            SaveAndLoadNN.loadWeights(filename, network);

            if (isTested) {
                System.out.println("Hello, I'm in main method...");
            }

            // Загрузка DataFiles
            File folder = new File("/home/michael/Рабочий стол/batch");
            File[] listOfFiles = folder.listFiles();

            if (isTested) {
                System.out.println("I start to prepare data, and learn NN...");
            }

            int iteration = 0;

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

                    if (isTested & network.count % 200 == 0) {
                        System.out.println("Save weights to file after " + network.count + " learning iterations...");
                        SaveAndLoadNN.saveWeights(filename, network);
                        System.out.println("Save is successful!");
                    }

                    network.count++;

                }
            }
            SaveAndLoadNN.saveWeights(filename, network);
            network.lGraph.createAndSaveChart();
        // }
    }

    // Метод обратного распространения ошибки
    public void backpropagation(float[] grayImage, float expectedX, float expectedY) {
        float[] predicted = new float[]{outputLayer[0], outputLayer[1]};
        float[] actual = new float[]{expectedX, expectedY};
        loss = mseLosses(predicted, actual);
        if(isTested & network.count % 10 == 0){
            System.out.println("Values before backpropagation: ");
            System.out.println("NN says that x is " + network.getXCoordinate(SCREEN_WIDTH) + ", but the x is " + x);
            System.out.println("NN says that y is " + network.getYCoordinate(SCREEN_HEIGTH) + ", but the y is " + y);
            System.out.println("Loss for X is: " + loss[0] + ", and loss for Y is: " + loss[1]);
            System.out.println("Learning Rate is: " + LEARNING_RATE);

        }
    
        // Вычисление ошибки для выходного слоя (предполагаем, что у нас 2 выходных нейрона)
        errorX = outputLayer[0] - expectedX / 1080;
        errorY = outputLayer[1] - expectedY / 2200;

        // Градиенты для выходного слоя
        float[] outputGradients = new float[OUTPUT_NEURONS];
        outputGradients[0] = errorX * derivativeActivationFunction(outputLayer[0]);
        outputGradients[1] = errorY * derivativeActivationFunction(outputLayer[1]);

        // Градиенты для скрытого слоя
        float[] hiddenGradients = new float[HIDDEN_NEURONS];
        for (int i = 0; i < HIDDEN_NEURONS; i++) {
            hiddenGradients[i] = 0;
            for (int j = 0; j < OUTPUT_NEURONS; j++) {
                hiddenGradients[i] += outputGradients[j] * hiddenToOutputWeights[i][j];
            }
            hiddenGradients[i] *= derivativeActivationFunction(hiddenLayer[i]);
        }

        // Градиенты для входного слоя
        float[][] inputToHiddenGradients = new float[INPUT_NEURONS][HIDDEN_NEURONS];
        for (int i = 0; i < INPUT_NEURONS; i++) {
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                inputToHiddenGradients[i][j] = hiddenGradients[j] * inputToHiddenWeights[i][j] * derivativeActivationFunction(inputLayer[i]);
            }
        }


        optimizerOutputToHidden.updateOutputToHidden(hiddenToOutputWeights, hiddenGradients, outputGradients);
        // Обновление весов для входного к скрытому слою
        optimizerHiddenToInput.updateHiddenToInput(inputToHiddenWeights, inputToHiddenGradients);

        network.feedForward(grayImage);
        float[] newPredicted = new float[] {outputLayer[0], outputLayer[1]};
        float[] newActual = new float[] {expectedX, expectedY};
        newLoss = mseLosses(newPredicted, newActual);
        AdaptiveLearningRate(loss, newLoss);
        if(isTested & network.count % 10 == 0){
            System.out.println("Values after backpropagation: ");
            System.out.println("NN says that x is " + network.getXCoordinate(SCREEN_WIDTH) + ", but the x is " + x);
            System.out.println("NN says that y is " + network.getYCoordinate(SCREEN_HEIGTH) + ", but the y is " + y);
            System.out.println("Loss for X is: " + newLoss[0] + ", and loss for Y is: " + newLoss[1]);
            System.out.println("Learning Rate is: " + LEARNING_RATE + "\n");

        }



    }

    public void AdaptiveLearningRate(float[] loss, float[] newloss){
        if (loss[0] < newloss[0] || loss[1] < newloss[1]) {
            LEARNING_RATE += LEARNING_RATE * 0.0001f;
        }else if((loss[0] > newloss[0] & loss[1] > newloss[1]) & ((LEARNING_RATE - LEARNING_RATE * 0.0001f) > 0)){
            LEARNING_RATE -= LEARNING_RATE * 0.0001f;
        }
    }

    public float mseLoss(float[] predicted, float[] actual) {
        int n = predicted.length;
        float sum = 0.0f;
        float diff;
        for (int i = 0; i < n; i++) {
            if(i == 0){
                diff = predicted[i] - (actual[i] / 1080);
            }
            else{
                diff = predicted[i] - (actual[i] / 2200);
            }
            //sum += diff * diff; // Используем умножение вместо Math.pow для повышения производительности
            sum += diff;
        }
        return Math.abs(sum / n);
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



