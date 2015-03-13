package archer.classifier;

import archer.matrix.Matrix;

public class Threshold {

	final static public int TH_MIN_ERROR = 1;
	final static public int TH_OTSU = 2;

	/**
	 * 
	 * @param M
	 * @param H
	 *            The histogram matrix of the data. the first column is the bin
	 *            value, the second one is the counted number of bin.
	 * @param method
	 *            The name of the thresholding method.
	 * @return
	 */
	static public double getThreshold(Matrix M, Matrix H, int method) {
		double th = -1;
		switch (method) {
		case TH_MIN_ERROR:
			th = th_min_error(H);
			break;
		case TH_OTSU:
			th = th_otsu(H);
		default:
			// throw out expection
		}

		return th;
	}

	/**
	 * Minimum error thresholding method, developed based on
	 * 
	 * @param H
	 * @return
	 * 
	 * @reference J. Kittler, J. Illingworth, Minimum error thresholding,
	 *            Pattern Recognition Volume 19, Issue 1, 1986, Pages 41�C47.
	 */
	static private double th_min_error(Matrix H) {
		double th = -1;
		int h = H.getRowDimension();
		double[][] A = new double[h][3];
		double a = 0;
		double b = 0;
		double c = 0;
		for (int i = 0; i < h; i++) {
			a = a + H.get(i, 1);
			A[i][0] = a;
			b = b + H.get(i, 0) * H.get(i, 1);
			A[i][1] = b;
			c = c + Math.pow(H.get(i, 0), 2) * H.get(i, 1);
			A[i][2] = c;
		}
		int k = 0;
		double dev = 0;
		for (int i = 0; i < h; i++) {
			double p = A[i][0] / a;
			double s = Math.sqrt(A[i][2] / A[i][0] - Math.pow(A[i][1] / A[i][0], 2)) / p;
			double t = Math.sqrt((c - A[i][2]) / (a - A[i][0]) - Math.pow((b - A[i][1]) / (a - A[i][0]), 2)) / (1 - p);
			double d = p * Math.log10(s) + (1 - p) * Math.log10(t);
			if (d == Double.NEGATIVE_INFINITY || Double.isNaN(d))
				continue;
			if (k == 0) {
				dev = d;
				k = i;
			} else {
				if (d < dev) {
					dev = d;
					k = i;
				}
			}
		}
		th = H.get(k, 0);
		return th;
	}
	
	
	/**
	 * Otsu 二值图像处理--最大类间方差法
	 * 
	 * 
	 */
	static private double th_otsu(Matrix H)
	{
		int k;
		for(k=0; k<=255; k++)
		{
			int total = 0;
			for(int t=-2; t<=2; t++)
			{
				int q = k + t;
				if(q<0) q=0;
				if(q>255) q=255;
				total = total + (int)H.get(q, 1);
			}
			H.set(k, 1, (int)((double)total / 5.0 + 0.5));
		}
		
		double sum=0.0, csum=0.0, sb=0.0;
		int n=0;
		for(k=0; k<=255; k++)
		{
			sum += k * H.get(k, 1);
			n += H.get(k, 1);
		}
		
		double fmax = -1.0;
		double threshValue = 1.0;
		int n1=0, n2=0;
		for(k=1; k<255; k++)    //0和255不参与计算
		{
			n1 += (int)H.get(k, 1);
			if(n1==0) continue;
			n2 = n - n1;
			if(n2==0) break;
			csum += k * H.get(k, 1);
			double m1 = csum / n1;
			double m2 = (sum - csum) / n2;
			sb = ((double)n1/n) * ((double)n2/n) * (m1 - m2) * (m1 - m2);
			if(sb > fmax)
			{
				fmax = sb;
				threshValue = (double)k;
			}
		}
		return threshValue;
	}
}
