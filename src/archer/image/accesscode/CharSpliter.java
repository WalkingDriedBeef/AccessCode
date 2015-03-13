package archer.image.accesscode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import archer.matrix.Matrix;

public class CharSpliter {
	
	public static final int DMAX = 15;
	public static final int DMIN = 9;
	public static final int DMEAN = 10;
	
	public static final int XPOSI = 0;
	public static final int YPOSI = 1;
	
	private Matrix binaryMatrix;
	
	public CharSpliter(Matrix binaryMatrix)
	{
		this.binaryMatrix = binaryMatrix;
	}
	
	public void setMatrix(Matrix bMatrix)
	{
		this.binaryMatrix = bMatrix;
	}
	
	/*
	 * 滴水分割方法
	 * 
	 */
	public List<Matrix> charSplitByDropWater()
	{
		int i;
		int origRow = binaryMatrix.getRowDimension();
		int origCol = binaryMatrix.getColumnDimension();
		
		int stickedCharNum = Math.round(origCol / DMEAN);
		List<Matrix> splitResult = new ArrayList<Matrix>();
		
		Matrix colSum = binaryMatrix.sumDim(Matrix.MATRIX_COL_DIM);
		double colShadow[] = colSum.getArrayCopy()[0];
		ArrayList<Integer> extremePoints = getExtremePoint(colShadow);
		
		List<int[]> previousSplitRoute = new ArrayList<int[]>();
		List<int[]> endRoute = new ArrayList<int[]>();
		for(i=0; i<origRow; i++)
		{
			int route[] = {i,0};
			previousSplitRoute.add(route);
			
			int route2[] = {i,origCol};
			endRoute.add(route2);
		}
		int previousExtremePoint = 0;
		int lastSplitePointOrder = 0;
		Matrix splitedMatrixByRoute = null;
		for(i=0; i<extremePoints.size(); i++)
		{
			int nowExtremePos = extremePoints.get(i);
			int distance = nowExtremePos - previousExtremePoint;
			if (distance <= DMAX
					&& distance >= DMIN
					&& ((lastSplitePointOrder + 1) <= (stickedCharNum -1))
					&& ((origCol - nowExtremePos) <= (stickedCharNum - (lastSplitePointOrder+1)) * DMAX)
					&& ((origCol - nowExtremePos) >= (stickedCharNum - (lastSplitePointOrder+1)) * DMIN))
			{
				List<int[]> nowSplitRoute = dropWater(nowExtremePos);
				
				splitedMatrixByRoute = splitMatrixByRoute(previousSplitRoute, nowSplitRoute);
				splitResult.add(splitedMatrixByRoute);
				
				previousExtremePoint = nowExtremePos;
				previousSplitRoute = nowSplitRoute;
				lastSplitePointOrder += 1;
			}
		}
		splitedMatrixByRoute = splitMatrixByRoute(previousSplitRoute,endRoute);
		splitResult.add(splitedMatrixByRoute);
		
		return splitResult;
	}
	
	/*
	 * 根据两条路径将矩阵分成两部分，每条路径中的x坐标是从0逐渐递增到row-1，y坐标是变化的
	 */
	public Matrix splitMatrixByRoute(List<int[]> previousRoute, List<int[]> nowRoute)
	{
		int row = binaryMatrix.getRowDimension();
		int col = binaryMatrix.getColumnDimension();
		
		int leftY = col, rightY = 0, leftY2 = col;
		for(int i=0; i<row; i++)
		{
			int previousPoint[] = previousRoute.get(i);
			int nowPoint[] = nowRoute.get(i);
		
			if(previousPoint[YPOSI] < leftY)
				leftY = previousPoint[YPOSI];
			if(nowPoint[YPOSI] > rightY)
				rightY = nowPoint[YPOSI];
			if(nowPoint[YPOSI] < leftY2)
				leftY2 = nowPoint[YPOSI];
		}
		
		int splitedMatrixWidth = rightY - leftY + 1;
		Matrix splitedMatrix = new Matrix(row, splitedMatrixWidth, 0.0);
		
		for(int i=0; i<row; i++)
		{
			int startY = previousRoute.get(i)[YPOSI];
			int endY = nowRoute.get(i)[YPOSI];
			for(int j=0; j<col; j++)
			{
				if(j>startY && j<=endY)
				{
					splitedMatrix.set(i, j-leftY, binaryMatrix.get(i, j));
				}
			}
		}
		
		return splitedMatrix;
	}
	
