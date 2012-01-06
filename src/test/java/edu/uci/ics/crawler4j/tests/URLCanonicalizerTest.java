package edu.uci.ics.crawler4j.tests;

import junit.framework.TestCase;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;

public class URLCanonicalizerTest extends TestCase {

	public void testCanonizalier() {
		
		assertEquals("http://hostname.com/", URLCanonicalizer.getCanonicalURL("http://hostname.com"));
		
		assertEquals("http://hostname.com/", URLCanonicalizer.getCanonicalURL("http://HOSTNAME.com"));

		assertEquals("http://www.example.com/index.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/index.html?&"));

		assertEquals("http://www.example.com/index.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/index.html?"));

		assertEquals("http://www.example.com/", URLCanonicalizer.getCanonicalURL("http://www.example.com"));

		assertEquals("http://www.example.com/bar.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com:80/bar.html"));

		assertEquals("http://www.example.com/index.html?name=test&rame=base",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/index.html?name=test&rame=base#123"));

		assertEquals("http://www.example.com/a/b/index.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com//A//B/index.html"));

		assertEquals("http://www.example.com/index.html?x=y",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/index.html?&x=y"));

		assertEquals("http://www.example.com/a.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/../../a.html"));

		assertEquals("http://www.example.com/a/c/d.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/../a/b/../c/./d.html"));

		assertEquals("http://foo.bar.com/?baz=1", URLCanonicalizer.getCanonicalURL("http://foo.bar.com?baz=1"));

		

	}
}
