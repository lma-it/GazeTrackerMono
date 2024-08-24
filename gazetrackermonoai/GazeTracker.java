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
    private static final int INPUT_NEURONS = 240 * 320;
    private static final int HIDDEN_NEURONS = 768; //(240 * 320) / 100;
    private static final int OUTPUT_NEURONS = 2;
    static float[] grayImage;
    private static final int SCREEN_WIDTH  = 1080; //ScreenSizeGetter.getScreenWidth();
    private static final int SCREEN_HEIGTH = 2200; //ScreenSizeGetter.getScreenHeight();
    private static final float LEARNING_RATE = (float)0.01;
    static float x;
    static float y;
    static float errorX;
    static float errorY;
    static float lambda = (float)0.01;
    

    // Веса для слоев
    static float[][] inputToHiddenWeights = new float[INPUT_NEURONS][HIDDEN_NEURONS];
    static float[][] hiddenToOutputWeights = new float[HIDDEN_NEURONS][OUTPUT_NEURONS];

    // Слои нейронов
    private static float[] inputLayer = new float[INPUT_NEURONS];
    private static float[] hiddenLayer = new float[HIDDEN_NEURONS];
    private static float[] outputLayer = new float[OUTPUT_NEURONS];
    static int count = 1;
    static String filename = "C:\\Users\\user\\Desktop\\NeuralNetwork\\MyAI5\\AdamWeights.txt";
    static boolean isTested = true; 
    static Random rand = new Random();
    static final GazeTracker network = new GazeTracker();
    AdamOptimizer optimizerOutputToHidden = new AdamOptimizer(HIDDEN_NEURONS, OUTPUT_NEURONS, LEARNING_RATE, (float) 0.9, (float)0.999, (float)1e-8);
    AdamOptimizer optimizerHiddenToInput = new AdamOptimizer(INPUT_NEURONS, HIDDEN_NEURONS, LEARNING_RATE, (float) 0.9, (float)0.999, (float)1e-8);
    

    // Конструктор
    public GazeTracker() {
        // Инициализация весов из файла или случайными значениями
        if(isTested){
            System.out.println("Create a NN...");
        }
        SaveAndLoadNN.loadWeights(filename);
    }

    // Инициализация весов
    static void initializeWeights() {
        if(isTested){
            System.out.println("initialize input-hidden weigths");
        }
        for (int i = 0; i < INPUT_NEURONS; i++) {
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                //inputToHiddenWeights[i][j] = (float) Math.random();
                inputToHiddenWeights[i][j] = (float) (rand.nextGaussian() * Math.sqrt(2.0 / (INPUT_NEURONS + HIDDEN_NEURONS)));
            }
        }
        if(isTested){
            System.out.println("initialize hidden-output weigths");
        }
        for (int i = 0; i < HIDDEN_NEURONS; i++) {
            for (int j = 0; j < OUTPUT_NEURONS; j++) {
                //hiddenToOutputWeights[i][j] = (float) Math.random();
                hiddenToOutputWeights[i][j] = (float) (rand.nextGaussian() * Math.sqrt(2.0 / (HIDDEN_NEURONS + OUTPUT_NEURONS)));
            }
        }
    }

    // Прямое распространение
    public void feedForward(float[] grayImage2) {
        
        // Преобразование RGB изображения в одномерный массив
        for (int i = 0; i < grayImage2.length; i++) {
            inputLayer[i] = grayImage2[i];
        }

        // Вычисление значений скрытого слоя
        for (int i = 0; i < HIDDEN_NEURONS; i++) {
           
            for (int j = 0; j < INPUT_NEURONS; j++) {
                hiddenLayer[i] += inputLayer[j] * inputToHiddenWeights[j][i];
                //hiddenLayer[i] = LEARNING_RATE * inputLayer[j] * inputToHiddenWeights[j][i];
            }
            hiddenLayer[i] = activationFunction(hiddenLayer[i]);
        }

        // Вычисление значений выходного слоя
        for (int i = 0; i < OUTPUT_NEURONS; i++) {
            
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                outputLayer[i] += hiddenLayer[j] * hiddenToOutputWeights[j][i];
                //outputLayer[i] = LEARNING_RATE * hiddenLayer[j] * hiddenToOutputWeights[j][i];
            }
            outputLayer[i] = activationFunction(outputLayer[i]);
            if(isTested & count % 10 == 0){
                System.out.println("The value of outputLayer[" + i + "]" + " = " + outputLayer[i]);
            }
        }
    }

    // Функция активации (например, сигмоид)
    private static float activationFunction(float x) {
        return (float)(1 / (1 + Math.exp(-x)));
    }

    // Геттеры для выходных значений
    public  float getXCoordinate(int width) {
        return (outputLayer[0] * width);
    }

    public float getYCoordinate(int heigth) {
        return (outputLayer[1] * heigth);
    }

    // Основная функция для тестирования
    public static void main(String[] args) throws IOException {
        if(isTested){
            System.out.println("Hello, I'm in main method...");
        }

        //SaveAndLoadNN.saveWeights(filename, network);

        // Загрузка DataFiles
        File folder = new File( "C:\\Users\\user\\Desktop\\dataset1");
        File[] listOfFiles = folder.listFiles();

        if(isTested){
            System.out.println("I start to prepare data, and learn NN...");
        }

        for (File file : listOfFiles) {
            
            if (file.isFile()) {
                // Тестовое RGB изображение (здесь должен быть ваш одномерный массив)
                BufferedImage originalImage = ImageIO.read(file);
                grayImage = ImageProcessor.convertToNormalizedArray(originalImage, 240, 320);

                String fileName = file.getName();
                fileName = fileName.replace("files_", ""); // Удаление "files_"
                String[] splitName = fileName.split("_");
                x = Float.parseFloat(splitName[0]);
                y = Float.parseFloat(splitName[1].split("\\.")[0] + "." + splitName[1].split("\\.")[1]);
                
                if(isTested & count % 10 == 0){
                    System.out.println("Start to learn of NN " + count + " times.");
                }

                for(int i = 0; i <= 10; i++){
                    
                }

                if(count % 10 != 0){
                    isTested = false;
                }else if(count % 10 == 0){
                    isTested = true;
                } 
                network.feedForward(grayImage);
                network.backpropagation(grayImage, x, y);

                
                
                // if(count % 10 == 0){

                //     if(isTested & count % 10 == 0){
                //         System.out.println("Start to backpropogation of NN after " + count + " times of learning epochs");
                //     }
                    
                // }
               
                // if(isTested & count % 5 == 0){
                //     System.out.println("NN says that x is " + network.getXCoordinate(SCREEN_WIDTH) + ", but the x is " + x);
                //     System.out.println("NN says that y is " + network.getYCoordinate(SCREEN_HEIGTH) + ", but the y is " + y);
                // }
                
                if(isTested & count % 100 == 0){
                    System.out.println("Save weights to file after " + count + " learning iterations...");
                    SaveAndLoadNN.saveWeights(filename, network);
                    System.out.println("Save is succesfull!");
                }
                // if(isTested){
                    
                // }
                count++;
                
            }
        }
        SaveAndLoadNN.saveWeights(filename, network);

    }

    // Метод обратного распространения ошибки
    public void backpropagation(float[] grayImage2, float expectedX, float expectedY) {

        if(isTested){
            System.out.println("Values before backpropogation: ");
            System.out.println("NN says that x is " + network.getXCoordinate(SCREEN_WIDTH) + ", but the x is " + x);
            System.out.println("NN says that y is " + network.getYCoordinate(SCREEN_HEIGTH) + ", but the y is " + y);
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


        // Обновления весов учитывая адаптивную скорость обучения с помощью AdamOptimizer
        
        // Обновление весов для скрытого к выходному слою
        optimizerOutputToHidden.updateOutputToHidden(hiddenToOutputWeights, outputGradients);
        // Обновление весов для входного к скрытому слою
        optimizerHiddenToInput.updateHiddenToInput(inputToHiddenWeights, hiddenGradients);

        // // Обновление весов для скрытого к выходному слою
        // for (int i = 0; i < HIDDEN_NEURONS; i++) {
        //     for (int j = 0; j < OUTPUT_NEURONS; j++) {
        //         hiddenToOutputWeights[i][j] -= LEARNING_RATE * outputGradients[j] * hiddenLayer[i];
        //     }
        // }

        // // Обновление весов для входного к скрытому слою
        // for (int i = 0; i < INPUT_NEURONS; i++) {
        //     for (int j = 0; j < HIDDEN_NEURONS; j++) {
        //         inputToHiddenWeights[i][j] -= LEARNING_RATE * hiddenGradients[j] * inputLayer[i];
        //     }
        // }

        network.feedForward(grayImage);
        if(isTested){
            float[] predicted = {outputLayer[0] * 1080, outputLayer[1] * 2200};
            float[] actual = {expectedX, expectedY};
            double loss = mseLoss(predicted, actual);
            System.out.println("Values after backpropogation: ");
            System.out.println("NN says that x is " + network.getXCoordinate(SCREEN_WIDTH) + ", but the x is " + x);
            System.out.println("NN says that y is " + network.getYCoordinate(SCREEN_HEIGTH) + ", but the y is " + y);
            System.out.println("Loss: " + loss);
        }

    }

    public double mseLoss(float[] predicted, float[] actual) {
        int n = predicted.length;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += Math.pow(predicted[i] - actual[i], 2);
        }
        return sum / n;
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



