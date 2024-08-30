public class AdamOptimizer2 {
    private float beta1;
    private float beta2;
    private float epsilon;
    private float[][] m;
    private float[][] v;

    public AdamOptimizer2(int numRows, int numCols, float beta1, float beta2, float epsilon) {
        this.beta1 = beta1;
        this.beta2 = beta2;
        this.epsilon = epsilon;
        this.m = new float[numRows][numCols];
        this.v = new float[numRows][numCols];
    }


    public void updateHiddenToInput(float[][] params, float[] grads) {
        for (int i = 0; i < params[i].length; i++) {
            for (int j = 0; j < params[j].length; j++) {

                float grad = grads[j];

                if (grad > 0.00005 || grad < -1.0) {
                    grad = clipGradient(grad);
                }

                m[i][j] = beta1 * m[i][j] + (1 - beta1) * grad;
                v[i][j] = beta2 * v[i][j] + (1 - beta2) * grad * grad;
                float mHat = m[i][j] / (1 - beta1);
                float vHat = v[i][j] / (1 - beta2);
                float regularizedGrad = grad + GazeTracker.l1Regularization(params[i][j]) + GazeTracker.l2Regularization(params[i][j]);
                params[i][j] -= (float) (GazeTracker.LEARNING_RATE * mHat / (Math.sqrt(vHat) + epsilon) + regularizedGrad);
            }
        }
    }

    public void updateOutputToHidden(float[][] params, float[] grads) {
        for (int i = 0; i < params[i].length; i++) {
            for (int j = 0; j < params[j].length; j++) {

                float grad = grads[j];

                if (grad > 0.00005 || grad < -1.0) {
                    grad = clipGradient(grad);
                }

                m[i][j] = beta1 * m[i][j] + (1 - beta1) * grad;
                v[i][j] = beta2 * v[i][j] + (1 - beta2) * grad * grad;
                float mHat = m[i][j] / (1 - beta1);
                float vHat = v[i][j] / (1 - beta2);
                float regularizedGrad = grad + GazeTracker.l1Regularization(params[i][j]) + GazeTracker.l2Regularization(params[i][j]);
                params[i][j] -= (float) (GazeTracker.LEARNING_RATE * mHat / (Math.sqrt(vHat) + epsilon) + regularizedGrad);
            }
        }
    }

    private static float clipGradient(float grad) {
        return Math.max((float) -0.5, Math.min((float) 0.000005, grad));
    }
}
