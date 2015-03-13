package archer.image.accesscode;

import java.util.HashSet;
import java.util.Set;

import archer.classifier.Threshold;
import archer.image.io.ImageMatrix;
import archer.matrix.Matrix;

public class Preprocessor {
	private ImageMatrix IM = null;
	
	
	public Preprocessor(){
	}
	
	public Preprocessor(ImageMatrix IM){
		this.IM = IM;
	}

	public void setIM(ImageMatrix iM) {
		IM = iM;
	}

	public Matrix binaryzation(int method){
		Matrix B = new Matrix(IM.getHeight(), IM.getWidth());
		int chan = IM.getChannelSize();
		double[] th = new double[chan];
		for(int i=0; i<chan; i++) {
			Matrix H = IM.histOf(i);
			th[i] = Threshold.getThreshold(null, H, method);
		}

		for(int i=0; i<IM.getHeight(); i++) {
			for (int j=0; j<IM.getWidth();j++){
				boolean tag = true;
				for(int k=0; k<chan; k++){
					tag = tag && IM.getChannel(k).get(i, j)<th[k] ;
				}
				if (tag)
					B.set(i, j, 1);
			}
		}
//		B.show();
		return B;
	}
	
	public ImageMatrix convert2Gray() {
		ImageMatrix G = null;
		int chan = IM.getChannelSize();
		if (chan == 1) {
			return IM;
		} else if (chan == 3) {
			Matrix[] M = new Matrix[1];
			M[0] = IM.getChannel(0).times(0.114);
			M[0] = M[0].plus(IM.getChannel(1).times(0.587));
			M[0] = M[0].plus(IM.getChannel(2).times(0.299));
			G = new ImageMatrix(M);
		}
		//输出转换后的像素矩阵
//		System.out.println("----------------------------------灰度矩阵-----------------------------------------");
//		G.getChannel(0).show();
		
		return G;
	}

	
	
	
	
	
	
	
	//--------------------------------------------zhuhw----------------------------------------------
	
	//中值滤波
	public void eraseNoise() {
		int chan = IM.getChannelSize();
		if (chan == 1) {
			return;
		} else if (chan == 3) {
			Matrix R = weightAvgFilter(IM.getChannel(0));
			Matrix G = weightAvgFilter(IM.getChannel(1));
			Matrix B = weightAvgFilter(IM.getChannel(2));
			IM.setChannel(0, R);
			IM.setChannel(1, G);
            IM.setChannel(2, B);
		}
		return;
	}
	
	//灰度化-----取RGB颜色通道最小值
	public ImageMatrix convert2Gray2() {
		ImageMatrix Gray = null;
		int chan = IM.getChannelSize();
		if (chan == 1) {
			return IM;
		} else if (chan == 3) {
			int row = IM.getHeight();
			int col = IM.getWidth();
			Matrix[] M = new Matrix[1];
			Matrix tmp = new Matrix(row,col);
			
			Matrix R = IM.getChannel(0);
			Matrix G = IM.getChannel(1);
			Matrix B = IM.getChannel(2);
			for(int i=0; i<row; i++)
			{
				for(int j=0; j<col; j++)
				{
					double gray = selectMin(R.get(i, j),G.get(i, j),B.get(i, j));
					tmp.set(i, j, gray);
				}
			}
			M[0] = tmp;
			Gray = new ImageMatrix(M);
		}
		//输出转换后的像素矩阵
		System.out.println("----------------------取RGB颜色通道最小值灰度化矩阵--------------------------------------");
		Gray.getChannel(0).show();
		
		return Gray;
	}
	
	//取RGB颜色通道平均值
	public ImageMatrix convert2Gray3() {
		ImageMatrix G = null;
		int chan = IM.getChannelSize();
		if (chan == 1) {
			return IM;
		} else if (chan == 3) {
			Matrix[] M = new Matrix[1];
			M[0] = IM.getChannel(0).times(0.333);
			M[0] = M[0].plus(IM.getChannel(1).times(0.333));
			M[0] = M[0].plus(IM.getChannel(2)).times(0.334);
			G = new ImageMatrix(M);
		}
		//输出转换后的像素矩阵
		System.out.println("----------------------取RGB颜色通道平均值灰度化矩阵--------------------------------------");
		G.getChannel(0).show();
		
		return G;
	}
	
