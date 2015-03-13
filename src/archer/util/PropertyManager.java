package archer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import archer.util.Path;


public class PropertyManager {
	private String comment = "#";
	
	private String mapsymbol = "=";
	
	private String propfile = null;
	
	private int paragraph = 0;
	
	private HashMap<String, String> propmap = new HashMap<String, String>();
	
	private HashMap<String, String> commap = new HashMap<String, String>();
	
	private HashMap<String, String> keymap = new HashMap<String, String>();
	
	public PropertyManager(){
		
	}
	
	public PropertyManager(String propfile){
		this.propfile = propfile;
		parse();
	}
	
	public String getPropfile() {
		return propfile;
	}

	public void setPropfile(String propfile) {
		this.propfile = propfile;
		parse();
	}

	private void parse(){
		FileReader fr = null;
		BufferedReader br = null;
		paragraph = 0;
		
		try {
			fr = new FileReader(propfile);
			br = new BufferedReader(fr);
			String line = null;

			int position = -1;
			while ((line = br.readLine()) != null) {
				String parag = String.valueOf(paragraph);
				if(line.startsWith(comment))
					commap.put(parag, line);
				else if((position = line.indexOf(mapsymbol)) != -1){
					String key = line.substring(0, position).trim(); 
					propmap.put(key, line.substring(position+1).trim());
					keymap.put(parag, key);
				} else
					commap.put(parag, line);
				paragraph++;
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPropertyValue(String property){
		String value = null;
		value = propmap.get(property);
		return value;
	}
	
	public boolean set(String property, String value){
		if(!propmap.containsKey(property))
			return false;
		
		propmap.put(property, value);
		return true;
	}
	
	public void save(){
		saveAs(propfile);
	}
	
	public void saveAs(String filename){
		File file = new File(filename);

		try {
			if(!file.exists())
				file.createNewFile();
			FileWriter fw =new FileWriter(file);
			String line = null;
			for(int i=0; i<paragraph; i++){
				String parag = String.valueOf(i);
				if(commap.containsKey(parag))
					line = commap.get(parag);
				else {
					String key = keymap.get(parag);
					line = key + " = " + propmap.get(key);
				}
				fw.write(line + "\n");
				fw.flush();
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		File file = new File("D:/a.txt");
		if(!file.exists())
			try {
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				fos.write("TEST".getBytes());
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		String path = Path.getFullClassPath(PropertyManager.class);
		PropertyManager pm = new PropertyManager(path + "/test.properties");
		pm.set("a", "9");
//		pm.save();
		pm.saveAs(path + "/a.properties");
	}
}