	/*
	 * 滴水算法，求解图像分割路径
	 * 								|(Xi,Yi-1)		Wi=1
	 * 								|(Xi,Yi+1) 		Wi=2
	 * T(Xi+1,Yi+1) = f(Xi,Yi,Wi) = |(Xi+1,Yi+1)	Wi=3
	 *  							|(Xi+1,Yi)		Wi=4
	 *  							|(Xi+1,Yi-1)	Wi=5
	 *  
	 *  其中Wi求解公式如下：
	 *  
	 *  	 |4			sum=0 or 15
	 *  Wi = |
	 * 		 |max(sum)	其他
	 * 
	 * sum = zj * wj  (j=1,2,3,4,5)
	 * 
	 * zj表示nj点的像素值，0表示黑点，1表示白点
	 * wj表示nj点被选为下一滴落点的权重 wj = 6 - j
	 * 
	 * nj点表示分布在当前点n0周围的5个点，位置关系如下：
	 * 
	 * n5  n0  n4
	 * n1  n2  n3
	 */
	
	public List<int[]> dropWater(int startY)
	{
		double weightArr[][] = {{1,0,2},{5,4,3}};
		Matrix weightM = new Matrix(weightArr);
		
		List<int[]> points = new ArrayList<int[]>();
		int startPoint[] = {0,startY};
		points.add(startPoint);
		int row = binaryMatrix.getRowDimension();
		int nowPoint[] = startPoint;
		int prePoint[] = nowPoint; 
		while(true)
		{
			int nowX = nowPoint[0];
			int nowY = nowPoint[1];
			if(nowX >= row-1)
				break;
			int nowWeight = 0;
			int nextPoint[] = new int[2];
			Matrix relateM = binaryMatrix.getMatrix(nowX, nowX+1, nowY-1, nowY+1);
			Matrix multiplyMatrix = relateM.arrayTimes(weightM);
			int sum = (int)multiplyMatrix.sum();
			if(sum==0 || sum==15)
			{
				nowWeight = 4;
			}
			else
			{
				nowWeight = (int)multiplyMatrix.max();
			}
			switch(nowWeight)
			{
			case 1:
				nextPoint[0] = nowX;
				nextPoint[1] = nowY - 1;
				break;
			case 2:
				nextPoint[0] = nowX;
				nextPoint[1] = nowY + 1;
				break;
			case 3:
				nextPoint[0] = nowX + 1;
				nextPoint[1] = nowY + 1;
				break;
			case 4:
				nextPoint[0] = nowX + 1;
				nextPoint[1] = nowY;
				break;
			case 5:
				nextPoint[0] = nowX + 1;
				nextPoint[1] = nowY - 1;
				break;
			default:
				nextPoint[0] = nowX;
				nextPoint[1] = nowY;
				break;
			}
			nextPoint[1] = Math.min(nextPoint[1], startY+5);
			
			if((nowPoint[0] == nextPoint[0]) && (nowPoint[1] == nextPoint[1]) )
			{
				nextPoint[0] = nowX + 1;
			}
			if(nextPoint[0] == prePoint[0] && nextPoint[1] == prePoint[1])
			{
				nextPoint[0] = nowPoint[0] + 1;
				nextPoint[1] = nowPoint[1] + 1;
			}
			if(nextPoint[0] > nowX)
				points.add(nextPoint);
			prePoint = nowPoint;
			nowPoint = nextPoint;
			
		}
		return points;
	}
	
	private boolean isSplitPoint2(List<Double> valueList, int position)
	{
		boolean isSplitPoint=false;
		boolean judgeCondition1=false, judgeCondition2=false;
		double difference1;
		double thisV = valueList.get(position);
		double beforeV = valueList.get(position-1);
		difference1 = thisV - beforeV;
		judgeCondition1 = thisV==0 || beforeV==0;
		judgeCondition2 = difference1 != 0;
		if(judgeCondition1 && judgeCondition2)
		{
			isSplitPoint = true;
		}
		return isSplitPoint;
	}
	
	
	/*验证该点是否是分割点
	 * d(y)是y点的竖直投影值
	 * y点的一阶差分H(y)=d(y+1)-d(y)
	 * 极值点满足如下两个条件中的一个即可：
	 * 1) H(y-1)<0
	 *    H(y)>0
	 * 2) H(y-1)<0
	 *    H(y+i)=0
	 *    H(y+j)>0
	 *    i=0,1,2,....,j-1
	 *    0<j<L-2  (L为投影点个数)
	 */
	public ArrayList<Integer> getExtremePoint(double pointValues[])
	{
		int pointNum = pointValues.length;
		double differenceArray[] = new double[pointNum];
		differenceArray[0] = 0.0;
		differenceArray[pointNum-1] = 0.0;
		for(int i=1; i<pointNum-1; i++)
		{
			differenceArray[i] = calcuDifference(pointValues, i);
		}
		
		ArrayList<Integer> extremePoints = new ArrayList<Integer>();
		for(int i=1; i<pointNum-1; i++)
		{
			if(differenceArray[i-1] < 0 && differenceArray[i] > 0)
			{
				extremePoints.add(i);
				continue;
			}
			if(differenceArray[i-1] < 0 && differenceArray[i] == 0)
			{
				for(int j=i+1; j<=pointNum-1; j++)
				{
					if(differenceArray[j-1] != 0)
						break;
					if(differenceArray[j] > 0)
					{
						extremePoints.add(i);
						extremePoints.add(j);
						break;
					}
				}
			}
		}
		return extremePoints;
	}
	
