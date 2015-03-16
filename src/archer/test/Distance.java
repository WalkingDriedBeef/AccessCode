package archer.test;

public class Distance {
	/*
	 * 欧氏距离
	 * 二维：0ρ = sqrt( (x1-x2)^2+(y1-y2)^2 )　|x| = √( x2 + y2 )
	 * 三维：0ρ = √( (x1-x2)2+(y1-y2)2+(z1-z2)2 )　|x| = √( x2 + y2 + z2 )
	 */
	public static double euclideanMetric(int[] a, int[] b){
		if(null == a || null == b || a.length != b.length || a.length == 0 || b.length == 0)return Double.MAX_VALUE;
		double dis = 0.0;
		for (int i = 0; i < a.length; i++) {
			dis += Math.pow(a[i] - b[i], 2);
		}
		return Math.sqrt(dis);
	}
	public static double euclideanMetric(double[] a, double[] b){
		if(null == a || null == b || a.length != b.length || a.length == 0 || b.length == 0)return Double.MAX_VALUE;
		double dis = 0.0;
		for (int i = 0; i < a.length; i++) {
			dis += Math.pow(a[i] - b[i], 2);
		}
		return Math.sqrt(dis);
	}
	/*
	 * cosine similarity
	 * 二维：cosine = (A1*B1 + A2*B2 + ... + An*Bn)/(√(A1^2 + ... + An^2) * √(B1^2 + ... + Bn^2))
	 */
	public static double cosineDis(double[] a, double[] b){
		if(null == a || null == b || a.length != b.length || a.length == 0 || b.length == 0)return Double.MAX_VALUE;
		double numerator = 1.0;
		double denominator = 1.0;
		double xa = 1.0;
		double xb = 1.0;
		for (int i = 0; i < a.length; i++) {
			numerator += a[i] * b[i];
			xa += Math.pow(a[i], 2);
			xb += Math.pow(b[i], 2);
		}
		denominator = Math.sqrt(xa) * Math.sqrt(xb);
		if(denominator == 0){
			return 0;
		}
//		System.out.println(1.0 - numerator / denominator);
		return 1.0 - numerator / denominator;
	}
	public static double cosineDis(int[] a, int[] b){
		if(null == a || null == b || a.length != b.length || a.length == 0 || b.length == 0)return Double.MAX_VALUE;
		double numerator = 1.0;
		double denominator = 1.0;
		double xa = 1.0;
		double xb = 1.0;
		for (int i = 0; i < a.length; i++) {
			numerator += a[i] * b[i];
			xa += Math.pow(a[i], 2);
			xb += Math.pow(b[i], 2);
		}
		denominator = Math.sqrt(xa) * Math.sqrt(xb);
		if(denominator == 0){
			return 0;
		}
//		System.out.println(1.0 - numerator / denominator);
		return 1.0 - numerator / denominator;
	}
}
