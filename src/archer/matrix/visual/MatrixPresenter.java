package archer.matrix.visual;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import archer.matrix.Matrix;
import archer.visual.colormap.ColorMap;
import archer.visual.colormap.ColorMap_Gray;


/**
 * Class for visualize Matrix
 * 
 * @author Archer
 * 
 */
public class MatrixPresenter {
	public static int positionX = 150;
	public static int positionY = 0;
	
	ColorMap cm = new ColorMap_Gray();;

	Matrix M = null;
	
	public MatrixPresenter(Matrix M){
		this.M = M;
	}
	
	public MatrixPresenter(Matrix M, ColorMap cm) {
		this.cm = cm;
		this.M = M;
	}
	
	public void setColorMap(ColorMap cm) {
		this.cm = cm;
	}

	public void setM(Matrix m) {
		M = m;
	}

	public void showMatrix(boolean newLine, String title, float d){
		BufferedImage bimg = buildBufferedImage();
		AffineTransform af = AffineTransform.getScaleInstance(d, d);
		AffineTransformOp atop = new AffineTransformOp(af,null);
		BufferedImage output = atop.filter(bimg, null);
		JFrame canvas = new JFrame();
		canvas.setLocation(positionX, positionY);
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setTitle(title);
		Container pane = canvas.getContentPane();
		ColorPanel panel = new ColorPanel(output, d);
		pane.add(panel);
		canvas.setSize((int)Math.ceil(d*bimg.getWidth())+ 50 + 15, (int)Math.ceil(d*bimg.getHeight()) + 20 +40);
		canvas.setVisible(true);
		positionX += 150;
		if(newLine){
			positionY += 120;
			positionX = 0;
		}
	}

	public BufferedImage buildBufferedImage(){
		int m = this.M.getRowDimension();
		int n = this.M.getColumnDimension();
		double max = this.M.max();
		double min = this.M.min();
		BufferedImage bimg = new BufferedImage(n, m, BufferedImage.TYPE_INT_RGB);
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				int rgb = this.cm.getColor((M.get(i,j)-min)/(max-min)).getRGB();
				bimg.setRGB(j, i, rgb);
			}
		}
		return bimg;
	}
	
	public BufferedImage buildBufferedImage2(){
		int m = this.M.getRowDimension();
		int n = this.M.getColumnDimension();
		BufferedImage bimg = new BufferedImage(n, m, BufferedImage.TYPE_INT_RGB);
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				int rgb = (int)M.get(i,j);
				bimg.setRGB(j, i, rgb);
			}
		}
		return bimg;
	}
	
	class ColorPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		BufferedImage theCat;
		float scale;

		public ColorPanel(BufferedImage image, float scale) {
			theCat = image;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(theCat, null, 10, 10);

		}
	}

}