/**
 * 验证码识别 -- 数据准备:
 * 
 *  加载图片（RGB） --> 图片去噪 --> RGB转Gray --> Gray二值化
 *  
 *  |
 *  +--> 图片切分  --> 训练数据 --> 结束
 *  
 *  
 *  验证码识别 -- 流程测试：
 *  加载图片（RGB） --> 图片去噪 --> RGB转Gray --> Gray二值化
 *  
 *  |
 *  +--> 图片切分  --> 加载训练数据 ---+
 *                                   |
 *     获得结果 <-- 比较相似度      <--+
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
	//
	public static List<Matrix> step3_split_img(Matrix img) {
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
		}
		return result;
	}
	
	public static List<Matrix> step5_rev_merprol(Matrix child){
		return Spliter.step2_split_part(child, new ArrayList<Matrix>());
	}
	
	public static void train(){
		File dir = new File("sim/img/");
		int index_l2 = 0;
		int index_fg = 0;
		for(File imgpath: dir.listFiles()){
//			File imgpath = new File("tmp/13521671514.gif");
			System.out.println(imgpath.getName());
			Matrix gray = step1_RGB_Gray(imgpath.getAbsolutePath());
			Matrix bina = step2_binaryzation(gray);
			Matrix rmv = Spliter.step1_remove_zero(bina);
			List<Matrix> mats = step3_split_img(rmv);
			String name = imgpath.getName().replace(".jpg", "");
			if(mats == null){
				continue;
			}
			System.out.println(++index_fg + "\t" + mats.size());
			if(name.length() <= mats.size()){
				for(int index = 0; index < name.length(); index++){
					Matrix mat = mats.get(index);
					ToolShowImg.buildImg(mat, "sim/dat/" + name.charAt(index) + "_" + ++index_l2 + ".jpg");
				}
			}
		}
	}
	public static HashMap<int[], String> loadTrainData(String train){
		HashMap<int[], String> traindata = new HashMap<int[], String>();
		for(File child: new File(train).listFiles()){
			Matrix mat = step1_RGB_Gray(child.getAbsolutePath());
			traindata.put(getFeature(mat, 100), String.valueOf(child.getName().charAt(0)));
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
		Matrix rmv = Spliter.step1_remove_zero(bina);
		List<Matrix> mats = step3_split_img(rmv);
		
		HashMap<int[], String> traindata = loadTrainData("sim/dat/");
//		System.out.println(traindata.size());
		String result = "";
		for (Matrix mat : mats) {
			double min = Double.MAX_VALUE;
			int[] tmp = null;
			for(int[] fet: traindata.keySet()){
				double dist = Distance.cosineDis(getFeature(mat, 100),fet);
				if(dist < min){
					min = dist;
					tmp = fet;
				}
			}
			result += traindata.get(tmp);
		}
		System.out.println("---------------------------------------------");
		System.out.println(file + "\t" + result + "\t" + (file.indexOf(result) > -1 ? 0 : 1));
		System.out.println("---------------------------------------------");
		return null;
	}
	
	public static void main(String[] args) {
		train();
//		getAllOcr("test/18601318772.gif");
		for(File child: new File("sim/test/").listFiles()){
			getAllOcr(child.getAbsolutePath());
		}
//		train();
	}
}
