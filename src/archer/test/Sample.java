package archer.test;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import archer.image.accesscode.CharSpliter;
import archer.matrix.Matrix;

public class Sample {
	//第一步：获取图片的RGB信息，然后做转灰色处理
	public static Matrix step1_RGB_Gray(String imgpath){
		try {
			File file = new File(imgpath);
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
		//basic way
		for(int i = 0; i < gray.getRowDimension(); i++){
			for(int j = 0; j < gray.getColumnDimension(); j++){
//				gray.set(i, j, gray.get(i, j) >= BinaryzationThreshold.basic(255) ? 0 : 1);
				gray.set(i, j, gray.get(i, j) >= BinaryzationThreshold.otsu(gray) ? 0 : 1);
			}
		}
//		System.out.println("============二值化=========================================================================");
//		gray.show(1);
		return gray;
	}
	
	
	//第三步，删除上下左右的全零行，亦或去噪
	public static Matrix step3_remove_zero_line(Matrix bina){
		Matrix rzl = Spliter.step1_remove_zero(bina);
//		System.out.println("============去0行=========================================================================");
//		rzl.show(1);
//		Spliter.step2_split_by_pixel_n_col(rzl);
		return rzl;
	}
	
	public static List<Matrix> step4_split_img(Matrix img) {
		CharSpliter spter = new CharSpliter(img);
//		List<Matrix> mats = spter.charSplitByDropWater();
		List<Matrix> mats = spter.getConnectAreas();
		return mats;
	}
	
	public static List<Matrix> step5_rev_merprol(Matrix child){
		return Spliter.step2_split_part(child, new ArrayList<Matrix>());
	}
	
	public static void train(){
		File dir = new File("tmp/");
		int index_l2 = 0;
		for(File imgpath: dir.listFiles()){
//			File imgpath = new File("tmp/13521671514.gif");
			System.out.println(imgpath.getName());
			Matrix gray = step1_RGB_Gray(imgpath.getAbsolutePath());
			Matrix bina = step2_binaryzation(gray);
			Matrix spt = step3_remove_zero_line(bina);
//			ToolShowImg.showimg(spt, "spt");
			List<Matrix> mats = step4_split_img(spt);
//			ToolShowImg.showimg(mats);
			List<Matrix> st5s = new ArrayList<Matrix>();
			for(Matrix tmp: mats){
				st5s.addAll(step5_rev_merprol(tmp));
			}
//			ToolShowImg.showimg(st5s);
			String name = imgpath.getName().replace(".gif", "");
			if(name.length() == st5s.size()){
				for(int index = 0; index < name.length(); index++){
					Matrix mat = st5s.get(index);
					ToolShowImg.buildImg(mat, "dat/" + name.charAt(index) + "_" + ++index_l2 + ".jpg");
				}
			}
		}
	}
	public static HashMap<int[], String> loadTrainData(String train){
		HashMap<int[], String> traindata = new HashMap<int[], String>();
		for(File child: new File(train).listFiles()){
			Matrix mat = step1_RGB_Gray(child.getAbsolutePath());
			traindata.put(getFeature(mat, 256), String.valueOf(child.getName().charAt(0)));
		}
		return traindata;
	}
	public static int[] getFeature(Matrix mat, int feature){
		if(feature <= 0) feature = 256;
		int[] feats = new int[feature];
		int flag = -1;
		for(int i = 0; i < mat.getRowDimension(); i++){
			for(int j = 0; j < mat.getColumnDimension(); j++){
				if(++flag < feats.length){
					feats[flag] = (int)mat.get(i, j);
				}
			}
		}
		return feats;
	}
	
	
	public static String getAllOcr(String file) {
		Matrix gray = step1_RGB_Gray(file);
		Matrix bina = step2_binaryzation(gray);
		Matrix spt = step3_remove_zero_line(bina);
		List<Matrix> mats = step4_split_img(spt);
		List<Matrix> st5s = new ArrayList<Matrix>();
		for(Matrix tmp: mats){
			st5s.addAll(step5_rev_merprol(tmp));
		}
		HashMap<int[], String> traindata = loadTrainData("dat");
//		System.out.println(traindata.size());
		String result = "";
//		ToolShowImg.showimg(st5s);
		for (Matrix mat : st5s) {
			double min = Double.MAX_VALUE;
			int[] tmp = null;
			for(int[] fet: traindata.keySet()){
				double dist = Distance.cosineDis(getFeature(mat, 256),fet);
				if(dist < min){
					min = dist;
					tmp = fet;
				}
			}
			result += traindata.get(tmp);
		}
		System.out.println("---------------------------------------------");
		System.out.println(file + "\t" + result);
		System.out.println("---------------------------------------------");
		return null;
	}
	
	public static void main(String[] args) {
//		getAllOcr("test/18601318772.gif");
		for(File child: new File("test").listFiles()){
			getAllOcr(child.getAbsolutePath());
		}
	}
}
