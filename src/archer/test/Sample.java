package archer.test;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import archer.matrix.Matrix;

public class Sample {
	
	//第一步：获取图片的RGB信息，然后做转灰色处理
	public static Matrix step1_RGB_Gray(){
		try {
			File file = new File("tmp/13601306622.gif");
			Image img = ImageIO.read(file);
			
			BufferedImageBuilder bib = new BufferedImageBuilder();
			BufferedImage bfimage = bib.bufferImage(img);
			
			int row = bfimage.getHeight();
			int col = bfimage.getWidth();
			
			Matrix R = new Matrix(row, col);
			Matrix G = new Matrix(row, col);
			Matrix B = new Matrix(row, col);
			
			for (int j=0; j<col; j++) {
				for (int i=0; i<row; i++) {
					int rgb = bfimage.getRGB(j, i);
					R.set(i, j, (rgb >> 16) & 0xFF);
					G.set(i, j, (rgb >> 8) & 0xFF);
					B.set(i, j, rgb & 0xFF);
				}
			}
			
			Matrix tmp = new Matrix(row, col);
			tmp = R.times(0.114);
			tmp = tmp.plus(G.times(0.587));
			tmp = tmp.plus(B.times(0.299));
			return tmp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//第二步，将图片信息二值化(有像素点置为1，否者置为0)
	public static Matrix step2_binaryzation(Matrix gray){
		for(int i = 0; i < gray.getRowDimension(); i++){
			for(int j = 0; j < gray.getColumnDimension(); j++){
				gray.set(i, j, gray.get(i, j) == 255? 0 : 1);
			}
		}
		System.out.println("============二值化=========================================================================");
		gray.show(1);
		return gray;
	}
	
	public static Matrix step3_remove_zero_line(Matrix bina){
		Matrix rzl = Spliter.step1_remove_zero(bina);
		System.out.println("============去0行=========================================================================");
		rzl.show(1);
		return rzl;
	}
	
	public static void main(String[] args) {
		Matrix gray = step1_RGB_Gray();
		Matrix bina = step2_binaryzation(gray);
		Matrix spt = step3_remove_zero_line(bina);
		ToolShowImg.showimg(spt, "spt");
	}
}
