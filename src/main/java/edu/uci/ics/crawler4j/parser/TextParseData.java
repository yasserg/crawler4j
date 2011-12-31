package edu.uci.ics.crawler4j.parser;

public class TextParseData implements ParseData {

	private String textContent;

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}
	
	@Override
	public String toString() {
		return textContent;
	}
	
}