	private double selectMin(double a, double b, double c)
	{
		double tmp;
		if(a > b)
			tmp = b;
		else
			tmp = a;
		if(tmp > c)
			return c;
		else
			return tmp;
	}
	
	//加权均值滤波
	public Matrix weightAvgFilter(Matrix matrix)
	{
		int row = matrix.getRowDimension();
		int col = matrix.getColumnDimension();
		
		Matrix tmp = new Matrix(row,col);
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				double filteredValue = 0;
				
				for(int s=-1; s<2; s++)
				{
					int tmp_row = r + s;
					if(tmp_row < 0 || tmp_row >= row)
						continue;
					
					for(int t=-1; t<2; t++)
					{
						int tmp_col = c + t;
						if(tmp_col < 0 || tmp_col >= col)
							continue;
						
						double weight = 1.0 / 16;
						if(tmp_row == r || tmp_col == c) 
							weight = 2.0 / 16;
						if(tmp_row == r && tmp_col == c) 
							weight = 4.0 / 16;
						double nowValue = matrix.get(tmp_row, tmp_col);
						nowValue =nowValue * weight;
						filteredValue += nowValue;
					}
				}
				tmp.set(r, c, filteredValue);
			}
		}
		System.out.println("--------------------------加权均值滤波 去噪后矩阵-----------------------------");
		tmp.show();
		return tmp;
	}
	
	//均值滤波
	public Matrix avgFilter(Matrix matrix)
	{
		int row = matrix.getRowDimension();
		int col = matrix.getColumnDimension();
		
		Matrix tmp = new Matrix(row,col);
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				double filteredValue = 0;
				
				for(int s=-1; s<2; s++)
				{
					int tmp_row = r + s;
					if(tmp_row < 0 || tmp_row >= row)
						continue;
					
					for(int t=-1; t<2; t++)
					{
						int tmp_col = c + t;
						if(tmp_col < 0 || tmp_col >= col)
							continue;
						
						double weight = 1.0 / 9;
						double nowValue = matrix.get(tmp_row, tmp_col);
						nowValue =nowValue * weight;
						filteredValue += nowValue;
					}
				}
				tmp.set(r, c, filteredValue);
			}
		}
		System.out.println("--------------------------均值滤波去噪后矩阵-----------------------------");
		tmp.show();
		return tmp;
	}
	
	//中值滤波
	public Matrix middleFilter(Matrix matrix)
	{
		int row = matrix.getRowDimension();
		int col = matrix.getColumnDimension();
		
		Matrix tmp = new Matrix(row,col);
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				double middleValue;
				Set<Double> zoneValues = new HashSet<Double>();
				for(int s=-1; s<2; s++)
				{
					int tmp_row = r + s;
					if(tmp_row < 0 || tmp_row >= row)
						continue;
					for(int t=-1; t<2; t++)
					{
						int tmp_col = c + t;
						if(tmp_col < 0 || tmp_col >= col)
							continue;
						
						double nowValue = matrix.get(tmp_row, tmp_col);
						zoneValues.add(nowValue);
					}
				}
				middleValue = getMiddleValue(zoneValues);
				tmp.set(r, c, middleValue);
			}
		}
		return tmp;
	}
	
	//阈值滤波
	public Matrix thresholdFilter(Matrix matrix)
	{
		int threshold = 2;
		int row = matrix.getRowDimension();
		int col = matrix.getColumnDimension();
		
		Matrix tmp = new Matrix(row,col);
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				int valueCount = 0;
				double nowValue = matrix.get(r, c);
				if(nowValue == 0.0)
				{
					tmp.set(r, c, nowValue);
					continue;
				}
				
				for(int s=-1; s<2; s++)
				{
					int tmpRow = r + s;
					if(tmpRow < 0 || tmpRow >= row)
						continue;
					for(int t=-1; t<2; t++)
					{
						int tmpCol = c + t;
						if(tmpCol < 0 || tmpCol >= col)
							continue;
						
						if(tmpRow==r && tmpCol==c)
							continue;
						
						double tmpValue = matrix.get(tmpRow, tmpCol);
						if(tmpValue == 1.0)
							valueCount++;
					}
				}
				
				if(valueCount <= threshold)
					nowValue = 0.0;
				tmp.set(r, c, nowValue);
			}
		}
		System.out.println("--------------------------去噪后矩阵-----------------------------");
		tmp.show();
		return tmp;
	}
	
	private double getMiddleValue(Set<Double> valueSet)
	{
		double tmp = 0;
		int size = valueSet.size();
		double valueArr[] = new double[size];
		int i=0;
		for(Double d : valueSet)
		{
			valueArr[i] = d;
			i++;
		}
		valueArr = insertSort(valueArr);
		if(size % 2 == 0)
			tmp = (valueArr[(size-1)/2] + valueArr[size/2]) / 2;
		else
			tmp = valueArr[size/2];
		return tmp;
	}
	
	private double[] insertSort(double originArr[])
	{
		int i,j;
		int size = originArr.length;
		for(i=1; i<size; i++)
		{
			if(originArr[i-1] > originArr[i])
			{
				double tmp = originArr[i];
				j=i;
				while(j>0 && originArr[j-1] > tmp)
				{
					originArr[j] = originArr[j-1];
					j--;
				}
				originArr[j] = tmp;
			}
		}
		return originArr;
	}
	
	//八连通区域腐蚀法
	public Matrix erodeImg8(Matrix binaryMatrix)
	{
		Matrix templateM = new Matrix(3,3,1);
		int row = binaryMatrix.getRowDimension();
		int col = binaryMatrix.getColumnDimension();
		Matrix newBinaryMatrix = new Matrix(row,col,0);
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				Matrix nowMatrix = new Matrix(3, 3, 0);
				nowMatrix = binaryMatrix.getMatrix(r-1, r+1, c-1, c+1);
				Matrix dotMultiply = nowMatrix.arrayTimes(templateM);
				double sum = dotMultiply.sum();
				if(sum < 6)
					newBinaryMatrix.set(r, c, 0);
				else
					newBinaryMatrix.set(r, c, 1);
			}
		}
		
		return newBinaryMatrix;
	}
	

	//八连通区域膨胀法
	public Matrix dilateImg8(Matrix binaryMatrix)
	{
		Matrix templateM = new Matrix(3,3,1);
		int row = binaryMatrix.getRowDimension();
		int col = binaryMatrix.getColumnDimension();
		Matrix newBinaryMatrix = new Matrix(row,col,0);
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				Matrix nowMatrix = new Matrix(3, 3, 0);
				nowMatrix = binaryMatrix.getMatrix(r-1, r+1, c-1, c+1);
				Matrix dotMultiply = nowMatrix.arrayTimes(templateM);
				double sum = dotMultiply.sum();
				if(sum > 0)
					newBinaryMatrix.set(r, c, 1);
				else
					newBinaryMatrix.set(r, c, 0);
			}
		}
		
		return newBinaryMatrix;
	}
	
	//四连通区域腐蚀法
	public Matrix erodeImg4(Matrix binaryMatrix)
	{
		double[][] templateArr = {{1,1,1},{1,1,1},{1,1,1}};
		Matrix templateM = new Matrix(templateArr);
		int row = binaryMatrix.getRowDimension();
		int col = binaryMatrix.getColumnDimension();
		Matrix newBinaryMatrix = new Matrix(row,col,0);
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				Matrix nowMatrix = new Matrix(3, 3, 0);
				nowMatrix = binaryMatrix.getMatrix(r-1, r+1, c-1, c+1);
				Matrix dotMultiply = nowMatrix.arrayTimes(templateM);
				double sum = dotMultiply.sum();
				if(sum < 2)
					newBinaryMatrix.set(r, c, 0);
				else
					newBinaryMatrix.set(r, c, 1);
			}
		}
		
		return newBinaryMatrix;
	}

	//四连通区域膨胀法
	public Matrix dilateImg4(Matrix binaryMatrix)
	{
		double[][] templateArr = {{1,1,1},{1,1,1},{1,1,1}};
		Matrix templateM = new Matrix(templateArr);
		int row = binaryMatrix.getRowDimension();
		int col = binaryMatrix.getColumnDimension();
		Matrix newBinaryMatrix = new Matrix(row,col,0);
		for(int r=0; r<row; r++)
		{
			for(int c=0; c<col; c++)
			{
				double nowV = binaryMatrix.get(r, c);
				if(nowV == 1.0)
				{
					newBinaryMatrix.set(r, c, 1);
					continue;
				}
				Matrix nowMatrix = new Matrix(3, 3, 0);
				nowMatrix = binaryMatrix.getMatrix(r-1, r+1, c-1, c+1);
				Matrix dotMultiply = nowMatrix.arrayTimes(templateM);
				double sum = dotMultiply.sum();
				if(sum > 0)
					newBinaryMatrix.set(r, c, 1);
				else
					newBinaryMatrix.set(r, c, 0);
			}
		}
		
		return newBinaryMatrix;
	}
	
	//分段线性变换
	public Matrix partLinearTransfer(Matrix binaryMatrix, double threshold, double lamda)
	{
		int row = binaryMatrix.getRowDimension();
		int col = binaryMatrix.getColumnDimension();
		
		double low = 255;
		double hight = threshold;
		
		int i,j;
		for(i=0; i<row; i++)
		{
			for(j=0; j<col; j++)
			{
				double nowValue = binaryMatrix.get(i, j);
				if(low > nowValue)
					low = nowValue;
			}
		}
		
		low += 10;
		double midIndex = (hight + low) / 2.0;
		double mid = (hight - low) / (1 + lamda) + low;
		
		Matrix transferedM = new Matrix(row,col);
//		for(i=0; i<row; i++)
//		{
//			for(j=0; j<col; j++)
//			{
//				double nowValue = binaryMatrix.get(i,j);
//				if(nowValue < lowValue)
//					tmpValue = 0.0;
//				else if(nowValue>=lowValue && nowValue<midIndex)
//					tmpValue = (128-0.0)/(mid - lowValue) * (nowValue - lowValue);
//				else if(nowValue>=midIndex && nowValue<=hightValue)
//					tmpValue = (255.0-128.0)/(hightValue - mid) * (nowValue - mid) + 128.0;
//				else
//					tmpValue = 255.0;
//				transferedM.set(i, j, tmpValue);
//			}
//		}
//		return transferedM;
//	}
		
		for(i=0; i<row; i++)
		{
			for(j=0; j<col; j++)
			{
				double nowValue = binaryMatrix.get(i,j);
				double tmpValue = 0.0;

				if(nowValue>=low && nowValue<mid)
					tmpValue = (128-0.0)/(mid - low) * (nowValue - low);
				else if(nowValue>=mid && nowValue<=hight)
					tmpValue = (255.0-128.0)/(hight - mid) * (nowValue - mid) + 128.0;
				
				if(tmpValue > hight)
					tmpValue = 255.0;
				if(tmpValue>=0 && tmpValue<low)
					tmpValue = 0.0;
				transferedM.set(i, j, tmpValue);
			}
		}
		return transferedM;
	}
	
	//分段线性变换后二次灰度化
	public Matrix secondBinaryzation(int method, double lamda){
		Matrix B = new Matrix(IM.getHeight(), IM.getWidth());
		int chan = IM.getChannelSize();
		double[] th = new double[chan];
		for(int i=0; i<chan; i++) {
			Matrix H = IM.histOf(i);
			th[i] = Threshold.getThreshold(null, H, method);
		}
		
		Matrix partLinearTransferedMatrix = partLinearTransfer(IM.getChannel(0),th[0],lamda);
		
		System.out.println("-------------------------------分段线性变换后的灰度矩阵-----------------------------------");
		partLinearTransferedMatrix.show();
		
		IM.setChannel(0, partLinearTransferedMatrix);
		for(int i=0; i<chan; i++)
		{
			Matrix H = IM.histOf(i);
			th[i] = Threshold.getThreshold(null, H, method);
		}
		
		for(int i=0; i<IM.getHeight(); i++) {
			for (int j=0; j<IM.getWidth();j++){
				boolean tag = true;
				for(int k=0; k<chan; k++){
					tag = tag && IM.getChannel(k).get(i, j)<th[k] ;
				}
				if (tag)
					B.set(i, j, 1);
			}
		}
		B.show();
		return B;
	}
	
	/*
	 * 双线性内插值算法，用于缩放图像
	 * 公式：
	 * srcX=dstX* (srcWidth/dstWidth) 
	 * srcY = dstY * (srcHeight/dstHeight)
	 * 
	 * 参数解释：
	 * 目标图像的宽、高：dstWidth、dstHeight
	 * 目标图坐标（destX,destY）   源图坐标（srcX,srcY）
	 * 
	 * f(i+u,j+v) = (1-u)(1-v)f(i,j) + (1-u)vf(i,j+1) + u(1-v)f(i+1,j) + uvf(i+1,j+1)
	 * 
	 */
	
	public Matrix magnifyShrinkImg(Matrix originMatrix, int targetRow, int targetCol)
	{
		Matrix resultM = new Matrix(targetRow, targetCol, 0);
		
		int row = originMatrix.getRowDimension();
		int col = originMatrix.getColumnDimension();
		
		double rowRate = (double)row / targetRow;
		double colRate = (double)col / targetCol;
		
		for(int i=0; i<targetRow; i++)
		{
			for(int j=0; j<targetCol; j++)
			{
				double srcX = i * rowRate;
				double srcY = j * colRate;
				int ii = (int)srcX;
				int jj = (int)srcY;
				double xWeight = srcX - ii;
				double yWeight = srcY - jj;
				if(ii+1 >= row && jj+1 < col)
				{
					double nowV = (1-xWeight)*(1-yWeight)*originMatrix.get(ii, jj) + (1-xWeight)*yWeight*originMatrix.get(ii, jj+1);
					nowV = Math.rint(nowV);
//					nowV = nowV > 0.0 ? 1.0 : 0.0;
					resultM.set(i, j, nowV);
					continue;
				}
				else if(ii+1 < row && jj+1 >= col)
				{
					double nowV = (1-xWeight)*(1-yWeight)*originMatrix.get(ii, jj) + xWeight*(1-yWeight)*originMatrix.get(ii+1, jj);
					nowV = Math.rint(nowV);
//					nowV = nowV > 0.0 ? 1.0 : 0.0;
					resultM.set(i, j, nowV);
					continue;
				}
				else if(ii+1 >= row && jj+1 >= col)
				{
					double nowV = (1-xWeight)*(1-yWeight)*originMatrix.get(ii, jj);
					nowV = Math.rint(nowV);
//					nowV = nowV > 0.0 ? 1.0 : 0.0;
					resultM.set(i, j, nowV);
					continue;
				}
				else
				{
					double nowV = (1 - xWeight) * (1 - yWeight) * originMatrix.get(ii, jj) 
							+ (1 - xWeight) * yWeight * originMatrix.get(ii, jj + 1) 
							+ xWeight * (1 - yWeight) * originMatrix.get(ii + 1, jj)
							+ xWeight * yWeight * originMatrix.get(ii + 1, jj + 1);
					nowV = Math.rint(nowV);
//					nowV = nowV > 0.0 ? 1.0 : 0.0;
					resultM.set(i, j, nowV);
				}
			}
		}
		return resultM;
	}
	
	
	
	/*
	 * 图像旋转，将图像中的字符转正
	 * 变换公式为(旋转角度为A，顺时针方向为正值，逆时针方向为负值)：
	 * x'=(x-w/2)*cosA + (-y+h/2)*sinA + h/2
	 * y'=-[-(x-w/2)*sinA + (-y+h/2)*cosA - h/2]
	 * 
	 */
	
	public Matrix rotateImg(Matrix originMatrix, int angle)
	{
		int height = originMatrix.getRowDimension();
		int width = originMatrix.getColumnDimension();
		
		double sinV = Math.sin((double)angle / 180 * Math.PI);
		double cosV = Math.cos((double)angle / 180 * Math.PI);
		
		double sinSupplementaryV = Math.sin((double)(360-angle) / 180 * Math.PI);
		double cosSupplementaryV = Math.cos((double)(360-angle) / 180 * Math.PI);
		
		//以图像中心为原点左上角,右上角,左下角和右下角的坐标,用于计算旋转后的图像的宽和高
		double plt_x = -(double)width / 2, plt_y = (double)height / 2;
		double prt_x = (double)width / 2, prt_y = (double)height / 2;
		double plb_x = -(double)width / 2, plb_y = -(double)height / 2;
		double prb_x = (double)width / 2, prb_y = -(double)height / 2;
		
		//旋转之后四个顶点的坐标
		double plt_xn,plt_yn,prt_xn,prt_yn,plb_xn,plb_yn,prb_xn,prb_yn;
		plt_xn = plt_x * cosV + plt_y * sinV;
		plt_yn = -plt_x * sinV + plt_y * cosV;
		prt_xn = prt_x * cosV + prt_y * sinV;
		prt_yn = -prt_x * sinV + prt_y * cosV;
		plb_xn = plb_x * cosV + plb_y * sinV;
		plb_yn = -plb_x * sinV + plb_y * cosV;
		prb_xn = prb_x * cosV + prb_y * sinV;
		prb_yn = -prb_x * sinV + prb_y * cosV;		
		
		int destHeight = (int)Math.max(Math.abs(prb_yn - plt_yn), Math.abs(plb_yn - prt_yn));
		int destWidth = (int)Math.max(Math.abs(prb_xn - plt_xn), Math.abs(plb_xn - prt_xn));
		
		Matrix rotatedM = new Matrix(destHeight,destWidth);
		
		for(int j=0; j<width; j++)
		{
			for(int i=0; i<height; i++)
			{
				int x1 = (int)((j - (double)width/2) * cosV + (-i + (double)height/2) * sinV + (double)destWidth/2);
				int y1 = -(int)(-(j - (double)width/2) * sinV + (-i + (double)height/2) * cosV - (double)destHeight/2);
				if(x1 < 0 || y1 < 0 || x1 >= destWidth || y1 >=destHeight )
					continue;
				rotatedM.set(y1, x1, originMatrix.get(i, j));
			}
		}
		
//		for(int i=0; i<destHeight; i++)
//		{
//			for(int j=0; j<destWidth; j++)
//			{
//				int srcX = (int)((j - destWidth/2) * cosSupplementaryV + (-i + destHeight/2) * sinSupplementaryV);
//				int srcY = (int)(-(j - destWidth/2) * sinSupplementaryV + (-i + destHeight/2) * cosSupplementaryV);
//				if(srcX <= -width/2 || srcX >= width/2  || srcY <= -height/2 || srcY >= height/2)
//				{
//					continue;
//				}
//				int srcXN = srcX + width/2;
//				int srcYN = Math.abs(srcY - height/2);
//				rotatedM.set(i, j, originMatrix.get(srcYN, srcXN));
//			}
//		}
		CharSpliter spliter = new CharSpliter(rotatedM);
		rotatedM = spliter.getImgMatrix(rotatedM);

		return rotatedM;
	}
	
	
	
	//----------------------zhuhw edit end-------------------------------------------
	public Matrix crossline(Matrix B) {
		Matrix T = B.copy();
		
		
		int m = T.getRowDimension();
		int n = T.getColumnDimension();
		
		// detect line return line end points and slope
		int [] pick = new int[1];
		pick[0] = 0;
		int r1 = getEnd(T.getMatrix(0, m-1, pick));
		pick[0] = n-1;
		int r2 = getEnd(T.getMatrix(0, m-1, pick));
		if (r1==-1 || r2==-1)
			return T;
		
		T.set(r1,0,0);
		T.set(r2, n-1, 0);
		double k = (r2-r1)/(double)n;
		for(int j=1; j<n-1; j++){
			int x = (int) Math.round(r1+(j-1)*k);
			int d0 = (int) Math.round(r1+(j-2)*k)-x;
			int d1 = (int) Math.round(r1+j*k)-x;
			
			int a = (x-1)<1?1:x-1;
			int b = (x+1)>(m-1)?m-1:x+1;
			
			Matrix M = new Matrix(b-a+1,3);
			M.fill(0, b-a, 0, 2, 1);
			if (d0+1>b-a)
				M.set(b-a, 0, 0);
			else
				M.set(d0+1, 0, 0);
			if (b-a==0)
				M.set(0, 1, 0);
			else
				M.set(1, 1, 0);
			if (d1+1>b-a)
				M.set(b-a, 2, 0);
			else
				M.set(d1+1, 2, 0);
			
			Matrix F = T.getMatrix(a, b, j-1, j+1);
			
			M = M.arrayTimes(F);
			if (M.sum()==0)
				T.set(x, j, 0);
		}
		return T;
	}
	
	private int getEnd(Matrix B){
		int y = -1;
		Matrix G = new Matrix(B.getRowDimension()+2, 1);
		int[] c =new int[1];
		c[0]=0;
		G.setMatrix(1, G.getRowDimension()-2, c, B);
		
		for (int i=1; i<G.getRowDimension()-1; i++) {
			if (G.get(i, 0)==1) {
				double g = G.get(i-1, 0) + G.get(i, 0) + G.get(i+1, 0);
				if (g==1)
					return i-1;
				
			}
		}
		return y;
	}
	
	public Matrix preprocess() {
		
		Matrix B = binaryzation(Threshold.TH_OTSU);
		B = crossline(B);
		return B;
	}

}
