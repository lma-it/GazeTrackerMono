public class AdamOptimizer {
    private float learningRate;
    private float beta1;
    private float beta2;
    private float epsilon;
    private float[][] m;
    private float[][] v;

    public AdamOptimizer(int numRows, int numCols, float learningRate, float beta1, float beta2, float epsilon) {
        this.learningRate = learningRate;
        this.beta1 = beta1;
        this.beta2 = beta2;
        this.epsilon = epsilon;
        this.m = new float[numRows][numCols];
        this.v = new float[numRows][numCols];
    }


    public void updateHiddenToInput(float[][] params, float[] grads) {
        for (int i = 0; i < params.length; i++) {
            for (int j = 0; j < grads.length; j++) {
                m[i][j] = beta1 * m[i][j] + (1 - beta1) * grads[j];
                v[i][j] = beta2 * v[i][j] + (1 - beta2) * grads[j] * grads[j];
                float mHat = m[i][j] / (1 - beta1);
                float vHat = v[i][j] / (1 - beta2);
                params[i][j] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon) + GazeTracker.l1Regularization(params[i][j]) + GazeTracker.l2Regularization(params[i][j]);
            }
        }
    }

    public void updateOutputToHidden(float[][] params, float[] grads) {
        for (int i = 0; i < params.length; i++) {
            for (int j = 0; j < grads.length; j++) {
                m[i][j] = beta1 * m[i][j] + (1 - beta1) * grads[0];
                v[i][j] = beta2 * v[i][j] + (1 - beta2) * grads[0] * grads[0];
                float mHat = m[i][j] / (1 - beta1);
                float vHat = v[i][j] / (1 - beta2);
                params[i][j] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon) + GazeTracker.l1Regularization(params[i][j]) + GazeTracker.l2Regularization(params[i][j]);
            }
        }
    }
}
