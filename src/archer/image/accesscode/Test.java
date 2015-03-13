package archer.image.accesscode;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import archer.classifier.Threshold;
import archer.image.io.ImageMatrix;
import archer.matrix.Matrix;
import archer.matrix.visual.MatrixPresenter;
import archer.visual.colormap.ColorMap_Gray;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Image image = null;
		String code = null;
		try {
			Preprocessor prep = new Preprocessor();

			for (int i = 1; i <= 1; i++) {
				// Load Image
				File file = new File("c:/test/sample/136.jpg");
//				File file = new File("c:/test/third22/MakN.jpg");

				image = ImageIO.read(file);
				//URL url = new
				//URL("http://dynamic.12306.cn/TrainQuery/passCodeActi0n.do?rand=rrand");
				//image = ImageIO.read(url);

				ImageMatrix IM = new ImageMatrix(image);

				// Preprocess
				prep.setIM(IM);
//				去噪--中值滤波
//				prep.eraseNoise();

				//转换成灰色图片
				IM = prep.convert2Gray();
				show(IM.getChannel(0), "ORIGIN GRAY");

				prep.setIM(IM);
//				Matrix M = prep.binaryzation(Threshold.TH_OTSU);
				Matrix M = prep.binaryzation(Threshold.TH_MIN_ERROR);
				show(M, "0-1 matrix");

				CharSpliter spliter = new CharSpliter(M);
				List<Matrix> splitResult = spliter.getConnectAreas();
				for (int ii = 0; ii < splitResult.size(); ii++) {
					Matrix splitedM = splitResult.get(ii);
					splitedM.sumDim(Matrix.MATRIX_COL_DIM).show();
//					System.out.println(String.valueOf(splitedM.getRowDimension()) + " : " + String.valueOf(splitedM.getColumnDimension()));
					show(splitedM, "magnified splited matrix");
				}

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
