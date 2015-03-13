package archer.image.io;

import java.io.FileWriter;
import java.io.IOException;

import archer.matrix.Matrix;

public class SaveMatrix {
	
	private Matrix toWriteMatrix;
	FileWriter fw;
	
	public SaveMatrix(Matrix origM, String outputName) throws IOException
	{
		this.toWriteMatrix = origM;
		fw = new FileWriter(outputName);
	}
	
	public void saveTemplate() throws IOException
	{
		int row = toWriteMatrix.getRowDimension();
		int col = toWriteMatrix.getColumnDimension();
		for(int i=0; i<row; i++)
		{
			for(int j=0; j<col; j++)
			{
				fw.write(String.valueOf((int)toWriteMatrix.get(i, j) + " "));
			}
			fw.write("\n");
			fw.flush();
		}
	}
	
	
}
