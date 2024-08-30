import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SaveAndLoadNN {
//    static GazeTracker network;
//    SaveAndLoadNN(GazeTracker network){
//        this.network = network;
//    }
    // Сохранение модели в файл
    public static void saveWeights(String filename, GazeTracker network) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            // Сохранение весов от входного к скрытому слою
            System.out.println("Save to file weights.txt input-hidden weigths");
            for (float[] layer : network.inputToHiddenWeights) {
                for (float weight : layer) {
                    out.print(weight + " ");
                }
                out.println();
            }
            // Сохранение весов от скрытого к выходному слою
            System.out.println("Save to file weights.txt hidden-output weigths");
            for (float[] layer : network.hiddenToOutputWeights) {
                for (float weight : layer) {
                    out.print(weight + " ");
                }
                out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Загрузка модели из файла
    public static void loadWeights(String filename, GazeTracker network) {
        File file = new File(filename);
        if (file.length() == 0) {
            // Инициализация весов с помощью Math.random(), если файл пустой
            GazeTracker.initializeWeights();
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                int i = 0;
                // Загрузка весов от входного к скрытому слою
                System.out.println("Read from file weights.txt input-hidden weigths");
                while ((line = br.readLine()) != null && i < network.inputToHiddenWeights.length) {
                    String[] weights = line.trim().split("\\s+");
                    for (int j = 0; j < weights.length; j++) {
                        network.inputToHiddenWeights[i][j] = (float)Double.parseDouble(weights[j]);
                    }
                    i++;
                }
                i = 0;
                // Загрузка весов от скрытого к выходному слою
                System.out.println("Read from file weights.txt hidden-output weigths");
                while ((line = br.readLine()) != null && i < network.hiddenToOutputWeights.length) {
                    String[] weights = line.trim().split("\\s+");
                    for (int j = 0; j < weights.length; j++) {
                        network.hiddenToOutputWeights[i][j] = (float)Double.parseDouble(weights[j]);
                    }
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
