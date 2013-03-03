package edu.uci.ics.crawler4j.url;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class TLDList {
	
	private final String tldNamesFileName = "tld-names.txt";

	private Set<String> tldSet = new HashSet<>();

	private static TLDList instance = new TLDList();

	private TLDList() {
		try {
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream(tldNamesFileName);
			if (stream == null) {
				System.err.println("Couldn't find " + tldNamesFileName);
				System.exit(-1);
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("//")) {
					continue;
				}
				tldSet.add(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static TLDList getInstance() {
		return instance;
	}

	public boolean contains(String str) {
		return tldSet.contains(str);
	}

}
