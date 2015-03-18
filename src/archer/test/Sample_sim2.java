/**
 * 
 *  验证码识别流程：
 *  
 * 【数据训练】
 * +--------------------------------------------------------------------------------------+
 * |  +--------------+     +-----------+     +------------+     +------------+            |
 * |  | 加载图片(RGB) | --> |  图片去噪  | --> |  RGB转Gray | --> |  二值化     | --> +      |
 * |  +--------------+     +-----------+     +------------+     +------------+     |      |
 * |                                                                               |      |
 * |                                       +--------------+      +------------+    |      |
 * |                                       | 训练数据完成   | <-- |  图片切分   | <--+      |
 * |                                       +--------------+      +------------+           |
 * +----------------------------------------------|---------------------------------------+ 
 *                                                |加
 *                                                |载
 *                                                +------------------------->
 *                                                                          |    +-----------------+
 * 【测试】                                                                  +--> | 获得结果（相似度）|
 * +----------------------------------------------------------------+       |    +-----------------+
 * | 加载图片(RGB) --> 图片去噪 --> RGB转Gray -->  二值化 --> 图片切分  |-------> 
 * +----------------------------------------------------------------+
 *  
 *  拿到的验证码识别的程序中涉及到的算法主要有如下：
 *  1. 二值化:  有ostu阈值、最小错误阈值；
 *  2. 图片分割: 连通区域分割算法、滴水算法
 *  
 *  通过调研发现，个人认为图片分割对图片的识别起到至关重要的作用；
 *  58同城、赶集网的企业的联系方式都是粘连很严重的图片，使用程序中的分割算法并不能得到一个较好的分割结果；
 *  故，想要识别58同城的联系方式需要更加深入的去了解OCR、Machine Learning的知识
 *  
 *  综上，我想确认一下，我后续是否还要继续的深入的研究OCR这部分，即继续研究验证码这块的代码；
 *  
 *  有以上的调研，目前可以解决没有粘连、少量粘连的验证码，如下两类，图片的识别率如下
 *  10 / 10 == 100.00%
 *  20 / 30 ==  66.67%
 * 
 *  参照的文章：
 *  1. http://skywen.iteye.com/blog/1828671
 *  2. http://www.geekso.com/Valite2/
 *  
 */
package archer.test;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import archer.matrix.Matrix;

public class Sample_sim2 {
	private static final int FEATURE_SIZE = 100;
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
			//RGB matrix
			for (int j=0; j<col; j++) {
				for (int i=0; i<row; i++) {
					int rgb = bfimage.getRGB(j, i);
					R.set(i, j, (rgb >> 16) & 0xFF);
					G.set(i, j, (rgb >> 8) & 0xFF);
					B.set(i, j, rgb & 0xFF);
				}
			}
			//RGB --> Gray matrix
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
	
