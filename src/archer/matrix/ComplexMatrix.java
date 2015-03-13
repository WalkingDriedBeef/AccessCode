package archer.matrix;

import archer.matrix.util.Complex;

public class ComplexMatrix {
	
	int m;
	
	int n;
	
	double [][] real = null;
	
	double [][] image = null;
	
	public ComplexMatrix(Matrix R, Matrix I){
		this.checkMatrixDimensions(R, I);
		m = R.getRowDimension();
		n = R.getColumnDimension();
		real = R.getArray();
		image = I.getArray();
	}
	
	public ComplexMatrix(int m, int n){
		real = new double[m][n];
		image = new double[m][n];
	}
	
	public ComplexMatrix(double[][] real, double[][] image){
		this.checkArrayDimensions(real, image);
		m = real.length;
	    n = real[0].length;
		this.real = real;
		this.image = image;
	}
	
	public int getRowDimension(){
		return m;
	}
	
	public int getColumnDimension(){
		return n;
	}
	
	public ComplexMatrix transpose(){
		double[][] re = new double[n][m];
		double[][] im = new double[n][m];
		
		for(int i=0; i<n; i++){
			for(int j=0; j<m; j++){
				re[i][j] = real[j][i];
				im[i][j] = image[j][i];
			}
		}
		
		return new ComplexMatrix(re, im);
	}
	
	public Complex getComplexAt(int i, int j){
		return new Complex(real[i][j], image[i][j]); 
	}
	
	public void setComplexAt(int i, int j, Complex c){
		real[i][j] = c.real();
		image[i][j] = c.imag();
	}
	
	public void setReal(Matrix R){
		this.checkMatrixDimensions(R);
		real = R.getArray();
	}
	
	public void setImage(Matrix I){
		this.checkMatrixDimensions(I);
		image = I.getArray();
	}
	
	public void setReal(double[][] real){
		this.checkArrayDimensions(real);
		this.real = real;
	}
	
	public void setImage(double[][] image){
		this.checkArrayDimensions(image);
		this.image = image;
	}
	
	public Matrix getReal(){
		return new Matrix(real);
	}
	
	public Matrix getImage(){
		return new Matrix(image);
	}
	
	public ComplexMatrix conjugate(){
		ComplexMatrix C = new ComplexMatrix(real, image);
		for(int i=0; i<C.m; i++){
			for(int j=0; j<C.n; j++){
				C.image[i][j] = - C.image[i][j];
			}
		}
		return C;
	}
	
	public Matrix amplitude(){
		Matrix A = new Matrix(m, n);
		for(int i=0; i<m; i++){
			for(int j=0; j<n; j++){
				A.set(i, j, Math.sqrt(image[i][j]*image[i][j]+real[i][j]*real[i][j]));
			}
		}
		return A;
	}
	

	
	/**
	 *  Check if size(A) == size(B)
	 * @param A
	 * @param B
	 */

	private void checkMatrixDimensions(Matrix A, Matrix B) {
		if (B.getRowDimension() != A.getRowDimension()
				|| B.getColumnDimension() != A.getColumnDimension()) {
			throw new IllegalArgumentException("Matrix dimensions must agree.");
		}
	}
	
	private void checkMatrixDimensions(Matrix A) {
		if (m != A.getRowDimension()
				|| n != A.getColumnDimension()) {
			throw new IllegalArgumentException("Matrix dimensions must agree.");
		}
	}
	
	private void checkArrayDimensions(double[][] a, double[][] b) {
		if (b.length != a.length || b[0].length != a[0].length) {
			throw new IllegalArgumentException("Matrix dimensions must agree.");
		}
	}
	
	private void checkArrayDimensions(double[][] a) {
		if (m != a.length || n != a[0].length) {
			throw new IllegalArgumentException("Matrix dimensions must agree.");
		}
	}
}
