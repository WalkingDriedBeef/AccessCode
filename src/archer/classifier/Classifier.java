package archer.classifier;

import archer.matrix.Matrix;

public interface Classifier {
	abstract public Matrix classify(Matrix feature);
	
	abstract public int getLabelSize();
}
