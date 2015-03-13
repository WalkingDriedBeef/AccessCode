package archer.matrix.visual;

import java.util.HashMap;

public class ChartRenderConfiger {
	HashMap<String, String> conf = new HashMap<String, String>();
	
	public ChartRenderConfiger(){
		initialize();
	}
	
	private void initialize(){
		set("", "");
	}
	
	public void set(String property, String value){
		conf.put(property, value);
	}
	
	public String get(String property){
		return conf.get(property);
	}
	
}
