package archer.image.accesscode;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import archer.image.io.ReadSample;
import archer.classifier.Threshold;
import archer.image.io.ImageMatrix;
import archer.matrix.Matrix;
import archer.matrix.visual.MatrixPresenter;
import archer.visual.colormap.ColorMap_Gray;

public class StatisticTemplateWidth {
	public static void main(String[] args) throws Exception {
		Image image = null;
		try {
			Preprocessor prep = new Preprocessor();
			ReadSample reader = new ReadSample("..\\image\\accesscode\\template");
			String path = reader.getTemplatePath();
			Map<String,String> fileNames = reader.readFile(path, null);
			Set<String> keys = fileNames.keySet();
			for(String key : keys)
			{
				String templateName = fileNames.get(key);
				File file = new File(templateName);
				image = ImageIO.read(file);
				ImageMatrix IM = new ImageMatrix(image);
				
//				Matrix originM = IM.getOriginMatrix();
//				Matrix magnifiedM = prep.magnifyShrinkImg2(originM, originM.getRowDimension()*2, originM.getColumnDimension()*2);
//				show(magnifiedM, "magnified graphy");
//				IM.loadImage(magnifiedM);
				// Preprocess
				prep.setIM(IM);
				
				//转换成灰色图片
				IM = prep.convert2Gray();
				show(IM.getChannel(0),"gray");
				prep.setIM(IM);
//				Matrix M = prep.binaryzation(Threshold.TH_OTSU);
				Matrix M = prep.binaryzation(Threshold.TH_MIN_ERROR);
				show(M,"binaryzationed Matrix");
				M = prep.erodeImg8(M);
				show(M,"erodeImg8 Matrix");
				M = prep.erodeImg8(M);
				show(M,"erodeImg8 Matrix");
				
				CharSpliter spliter = new CharSpliter(M);
				Matrix imageMatrix= spliter.getImgMatrix(M);
				System.out.println(key + " height:" + String.valueOf(imageMatrix.getRowDimension()) 
						+ "  width:" + String.valueOf(imageMatrix.getColumnDimension()));
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void show(Matrix D, String title) {
		ColorMap_Gray cmf = new ColorMap_Gray();
		MatrixPresenter mp = new MatrixPresenter(D, cmf);
		float scale = 3f;
		mp.showMatrix(false, title, scale);
	}
}
