package archer.test;

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

public class ToolShowImg {
	public static void showimg(Matrix tmp, String title){
		float d = 3.0f;
		int positionX = 150;
		int positionY = 0;
		int m = tmp.getRowDimension();
		int n = tmp.getColumnDimension();
		BufferedImage bimg = new BufferedImage(n, m, BufferedImage.TYPE_BYTE_GRAY);
		ColorMap cm = new ColorMap_Gray();;
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				int rgb = cm.getColor(tmp.get(i, j)).getRGB();
				bimg.setRGB(j, i, rgb);
			}
		}
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
	}
	
	
	static class ColorPanel extends JPanel {
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
