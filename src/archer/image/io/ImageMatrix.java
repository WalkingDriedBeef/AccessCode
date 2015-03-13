package archer.image.io;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import archer.matrix.Matrix;


/**
 * The ImageMatrix stores the Image object in the Matrix format to make 
 * the image processing more easier.
 * 
 * @author Archer
 * @version 15 December 2011
 */
public class ImageMatrix {

	private static final long serialVersionUID = 1L;
	
	int row=0;
	int col=0;
	
	public ArrayList<Matrix> channels = new ArrayList<Matrix>();
	public ArrayList<Matrix> hsvChannels = new ArrayList<Matrix>();
	
	public BufferedImage bfimage;
	public Matrix originMatrix;
	
	public BufferedImage getBfimage() {
		return bfimage;
	}

	public void setBfimage(BufferedImage bfimage) {
		this.bfimage = bfimage;
	}

	public Matrix getOriginMatrix() {
		return originMatrix;
	}

	public void setOriginMatrix(Matrix originMatrix) {
		this.originMatrix = originMatrix;
	}

	public ImageMatrix(File  imagefile){
		try {
			Image img = ImageIO.read(imagefile);
			this.loadImage(img);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ImageMatrix(URL url){
		try {
			Image img = ImageIO.read(url);
			loadImage(img);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ImageMatrix(Image img){
		loadImage(img);		
	}
	
	public ImageMatrix(Matrix [] M){
		for(int i=0; i<M.length; i++){
			this.channels.add(M[i]);
			row = M[i].getRowDimension();
			col = M[i].getColumnDimension();
		}
	}
	
	public void loadImage(Image img) {
		// TODO Auto-generated method stub
		BufferedImageBuilder bib = new BufferedImageBuilder();
		bfimage = bib.bufferImage(img);
		this.row = bfimage.getHeight(null);
		this.col = bfimage.getWidth(null);
		originMatrix = new Matrix(row,col);
		
		Matrix R = new Matrix(row, col);
		Matrix G = new Matrix(row, col);
		Matrix B = new Matrix(row, col);
		for (int j=0; j<col; j++) {
			for (int i=0; i<row; i++) {
				int rgb = bfimage.getRGB(j, i);
				originMatrix.set(i, j, rgb);
				R.set(i, j, (rgb >> 16) & 0xFF);
				G.set(i, j, (rgb >> 8) & 0xFF);
				B.set(i, j, rgb & 0xFF);
			}
		}
		this.channels.add(0, R);
		this.channels.add(1, G);
		this.channels.add(2, B);
//		R = R.minusEquals(B);
//		if(!R.equals(B) || !R.equals(G)){
//			this.channels.add(1, G);
//			this.channels.add(2, B);
//		}
		
		//输出图形矩阵
//		System.out.println("-----------------------输出图形矩阵-------------------------------");
//		for(int i=0; i<3; i++){
//			System.out.println("-----------------------channels[" + i + "]-------------------------------");
//			channels.get(i).show();
//			System.out.println();
//		}
	}
	
	public void loadImage(Matrix matrix) {
		this.row = matrix.getRowDimension();
		this.col = matrix.getColumnDimension();
		
		this.channels.clear();
		Matrix R = new Matrix(row, col);
		Matrix G = new Matrix(row, col);
		Matrix B = new Matrix(row, col);
		for (int j=0; j<col; j++) {
			for (int i=0; i<row; i++) {
				int rgb = (int)matrix.get(i, j);
				R.set(i, j, (rgb >> 16) & 0xFF);
				G.set(i, j, (rgb >> 8) & 0xFF);
				B.set(i, j, rgb & 0xFF);
			}
		}
		this.channels.add(0, R);
		this.channels.add(1, G);
		this.channels.add(2, B);
	}
	
	
	private void getHsvMatrix0()
	{
		Matrix R = this.channels.get(0);
		Matrix G = this.channels.get(1);
		Matrix B = this.channels.get(2);
		
		Matrix H = new Matrix(row, col);
		Matrix S = new Matrix(row, col);
		Matrix V = new Matrix(row, col);
		
		for(int i=0; i<this.row; i++)
		{
			for(int j=0; j<this.col; j++)
			{
				double r = R.get(i, j);
				double g = G.get(i, j);
				double b = B.get(i, j);
				double max = selectMax(r, g, b);
				double min = selectMin(r, g, b);
				double delta = max - min;
				double h=0,s,v;
				v = max;
				
				if(delta == 0.0){
					delta = 1.0;
				}
				if(max != 0.0){
					s = delta / max;
				}else{
					s = 0;
					h = -1;
				}
					
				if(r == max) h = (g-b)/delta;
				if(g == max) h = 2+(b-r)/delta;
				if(b == max) h = 4+(r-g)/delta;
				h = h * 60;
				if(h < 0) h += 360;
				H.set(i, j, h);
				S.set(i, j, s);
				V.set(i, j, v);
			}
		}
		
		System.out.println("------------------------输出HSV矩阵------------------------------");
		H.show();
		System.out.println();
		S.show();
		System.out.println();
		V.show();
		System.out.println();
		
		this.hsvChannels.add(0, H);
		this.hsvChannels.add(1, S);
		this.hsvChannels.add(2, V);
	}
	
	//尝试根据hsv图像矩阵抽取出字符
//	public Matrix checkHsvBinary()
//	{
//		Matrix H = getHsvChannel(0);
//		int row = H.getRowDimension();
//		int col = H.getColumnDimension();
//		Matrix binaryM = new Matrix(row,col);
//		int values[] = {23,26,60,240,330};
//		for(int i=0; i<row; i++)
//		{
//			for(int j=0; j<col; j++)
//			{
//				int now = (int)H.get(i, j);
//				int tmp = 0;
//				for(int m=0; m<values.length; m++)
//				{
//					if(values[m] == now)
//						tmp = 1;
//				}
//				binaryM.set(i, j, tmp);
//			}
//		}
//		
//		return binaryM;
//	}
	
	
	private void getHsvMatrix1()
	{
		Matrix R = this.channels.get(0);
		Matrix G = this.channels.get(1);
		Matrix B = this.channels.get(2);
		
		Matrix H = new Matrix(row, col);
		Matrix S = new Matrix(row, col);
		Matrix V = new Matrix(row, col);
		
		for(int i=0; i<this.row; i++)
		{
			for(int j=0; j<this.col; j++)
			{
				double r = R.get(i, j);
				double g = G.get(i, j);
				double b = B.get(i, j);
				
				double max = selectMax(r, g, b);
				double min = selectMin(r, g, b);
				double delta = max - min;
				
				double h=0,s,v;
				v = max;
				s = delta / max;
				S.set(i, j, s);
				V.set(i, j, v);
				
				if(s == 0)
				{
					H.set(i, j, h);
					continue;
				}
				
				double judge_condition = s * v;
				if((r == max) && (judge_condition >= 0.01)) h = 60 * (g-b)/judge_condition;
				if((g == max) && (judge_condition >= 0.01)) h = 60 * (2+(b-r)/judge_condition);
				if((b == max) && (judge_condition >= 0.01)) h = 60 * (4+(r-g)/judge_condition);
				h -= 300;
				if(h < 0) h += 360;
				H.set(i, j, h);
			}
		}
		
		System.out.println("------------------------输出HSV矩阵------------------------------");
		H.show();
		System.out.println();
		S.show();
		System.out.println();
		V.show();
		System.out.println();
		
		this.hsvChannels.add(0, H);
		this.hsvChannels.add(1, S);
		this.hsvChannels.add(2, V);
	}
	
	public Matrix getHsvGrayMatrix(double threshold)
	{
		Matrix HMatrix = this.hsvChannels.get(0);
		int row = HMatrix.getRowDimension();
		int col = HMatrix.getColumnDimension();
		
		Matrix gray = new Matrix(row,col);
		for(int i=0; i<row; i++)
		{
			for(int j=0; j<col; j++)
			{
				double nowValue = HMatrix.get(i, j);
				if(nowValue >= threshold)
				{
					gray.set(i, j, 0);
				}
				else
				{
					gray.set(i, j, 1);
				}
			}
		}
		
		return gray;
	}
	
	
	
	private double selectMax(double a, double b, double c)
	{
		double tmp;
		if(a > b)
			tmp = a;
		else
			tmp = b;
		if(tmp > c)
			return tmp;
		else
			return c;
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
	
	public void addChannel(Matrix B){
		if (this.checkMatrixDimension(B)) {
			this.channels.add(B);
		    if (this.channels.size()==1) {
		    	this.col = B.getColumnDimension();
		    	this.row = B.getRowDimension();
		    }
		} else {
			// throw exception 	
		}
	}
	
	public Matrix getChannel(int index) {
		return this.channels.get(index);
	}
	
	public Matrix getHsvChannel(int index) {
		return this.hsvChannels.get(index);
	}
	
	public void setChannel(int index, Matrix matrix)
	{
		this.channels.set(index, matrix);
	}
	
	private boolean checkMatrixDimension(Matrix B){
		if (this.channels.size()!=0) 
			if ( (B.getColumnDimension()!= this.col) || (B.getRowDimension()!=this.row) ) 
				return false;
			else
				return true;
		else
			return false;
	}
	
	public Matrix histOf(int index) {
		Matrix M = this.getChannel(index);
		Matrix H = new Matrix(256,2);
		
		for (int i=0; i<256; i++){
			H.set(i, 0, i);
		}
		int m = M.getRowDimension();
		int n = M.getColumnDimension();
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				int v = (int)Math.round(M.get(i, j));
				H.set(v, 1, H.get(v, 1)+1);
			}
		}
		return H;
	}
	
	public int getWidth() {
		return this.col;
	}
	
	public int getHeight() {
		return this.row;
	}
	
	public int getChannelSize(){
		return this.channels.size();
	}
	
	public void showRGB(int[] channels) throws IllegalArgumentException{
		if (channels.length!=3){
			throw new IllegalArgumentException("Select 3 channels to show colorful image.");
		}
	}
	
	//缩放图片
//	public static BufferedImage resize(BufferedImage source, int targetW, int targetH) {
//		// targetW，targetH分别表示目标长和宽
//		int type = source.getType();
//		BufferedImage target = null;
//		double sx = (double) targetW / source.getWidth();
//		double sy = (double) targetH / source.getHeight();
//
//		//这里想实现在targetW，targetH范围内实现等比缩放。如果不需要等比缩放
//		//则将下面的if else语句注释即可
//		if (sx > sy) {
//			sx = sy;
//			targetW = (int) (sx * source.getWidth());
//		} else {
//			sy = sx;
//			targetH = (int) (sy * source.getHeight());
//		}
//
//		if (type == BufferedImage.TYPE_CUSTOM) { //handmade
//			ColorModel cm = source.getColorModel();
//			WritableRaster raster = cm.createCompatibleWritableRaster(targetW, targetH);
//			boolean alphaPremultiplied = cm.isAlphaPremultiplied();
//			target = new BufferedImage(cm, raster, alphaPremultiplied, null);
//		} else
//			target = new BufferedImage(targetW, targetH, type);
//		Graphics2D g = target.createGraphics();
//		//smoother than exlax:
//		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//
//		g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
//		g.dispose();
//		return target;
//	}
	
	
}
