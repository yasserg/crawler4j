package edu.uci.ics.crawler4j.examples.basic.test;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

public class ParseTester {
	protected CrawlController crawlController;
	private Parser parser = crawlController.getParser();
	WebURL testURL = new WebURL();
	byte [] contentData = null;

    @Test
    public void testHTMLtextParsing() {	
    	testURL.setURL("http://127.0.0.1:8080/oneGermanWord.html");
    	Page page = new Page(testURL);
    	page.setContentData(contentData);
    	
    	
    	
    	
    	try {
			parser.parse(page, testURL.getURL());
		} catch (NotAllowedContentException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	HtmlParseData parsedData= (HtmlParseData) page.getParseData();
    	String testText = parsedData.getText();
        assertEquals("passwörtern", testText);
        
        
        

/*        assertEquals("http://www.example.com/?q=a%2Bb",
                     URLCanonicalizer.getCanonicalURL("http://www.example.com/?q=a+b"));

        assertEquals("http://www.example.com/display?category=foo%2Fbar%2Bbaz",
                     URLCanonicalizer.getCanonicalURL(
                         "http://www.example.com/display?category=foo%2Fbar%2Bbaz"));*/
    }
}