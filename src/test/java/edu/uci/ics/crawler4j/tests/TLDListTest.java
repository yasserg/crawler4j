package edu.uci.ics.crawler4j.tests;

import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import junit.framework.TestCase;

public class TLDListTest extends TestCase {
	
	private WebURL webUrl = new WebURL();
	
	private void setUrl(String url) {
		webUrl.setURL(URLCanonicalizer.getCanonicalURL(url));
	}

	public void testTLD() {
		
		setUrl("http://example.com");
		assertEquals("example.com", webUrl.getDomain());
		assertEquals("", webUrl.getSubDomain());
		
		setUrl("http://test.example.com");
		assertEquals("example.com", webUrl.getDomain());
		assertEquals("test", webUrl.getSubDomain());
		
		setUrl("http://test2.test.example.com");
		assertEquals("example.com", webUrl.getDomain());
		assertEquals("test2.test", webUrl.getSubDomain());
		
		setUrl("http://test3.test2.test.example.com");
		assertEquals("example.com", webUrl.getDomain());
		assertEquals("test3.test2.test", webUrl.getSubDomain());
		
		setUrl("http://www.example.ac.jp");
		assertEquals("example.ac.jp", webUrl.getDomain());
		assertEquals("www", webUrl.getSubDomain());
		
		setUrl("http://example.ac.jp");
		assertEquals("example.ac.jp", webUrl.getDomain());
		assertEquals("", webUrl.getSubDomain());
		
	}
	
}