	//计算一阶差分
	private double calcuDifference(double pointValues[], int position)
	{
		double difference = pointValues[position+1] - pointValues[position];
		return difference;
	}
	
	//获取图像积分投影后的图像矩阵
	public Matrix getImgMatrix(Matrix originMatrix)
	{
		int i,j;
		int row = originMatrix.getRowDimension();
		int col = originMatrix.getColumnDimension();
		int rowStart=0, rowEnd=row-1, colStart=0, colEnd=col-1;
		Matrix rowSum = originMatrix.sumDim(Matrix.MATRIX_ROW_DIM);
		Matrix colSum = originMatrix.sumDim(Matrix.MATRIX_COL_DIM);
		boolean hasStart = false, hasEnd = false;
		for(i=0; i<row; i++)
		{
			if(!hasStart && rowSum.get(i, 0) != 0)
			{
				rowStart = i;
				hasStart = true;
			}
			if(!hasEnd && rowSum.get(row-1-i, 0) != 0)
			{
				rowEnd = row-1-i;
				hasEnd = true;
			}
		}
		hasStart = false;
		hasEnd = false;
		for(j=0; j<col; j++)
		{
			if(!hasStart && colSum.get(0, j) != 0)
			{
				colStart = j;
				hasStart = true;
			}
			if(!hasEnd && colSum.get(0, col-1-j) != 0)
			{
				colEnd = col-1-j;
				hasEnd = true;
			}
		}
		
		Matrix imgMatrix = originMatrix.getMatrix(rowStart, rowEnd, colStart, colEnd);
		return imgMatrix;
	}
	
