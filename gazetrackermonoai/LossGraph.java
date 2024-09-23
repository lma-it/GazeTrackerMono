import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

import static java.lang.Float.NaN;

public class LossGraph {
    private float[] lossX;
    private float[] lossY;
    private final int MAX_ITERATIONS = 125;
    private Path path = null;

    public LossGraph() {
        lossX = new float[MAX_ITERATIONS];
        lossY = new float[MAX_ITERATIONS];
    }

    public void fillLosses(int iteration, float[] losses){
        lossX[iteration] = losses[0];
        lossY[iteration] = losses[1];
    }


    // Метод для построения и сохранения графика
    public void createAndSaveChart(int index, int epoch) {
        int width = 1980;  // Ширина изображения
        int height = 1080;
        int margin = 50;

        // Высота изображения
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Задний фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Настройка цвета и линий сетки
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 10; i++) {
            int x = i * width / 10;
            int y = i * height / 10;
            g.drawLine(x, 0, x, height);
            g.drawLine(0, y, width, y);
        }



        // Оси
        g.setColor(Color.BLACK);
        g.drawLine(50, height - 50, width - 50, height - 50); // x-ось
        g.drawLine(50, 50, 50, height - 50);                 // y-ось

        // Подписи к осям
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Количество итераций", width / 2 - 50, height - 10);
        g.drawString("Для оси Х цвета: ", width / 2 - 860, height - 10);
        g.setColor(Color.ORANGE);
        g.fillRect(width / 2 - 725, height - 25, 15, 15);
        g.setColor(Color.BLUE);
        g.fillRect(width / 2 - 700, height - 25, 15, 15);
        g.drawString("Для оси Y цвета: ", width / 2 - 650, height - 10);
        g.setColor(Color.MAGENTA);
        g.fillRect(width / 2 - 525, height - 25,  15, 15);
        g.setColor(Color.RED);
        g.fillRect(width / 2 - 495, height - 25,  15, 15);
        //Дописать код для определения цветов для осей.
        g.setColor(Color.BLACK);
        g.drawString("Ошибка", 10, height / 2);

        // Метки на оси Y
        for (int i = 0; i <= 10; i++) {
            int y = 50 + i * (height - 100) / 10;
            g.drawString(String.format("%.1f", 1.0 - i * 0.1), 10, y + 5);
        }

        // Масштабирование данных для отображения
        double maxLoss = 1.0;  // Максимальное значение ошибки, предполагается от 0 до 1
        double scaleX = (width - 100) / (double) MAX_ITERATIONS;
        double scaleY = (height - 100) / maxLoss;

        // Рисуем кривую X
        for (int i = 0; i < MAX_ITERATIONS - 1; i++) {
            if(lossX[i] != 2){
                int x1 = 50 + (int) (i * scaleX);
                int y1 = height - 50 - (int) (lossX[i] * scaleY);
                int x2 = 50 + (int) ((i + 1) * scaleX);
                int y2 = height - 50 - (int) (lossX[i + 1] * scaleY);
                if(lossX[i] < 0.1){
                    g.setColor(Color.ORANGE);
                    g.fillRect(x1, y1, 6, 6);
                }else {
                    g.setColor(Color.BLUE);
                    g.fillRect(x1, y1, 6, 6);
                }
            }else{
                break;
            }

        }

        // Рисуем кривую Y
        for (int i = 0; i < MAX_ITERATIONS - 1; i++) {
            if(lossY[i] != 2){
                int x1 = 50 + (int) (i * scaleX);
                int y1 = height - 50 - (int) (lossY[i] * scaleY);
                int x2 = 50 + (int) ((i + 1) * scaleX);
                int y2 = height - 50 - (int) (lossY[i + 1] * scaleY);
                if(lossY[i] < 0.1){
                    g.setColor(Color.MAGENTA);
                    g.fillRect(x1, y1, 6, 6);
                }else{
                    g.setColor(Color.RED);
                    g.fillRect(x1, y1, 6, 6);
                }
            }else{
                break;
            }


        }

        // Установка толщины линии в 3 пикселя
        float lineWidth = 3.0f;
        g.setStroke(new BasicStroke(lineWidth));

        addTrendLine(g, lossX, width, height, margin, Color.BLACK);
        addTrendLine(g, lossY, width, height, margin, Color.RED);

        // Освобождаем ресурсы
        g.dispose();
        String directoryName = "/home/michael/Desktop/LossGraphs/epoch" + epoch;

        // Сохранение изображения в файл
        try {
            Path directoryPath = Paths.get(directoryName);
            if(Files.notExists(directoryPath)){
                // Создаем директорию для текущей эпохи
                path = Files.createDirectories(directoryPath);
                File outputFile = new File(path.toString(), "batch_" + index + ".png");
                //ImageIO.write(image, "png", new File("/home/michael/Desktop/LossGraphs/batch_" + index + ".png"));
                ImageIO.write(image, "png", outputFile);
                System.out.println("График сохранен как PNG.");
            }else{
                File outputFile = new File(path.toString(), "batch_" + index + ".png");
                ImageIO.write(image, "png", outputFile);
                System.out.println("График сохранен как PNG.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshLoss(){
        for(int i = 0; i < MAX_ITERATIONS; i++){
            lossX[i] = 2;
            lossY[i] = 2;
        }
    }

    private static void addTrendLine(Graphics2D g2d, float[] data, int width, int height, int margin, Color color) {
        int count = 0;
        for(int i = 0; i < data.length; i++){
            if(data[i] != 2){
                count++;
            }else{
                break;
            }
        }
        int n = count;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += data[i];
            sumXY += i * data[i];
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        g2d.setColor(color);
        int x1 = margin;
        int y1 = height - margin - (int) ((intercept + slope * 0) * (height - 2 * margin));
        int x2 = width - margin;
        int y2 = height - margin - (int) ((intercept + slope * (n - 1)) * (height - 2 * margin));
        g2d.drawLine(x1, y1, x2, y2);
    }
}
