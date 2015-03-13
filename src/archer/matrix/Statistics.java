package archer.matrix;

//import org.archer.matrix.visual.XYAreaChart;

public class Statistics {

	/**
	 * Calculate the property density function of matrix. Each element is
	 * treated as a sample. The bin range is decided by the number of bins. The
	 * middle value of the bin are recoded in the first column of histogram
	 * matrix. The second column contains the count number.
	 * 
	 * @param M
	 * @return
	 * @throws InvalidateArgumentException
	 */
	public static Matrix pdf(Matrix M, int bins)
			throws InvalidateArgumentException {
		if (bins <= 0)
			throw new InvalidateArgumentException("");
		double m = M.getRowDimension();
		double n = M.getColumnDimension();

		Matrix P = new Matrix(bins, 2);
		double min = M.min();
		double max = M.max();
		double space = (max - min) / (double) (bins - 1);
		double r = space / 2;
		for (int b = 0; b < bins; b++) {
			double d = min + b * space;
			P.set(b, 0, d);
			double count = 0;
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					if (M.get(i, j) > d - r && M.get(i, j) <= d + r)
						count++;
				}
			}
			P.set(b, 1, count / (m * n));
		}
		return P;
	}

	/**
	 * Calculate the histogram of matrix. Each element is treated as a sample.
	 * The bin range is decided by the number of bins. The middle value of the
	 * bin are recoded in the first column of histogram matrix. The second
	 * column contains the count number.
	 * 
	 * @param M
	 * @return
	 * @throws InvalidateArgumentException
	 */
	public static Matrix hist(Matrix M, int bins)
			throws InvalidateArgumentException {
		if (bins <= 0)
			throw new InvalidateArgumentException("");
		int m = M.getRowDimension();
		int n = M.getColumnDimension();

		Matrix H = new Matrix(bins, 2);
		double min = M.min();
		double max = M.max();
		double space = (max - min) / (double) (bins - 1);
		double r = space / 2;
		for (int b = 0; b < bins; b++) {
			double d = min + b * space;
			H.set(b, 0, d);
			int count = 0;
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					if (M.get(i, j) > d - r && M.get(i, j) <= d + r)
						count++;
				}
			}
			H.set(b, 1, count);
		}
		return H;
	}

	public static double mean(Matrix M){
		return M.sum()/(M.getRowDimension()*M.getColumnDimension());
	}
	
	public static double variances(Matrix M){
		return 0;
	}
}
