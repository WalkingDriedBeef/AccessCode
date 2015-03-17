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

public class Sample_sim {
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
				gray.set(i, j, gray.get(i, j) < BinaryzationThreshold.basic(255) ? 1 : 0);
			}
		}
		return gray;
	}
	//第三步，分割图片，非粘连图片，按照像素的边界区域分割（）
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
		File dir = new File("sim/img/");
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
					ToolShowImg.buildImg(mat, "sim/dat/" + name.charAt(index) + "_" + ++index_l2 + ".jpg");
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
		HashMap<int[], String> traindata = loadTrainData("sim/dat/");
		for(File child: new File("sim/test/").listFiles()){
			getAllOcr(child.getAbsolutePath(), traindata);
		}
	}
}
