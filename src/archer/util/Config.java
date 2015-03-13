package archer.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
	
	public static Map<String,String> getConfig() throws IOException
	{
		Map<String, String> configs = new HashMap<String, String>();
		BufferedReader brd = new BufferedReader(new FileReader("src//archer//config"));
		String tmpStr = null;
		while ((tmpStr = brd.readLine()) != null) {
			String confs[] = tmpStr.split("=");
			configs.put(confs[0], confs[1]);
		}
		
		return configs;
	}
}
