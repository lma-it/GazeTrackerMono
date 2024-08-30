import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class LossGraph {
    private float[] lossX;
    private float[] lossY;
    private final int MAX_ITERATIONS = 4642;

    public LossGraph() {
        lossX = new float[MAX_ITERATIONS];
        lossY = new float[MAX_ITERATIONS];
    }

    public void fillLosses(int iteration, float[] losses){
        lossX[iteration] = losses[0];
        lossY[iteration] = losses[1];
    }


    // Метод для построения и сохранения графика
    public void createAndSaveChart() {
        int width = 1980;  // Ширина изображения
        int height = 1080; // Высота изображения
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
        g.setColor(Color.BLUE);
        for (int i = 0; i < MAX_ITERATIONS - 1; i++) {
            int x1 = 50 + (int) (i * scaleX);
            int y1 = height - 50 - (int) (lossX[i] * scaleY);
            int x2 = 50 + (int) ((i + 1) * scaleX);
            int y2 = height - 50 - (int) (lossX[i + 1] * scaleY);
            g.drawLine(x1, y1, x2, y2);
        }

        // Рисуем кривую Y
        g.setColor(Color.RED);
        for (int i = 0; i < MAX_ITERATIONS - 1; i++) {
            int x1 = 50 + (int) (i * scaleX);
            int y1 = height - 50 - (int) (lossY[i] * scaleY);
            int x2 = 50 + (int) ((i + 1) * scaleX);
            int y2 = height - 50 - (int) (lossY[i + 1] * scaleY);
            g.drawLine(x1, y1, x2, y2);
        }

        // Освобождаем ресурсы
        g.dispose();

        // Сохранение изображения в файл
        try {
            ImageIO.write(image, "png", new File("/home/michael/Рабочий стол/LossGraphs/epoch_" + System.currentTimeMillis() + ".png"));
            System.out.println("График сохранен как PNG.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
