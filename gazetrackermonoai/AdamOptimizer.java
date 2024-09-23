public class AdamOptimizer {
    private float beta1;
    private float beta2;
    private float epsilon;
//    private float[][] m;
//    private float[][] v;

    public AdamOptimizer(float beta1, float beta2, float epsilon) {
        this.beta1 = beta1;
        this.beta2 = beta2;
        this.epsilon = epsilon;
//        this.m = new float[numRows][numCols];
//        this.v = new float[numRows][numCols];
    }

    public void updateHiddenToInput(float[][] params, float[][] grads) {

        for (int i = 0; i < params.length; i++) {
            for (int j = 0; j < params[i].length; j++) {
                float grad = grads[i][j];

                // Обрезка градиента, если он выходит за пределы
                //grad = clipGradient(grad);

                // Обновление моментов
//                m[i][j] = beta1 * m[i][j] + (1 - beta1) * grad;
//                v[i][j] = beta2 * v[i][j] + (1 - beta2) * grad * grad;
                float m = (1 - beta1) * grad;
                float v = (1 - beta2) * grad * grad;

                // Коррекция смещения
//                float mHat = m[i][j] / (1 - beta1);
//                float vHat = v[i][j] / (1 - beta2);

                float mHat = m / (1 - beta1);
                float vHat = v / (1 - beta2);

                // Регуляризация и обновление весов
                float regularizedGrad = grad + GazeTracker.l1Regularization(params[i][j]) + GazeTracker.l2Regularization(params[i][j]);
                params[i][j] -= (float) (GazeTracker.LEARNING_RATE * mHat / (Math.sqrt(vHat) + epsilon) + regularizedGrad);
            }
        }
    }

    public void updateOutputToHidden(float[][] params, float[][] hiddenGrads) {

        for (int i = 0; i < params.length; i++) {
            for (int j = 0; j < params[i].length; j++) {
                //float grad = hiddenGrads[i] * outputGrads[j];
                float grad = hiddenGrads[i][j];

                // Обрезка градиента
                //grad = clipGradient(grad);

                // Обновление моментов
//                m[i][j] = beta1 * m[i][j] + (1 - beta1) * grad;
//                v[i][j] = beta2 * v[i][j] + (1 - beta2) * grad * grad;
                float m = (1 - beta1) * grad;
                float v = (1 - beta2) * grad * grad;

                // Коррекция смещения
//                float mHat = m[i][j] / (1 - beta1);
//                float vHat = v[i][j] / (1 - beta2);
                float mHat = m / (1 - beta1);
                float vHat = v / (1 - beta2);

                // Регуляризация и обновление весов
                float regularizedGrad = grad + GazeTracker.l1Regularization(params[i][j]) + GazeTracker.l2Regularization(params[i][j]);
                params[i][j] -= (float) (GazeTracker.LEARNING_RATE * mHat / (Math.sqrt(vHat) + epsilon) + regularizedGrad);
            }
        }
    }

    private static float clipGradient(float grad) {
        return Math.max(-0.5f, Math.min(0.5f, grad));
    }
}

