package archer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class KeyboardReader {
	public static void pause(String tip) {
		System.out.print(tip);
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			inputReader.readLine();
		} catch (IOException e) {
			System.out.println("Error reading keyboard input");
		}
	}
	
	public static String read(String tip){
		String line = null;
		System.out.print(tip);
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			line = inputReader.readLine();
		} catch (IOException e) {
			System.out.println("Error reading keyboard input");
		}
		
		return line;
	}
}
