package edu.uci.ics.crawler4j.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class TLDList {

	private static Set<String> tldSet;
	
	public static boolean contains(String str) {
		return tldSet.contains(str);
	}
	
	static {
		tldSet = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(TLDList.class.getClassLoader().getResourceAsStream("tld-names.txt")));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("//")) {
					continue;
				}
				tldSet.add(line);
			}
			reader.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
}
