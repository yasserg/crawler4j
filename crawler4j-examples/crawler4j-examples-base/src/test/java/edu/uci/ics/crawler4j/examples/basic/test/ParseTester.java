package edu.uci.ics.crawler4j.examples.basic.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.HtmlParser;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.parser.TikaHtmlParser;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Class to test html parsing in response to 
 * issue #261 (closed) on https://github.com/yasserg/crawler4j/issues/261
 * 
 * @author saleemhalipoto
 *
 */
public class ParseTester {
	CrawlConfig config = new CrawlConfig();
	protected CrawlController crawlController;
	//private Parser parser = crawlController.getParser();

	WebURL testURL = new WebURL();
	

    @Test
    // This will test correct parsing of German characters 'ö' represented by
    // the HTML entity &ouml;
    public void testHTMLGermantextParsing() throws IllegalAccessException, InstantiationException{	
    	Parser parser = new Parser(config);
    	HtmlParser htmlContentParser = new TikaHtmlParser(config);    	
    	testURL.setURL("http://127.0.0.1:8080/oneGermanWord.html");
    	Page page = new Page(testURL);

    	// file is simulating a fetched HTML resource that contains
    	// a German word having a language specific character 'ö' and is needed for this test
    	File file = new File("/Users/saleemhalipoto/development/crawler4j/crawler4j/src/test/resources/testFileContent");
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(file);
			byte [] contentData = new byte [fis.available()];
			System.out.println("Total file size to read (in bytes) : "
					+ fis.available());

			byte content;
			int i = 0;
			while ((content = (byte) fis.read()) != -1) {
				
				// convert to char and display it
				System.out.print((char) content);
				
				// store current character in memory
				contentData[i] = content;
				i++;
			}
	    	page.setContentData(contentData);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

    	try {
    		HtmlParseData parsedData = htmlContentParser.parse(page, testURL.getURL());
        	String testText = parsedData.getText();
            assertEquals("passwörtern", testText); // passwörtern 
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
}