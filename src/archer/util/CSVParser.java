package archer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CSVParser {
	private String item_token = ",";

	private String record_token = "\r\n";
	
	private String comment_prelabel = "#$";

	public CSVParser() {

	}

	public String getItem_token() {
		return item_token;
	}

	public void setItem_token(String item_token) {
		this.item_token = item_token;
	}

	public String getRecord_token() {
		return record_token;
	}

	public void setRecord_token(String record_token) {
		this.record_token = record_token;
	}

	
	
	public String getComment_prelabel() {
		return comment_prelabel;
	}

	public void setComment_prelabel(String comment_prelabel) {
		this.comment_prelabel = comment_prelabel;
	}

	public ArrayList<String[]> parse(String csvfile) {
		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<String[]> records = new ArrayList<String[]>();

		try {
			fr = new FileReader(csvfile);
			br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if(line.startsWith(comment_prelabel) || !line.contains(item_token))
					continue;
				String[] record = line.split(item_token);
				if (record.length > 0)
					records.add(record);
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return records;
	}

}