	//获取图像积分投影后的图像矩阵x坐标
	public int[] getImgMatrixX(Matrix originMatrix)
	{
		int positionX[] = new int[2];
		int col = originMatrix.getColumnDimension();
		int colStart=0, colEnd=col-1;
		Matrix colSum = originMatrix.sumDim(Matrix.MATRIX_COL_DIM);
		boolean hasStart = false, hasEnd = false;
		for(int j=0; j<col; j++)
		{
			if(!hasStart && colSum.get(0, j) != 0)
			{
				colStart = j;
				hasStart = true;
			}
			if(!hasEnd && colSum.get(0, col-1-j) != 0)
			{
				colEnd = col-1-j;
				hasEnd = true;
			}
		}
		positionX[0] = colStart;
		positionX[1] = colEnd;
		return positionX;
	}
	
	
	/*
	 * 连通区域法分割图像
	 * 根据二值化图像求取连通区域，并截取出联通图像的图像矩阵,返回连通区域的矩阵
	 */
	public List<Matrix> getConnectAreas()
	{
		int i, j;
		int sign = 0;
		int row = binaryMatrix.getRowDimension();
		int col = binaryMatrix.getColumnDimension();
		Matrix signMatrix = new Matrix(row, col, -1.0);
		List<Matrix> resultMatrixs = new ArrayList<Matrix>();
		List<int[]> neighbours = null;
		for (j = 0; j < col; j++) {
			for (i = 0; i < row; i++) {
				double nowValue = binaryMatrix.get(i, j);
				double nowValueSign = signMatrix.get(i, j);
				if (nowValue == 1 && nowValueSign == -1.0) {
					neighbours = new ArrayList<int[]>();
					int point[] = { i, j };
					neighbours.add(point);
					sign++;
					int neighbourNum = neighbours.size();
					for (int i2 = 0; i2 < neighbourNum; i2++) {
						int neighbourPos[] = neighbours.get(i2);
						int x = neighbourPos[0];
						int y = neighbourPos[1];
						signMatrix.set(x, y, sign);

						for (int jj = -1; jj <= 1; jj++) {
							for (int ii = -1; ii <= 1; ii++) {
								if (x + ii >= 0 && y + jj >= 0 && x + ii < row && y + jj < col) {
									double neighbourValue = binaryMatrix.get(x + ii, y + jj);
									double neighbourValueSign = signMatrix.get(x + ii, y + jj);
									if (neighbourValue == 1.0 && neighbourValueSign == -1.0) {
										int point2[] = { x + ii, y + jj };
										neighbours.add(point2);
										signMatrix.set(x + ii, y + jj, sign);
									}
								}
							}
						}
						neighbourNum = neighbours.size();
					}
					int startx, endx, starty, endy;
					int pos1[] = neighbours.get(0);
					int pos2[] = neighbours.get(1);
					startx = pos1[0];
					starty = pos1[1];
					endx = pos2[0];
					endy = pos2[1];
					for (int k = 0; k < neighbourNum; k++) {
						int pos[] = neighbours.get(k);
						int x = pos[0];
						int y = pos[1];
						if (x < startx)
							startx = x;
						if (x > endx)
							endx = x;
						if (y < starty)
							starty = y;
						if (y > endy)
							endy = y;
					}
					int newRow = endx - startx + 1;
					int newCol = endy - starty + 1;
					Matrix resultM = new Matrix(newRow, newCol, 0);
					for (int k = 0; k < neighbourNum; k++) {
						int pos[] = neighbours.get(k);
						int x = pos[0] - startx;
						int y = pos[1] - starty;
						resultM.set(x, y, 1.0);
					}
					resultMatrixs.add(resultM);
				}
			}
		}
		return resultMatrixs;
	}
	
	
	public Map<Integer,Matrix> getConnectAreas(Matrix magnifiedM)
	{
		double background = magnifiedM.min();
		Map<Integer, Matrix> result = new HashMap<Integer, Matrix>();
		int i, j;
		int sign = 0;
		int row = binaryMatrix.getRowDimension();
		int col = binaryMatrix.getColumnDimension();
		Matrix signMatrix = new Matrix(row, col, -1.0);
		List<int[]> neighbours = null;
		for (j = 0; j < col; j++) {
			for (i = 0; i < row; i++) {
				double nowValue = binaryMatrix.get(i, j);
				double nowValueSign = signMatrix.get(i, j);
				if (nowValue == 1 && nowValueSign == -1.0) {
					neighbours = new ArrayList<int[]>();
					int point[] = { i, j };
					neighbours.add(point);
					sign++;
					int neighbourNum = neighbours.size();
					for (int i2 = 0; i2 < neighbourNum; i2++) {
						int neighbourPos[] = neighbours.get(i2);
						int x = neighbourPos[0];
						int y = neighbourPos[1];
						signMatrix.set(x, y, sign);

						for (int jj = -1; jj <= 1; jj++) {
							for (int ii = -1; ii <= 1; ii++) {
								if (x + ii >= 0 && y + jj >= 0 && x + ii < row
										&& y + jj < col) {
									double neighbourValue = binaryMatrix.get(x + ii, y + jj);
									double neighbourValueSign = signMatrix.get(x + ii, y + jj);
									if (neighbourValue == 1.0 && neighbourValueSign == -1.0) {
										int point2[] = { x + ii, y + jj };
										neighbours.add(point2);
										signMatrix.set(x + ii, y + jj, sign);
									}
								}
							}
						}
						neighbourNum = neighbours.size();
					}
					int startx, endx, starty, endy;
					int pos1[] = neighbours.get(0);
					int pos2[] = neighbours.get(1);
					startx = pos1[0];
					starty = pos1[1];
					endx = pos2[0];
					endy = pos2[1];
					for (int k = 0; k < neighbourNum; k++) {
						int pos[] = neighbours.get(k);
						int x = pos[0];
						int y = pos[1];
						if (x < startx)
							startx = x;
						if (x > endx)
							endx = x;
						if (y < starty)
							starty = y;
						if (y > endy)
							endy = y;
					}
					int newRow = endx - startx + 1;
					int newCol = endy - starty + 1;
					Matrix resultM = new Matrix(newRow, newCol, background);
					for (int k = 0; k < neighbourNum; k++) {
						int pos[] = neighbours.get(k);
						int x = pos[0] - startx;
						int y = pos[1] - starty;
						resultM.set(x, y, magnifiedM.get(pos[0], pos[1]));
					}
					result.put(sign, resultM);
				}
			}
		}
		return result;
	}
}
