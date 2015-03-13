package archer.test;

import archer.matrix.Matrix;

public class BinaryzationThreshold {
	public static double basic(double threshold){
		return threshold;
	}
	public static double otsu(Matrix tmp){
		Matrix H = histOf(tmp);
		int k;
		for(k=0; k<=255; k++){
			int total = 0;
			for(int t=-2; t<=2; t++){
				int q = k + t;
				if(q<0) q=0;
				if(q>255) q=255;
				total = total + (int)H.get(q, 1);
			}
			H.set(k, 1, (int)((double)total / 5.0 + 0.5));
		}
		double sum=0.0, csum=0.0, sb=0.0;
		int n=0;
		for(k=0; k<=255; k++){
			sum += k * H.get(k, 1);
			n += H.get(k, 1);
		}
		double fmax = -1.0;
		double threshValue = 1.0;
		int n1=0, n2=0;
		for(k=1; k<255; k++) {   //0和255不参与计算
			n1 += (int)H.get(k, 1);
			if(n1==0) continue;
			n2 = n - n1;
			if(n2==0) break;
			csum += k * H.get(k, 1);
			double m1 = csum / n1;
			double m2 = (sum - csum) / n2;
			sb = ((double)n1/n) * ((double)n2/n) * (m1 - m2) * (m1 - m2);
			if(sb > fmax){
				fmax = sb;
				threshValue = (double)k;
			}
		}
		return threshValue;
	}
	private static Matrix histOf(Matrix tmp) {
		Matrix H = new Matrix(256,2);
		
		for (int i=0; i<256; i++){
			H.set(i, 0, i);
		}
		int m = tmp.getRowDimension();
		int n = tmp.getColumnDimension();
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				int v = (int)Math.round(tmp.get(i, j));
				H.set(v, 1, H.get(v, 1)+1);
			}
		}
		return H;
	}

}
