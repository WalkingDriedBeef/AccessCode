package archer.test;

import java.util.ArrayList;
import java.util.List;

import archer.matrix.Matrix;

public class Spliter {
	
	public static boolean isZeroArray(double[] col){
		boolean flag = true;
		for(int i = 0; i < col.length; i++) {
			if (col[i] > 0) flag = false;
		}
		return flag;
	}
	public static int pixel_col_count(double[] col){
		int count = 0;
		for(int i = 0; i < col.length; i++) {
			if (col[i] > 0) count ++;
		}
		return count;
	}
	
	//去除全0行，全0列
	public static Matrix step1_remove_zero(Matrix mat){
		int len = mat.getColumnDimension();
		int start = 0;
		int offset = 0;
		int row_start = 0;
		int row_offset =  0;
		int row_len = mat.getRowDimension();
		while((start < len) && (isZeroArray(mat.getColumnArray(start + offset)))){
			start ++;
		}
		while ((start < len) && (isZeroArray(mat.getColumnArray(len + offset - 1)))) {
		    len --;
		}
		while((row_start < row_len) && (isZeroArray(mat.getRowArray(row_start + row_offset)))){
			row_start ++;
		}
		while ((row_start < row_len) && (isZeroArray(mat.getRowArray(row_len + row_offset - 1)))) {
			row_len --;
		}
		Matrix tmp = mat.getMatrix(row_start, row_len - 1, start, len - 1);
		return tmp;
	}
//	//找到某列上的的像素点数小于N时，这说明该地方很可能就是分割点（N = 2）
//	public static List<Matrix> step1_split_part(Matrix mat){
//		List<Matrix> spts = new ArrayList<Matrix>();
//		int len = mat.getColumnDimension();
//		int start = 0;
//		int offset = 0;
//		while((start < len) && (isZeroArray(mat.getColumnArray(start + offset)))){
//			start ++;
//		}
//		while ((start < len) && (isZeroArray(mat.getColumnArray(len + offset - 1)))) {
//		    len --;
//		}
//		return null;
//	}
	public static List<Matrix> step2_split_part(Matrix child, List<Matrix> spts){
		if(spts == null){
			spts = new ArrayList<Matrix>();
		}
		int threshold_max = 17;
		int threshold_min = 9;
		int part = threshold_min;
		if(child.getColumnDimension() >= threshold_max){
			spts.add(child.getMatrix(0, part));
			return step2_split_part(child.getMatrix(part - 2, child.getColumnDimension() - 1), spts);
		}else{
			spts.add(child);
		}
		return spts;
	}
	
	/*
	 * 连通区域法分割图像
	 * 根据二值化图像求取连通区域，并截取出联通图像的图像矩阵,返回连通区域的矩阵
	 */
	public static List<Matrix> getConnectAreas(Matrix mat){
		int i, j;
		int sign = 0;
		int row = mat.getRowDimension();
		int col = mat.getColumnDimension();
		Matrix signMatrix = new Matrix(row, col, -1.0);
		List<Matrix> resultMatrixs = new ArrayList<Matrix>();
		List<int[]> neighbours = null;
		for (j = 0; j < col; j++) {
			for (i = 0; i < row; i++) {
				double nowValue = mat.get(i, j);
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
									double neighbourValue = mat.get(x + ii, y + jj);
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
}