	//第二步，将图片信息二值化(有像素点置为1，否者置为0)，该步骤之前或许会有去噪点的步骤
	public static Matrix step2_binaryzation(Matrix gray){
		for(int i = 0; i < gray.getRowDimension(); i++){
			for(int j = 0; j < gray.getColumnDimension(); j++){
				gray.set(i, j, gray.get(i, j) < BinaryzationThreshold.basic(255)  && gray.get(i, j) >= 131 ? 0 : 1);
			}
		}
		return gray;
	}
	//第三步，分割图片，非粘连图片，按照像素的边界区域分割（）
	public static List<Matrix> step3_split_img(Matrix img) {
//		img.show(1);
		ArrayList<Integer> points = new ArrayList<Integer>();
		for(int i = 0; i < img.getColumnDimension(); i++){
			if(Spliter.isZeroArray(img.getColumnArray(i))){
				points.add(i);
			}
		}
		ArrayList<Integer> tmp = new ArrayList<Integer>();
//		for(int i: points){
//			System.out.print(i + "\t");
//		}
//		System.out.println();
		tmp.add(0);
		for(int i = 0; i < points.size(); i++){
			if(i == 0){
				tmp.add(points.get(i));
			}else{
				int prev = points.get(i - 1);
				int curr = points.get(i);
				if(curr - prev != 1) {
					tmp.add(curr);
				}
			}
		}
		tmp.add(img.getColumnDimension() - 1);
//		for(int i: tmp){
//			System.out.print(i + "\t");
//		}
//		System.out.println();
		////////////////////////////////
		if(tmp.size() == 5){
			ArrayList<Integer> tmp_x = new ArrayList<Integer>();
			for(int i = 0; i < tmp.size(); i++){
				if(i == tmp.size() - 1){
					tmp_x.add(tmp.get(i));
				}else{
					if(tmp.get(i+1) - tmp.get(i) >= 20 ){
						tmp_x.add(tmp.get(i));
						tmp_x.add(tmp.get(i) + (tmp.get(i+1) - tmp.get(i)) / 2 );
					}else{
						tmp_x.add(tmp.get(i));
					}
				}
			}
			tmp = tmp_x;
		}
		////////////////////////////////
//		for(int i: tmp){
//			System.out.print(i + "\t");
//		}
//		System.out.println();
		if(tmp.size() != 6){
			return null;
		}
		List<Matrix> result = new ArrayList<Matrix>();
		for(int i = 0; i < tmp.size() - 1; i++){
			Matrix x = img.getMatrix(tmp.get(i), tmp.get(i+1));
			result.add(Spliter.step1_remove_zero(x));
//			Spliter.step1_remove_zero(x).show(1);
		}
		return result;
	}
	//第四步，将分割后的图片当做训练数据保存起来
	public static void train(){
		File dir = new File("sim2/img/");
		int index_l2 = 0;
		int index_fg = 0;
		for(File imgpath: dir.listFiles()){
			System.out.println(imgpath.getName());
			Matrix gray = step1_RGB_Gray(imgpath.getAbsolutePath());
			Matrix bina = step2_binaryzation(gray);
			Matrix rmv = Spliter.step1_remove_zero(bina);
//			rmv.show(1);
			List<Matrix> mats = step3_split_img(rmv);
			String name = imgpath.getName().replace(".jpg", "");
			if(mats == null){
				continue;
			}
			System.out.println(++index_fg + "\t" + mats.size());
			if(name.length() == mats.size()){
				for(int index = 0; index < name.length(); index++){
					Matrix mat = mats.get(index);
					ToolShowImg.buildImg(mat, "sim2/dat/" + name.charAt(index) + "_" + ++index_l2 + ".jpg");
				}
			}
//			break;
		}
	}
	
	public static HashMap<int[], String> loadTrainData(String train){
		HashMap<int[], String> traindata = new HashMap<int[], String>();
		for(File child: new File(train).listFiles()){
			Matrix mat = step1_RGB_Gray(child.getAbsolutePath());
			traindata.put(getFeature(mat, FEATURE_SIZE), String.valueOf(child.getName().charAt(0)));
		}
		return traindata;
	}
	public static int[] getFeature(Matrix mat, int feature){
		if(feature <= 0) feature = FEATURE_SIZE;
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
	//测试
	public static String getAllOcr(String file, HashMap<int[], String> traindata) {
		Matrix gray = step1_RGB_Gray(file);            //加载图片
		Matrix bina = step2_binaryzation(gray);        //二值化
		Matrix rmv = Spliter.step1_remove_zero(bina);  //去除边界的非像素行或列
		List<Matrix> mats = step3_split_img(rmv);      //分割图片
		String result = "";
		for (Matrix mat : mats) {
			double min = Double.MAX_VALUE;
			int[] tmp = null;
			for(int[] fet: traindata.keySet()){
				double dist = Distance.cosineDis(getFeature(mat, FEATURE_SIZE),fet);
				if(dist < min){
					min = dist;
					tmp = fet;
				}
			}
			result += traindata.get(tmp);
		}
		System.out.println(file + "\t" + result + "\t" + (file.indexOf(result) > -1 ? 0 : 1));
		System.out.println("---------------------------------------------");
		return null;
	}
	
	public static void main(String[] args) {
//		train();
//		加载训练数据
		HashMap<int[], String> traindata = loadTrainData("sim2/dat/");
		for(File child: new File("sim2/test/").listFiles()){
			getAllOcr(child.getAbsolutePath(), traindata);
		}
	}
}
