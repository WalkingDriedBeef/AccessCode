package archer.image.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import archer.util.Path;

public class ReadSample {
	
	public static final String SAMPLE = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghkmnpqrsuvwxyz23456789";
	
	
	private String templatePath;
	
	public ReadSample(String templatePath)
	{
		this.templatePath = getTemplatePath(templatePath);
	}
	
	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}
	
	private String getTemplatePath(String pathName) {
		String path = null;
		try {
			path = Path.getFullPathRelateClass(pathName,ReadSample.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
	
	public void preprocessSample()
	{
		String readPath = getTemplatePath("..\\image\\accesscode\\origintemplate");
		String writePath = getTemplatePath("..\\image\\accesscode\\sumtemplate");
		int templateNum = SAMPLE.length();
		FileWriter fw = null;
		for(int i=0; i<templateNum; i++)
		{
			char nameChr = SAMPLE.charAt(i);
			String templateName = String.valueOf(nameChr);
			if(isUpperCase(nameChr))
				templateName += "1";
			
			try {
				Map<Integer, String> templates = readFilePath(readPath + "\\" + templateName, null);
				fw = new FileWriter(writePath + "\\" + templateName);
				for(int j=0; j<templates.size(); j++)
				{
					String templateFile = templates.get(j);
					List<String> templateN = readTemplate(templateFile);
					for(String str : templateN)
					{
						fw.write(str + " ");
					}
					fw.write("\n");
					fw.flush();
				}
				fw.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean isUpperCase(char chr)
	{
		if(chr>=65 && chr<=90)
			return true;
		else
			return false;
	}
	
	
	private boolean assembleTrainSample(){
		String readPath = getTemplatePath("..\\image\\accesscode\\sumtemplate");
		String writePath = getTemplatePath("..\\image\\accesscode\\trainsample");
		int sampleNum = SAMPLE.length();
		FileWriter fw;
		for (int i=0; i<sampleNum; i++) {
			
			for(int j=1; j<sampleNum-i; j++)
			{
				
				String sample1 = String.valueOf(SAMPLE.charAt(i));
				if(isUpperCase(SAMPLE.charAt(i)))
					sample1 += "1";
				String sample2 = String.valueOf(SAMPLE.charAt(i + j));
				if(isUpperCase(SAMPLE.charAt(i + j)))
					sample2 += "1";
				String outputFile = sample1 + sample2;
				
				try {
					fw = new FileWriter(writePath + "\\" + outputFile);

					BufferedReader bfr1 = new BufferedReader(new FileReader(readPath + "\\" + sample1));
					String line = null;
					while ((line = bfr1.readLine()) != null) 
					{
						String sampleV = "+1" + " " + line;
						fw.write(sampleV);
						fw.flush();
					}
					fw.write("\n");
					bfr1.close();

					bfr1 = new BufferedReader(new FileReader(readPath +  "\\" + sample2));
					while ((line = bfr1.readLine()) != null) 
					{
						String sampleV = "-1" + " " + line;
						fw.write(sampleV);
						fw.flush();
					}
					bfr1.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	public List<String> readTemplate(String fileName) throws IOException
	{
		int row = 0;
		List<String> templateValue = new ArrayList<String>();
		List<String[]> S = new ArrayList<String[]>();
		File file = new File(fileName);
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String tempStr = null;
			while((tempStr = reader.readLine()) != null)
			{
				String result[] = tempStr.split(" ");
				S.add(result);
				row++;
			}
			String tempResult = "";
			int col = S.get(0).length;
			for(int i=0; i<row; i++)
			{
				for(int j=0; j<col; j++)
				{
					int nowOrder = i * col + j + 1;
					if(Integer.valueOf(S.get(i)[j]) == 1)
					{
						String templateV = String.valueOf(nowOrder) + ":" + "1"; 
						tempResult += templateV;
						tempResult += " ";
					}
				}
			}
			templateValue.add(tempResult);
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(fileName);
		}
		reader.close();
		return templateValue;
	}
	
	public Map<String, String> readFile(String filePath, HashMap<String,String> fileMap) throws Exception{
		Map<Integer, String> filePathName = readFilePath(filePath,null);
		if(fileMap == null){
			fileMap = new HashMap<String, String>();
		}
		Set<Integer> keys = filePathName.keySet();
		for(Integer k : keys)
		{
			String fileName = filePathName.get(k);
			String names[] = fileName.split("\\\\");
			String name = names[names.length-1];
			name = name.replace(".jpg", "");
			fileMap.put(name, fileName);
		}
		
		return fileMap;
	}
	
	/*
	 * 获取文件夹下文件名
	 */
	
	public Map<Integer, String> readFilePath(String filepath, Map<Integer, String> pathMap) throws Exception {
		if (pathMap == null) {
			pathMap = new HashMap<Integer, String>();
		}

		File file = new File(filepath);
		// 文件
		if (!file.isDirectory()) {
			pathMap.put(pathMap.size(), file.getPath());

		} else if (file.isDirectory()) { // 如果是目录， 遍历所有子目录取出所有文件名
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
				File readfile = new File(filepath + "/" + filelist[i]);
				if (!readfile.isDirectory()) {
					pathMap.put(pathMap.size(), readfile.getPath());

				} else if (readfile.isDirectory()) { // 子目录的目录
					readFilePath(filepath + "/" + filelist[i], pathMap);
				}
			}
		}
		return pathMap;
	}
	
	public static void main(String[] args)
	{
		ReadSample rs = new ReadSample("abc");
		rs.preprocessSample();
		rs.assembleTrainSample();
		System.out.println("output train sample OK");
	}
	
}
