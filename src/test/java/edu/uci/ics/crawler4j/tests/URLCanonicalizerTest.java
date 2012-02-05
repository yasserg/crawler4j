package edu.uci.ics.crawler4j.tests;

import junit.framework.TestCase;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;

public class URLCanonicalizerTest extends TestCase {

	public void testCanonizalier() {

		assertEquals("http://www.example.com/display?category=foo%2Fbar%2Bbaz",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/display?category=foo/bar+baz"));

		assertEquals("http://www.example.com/?q=a%2Bb",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/?q=a+b"));

		assertEquals("http://www.example.com/display?category=foo%2Fbar%2Bbaz",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/display?category=foo%2Fbar%2Bbaz"));

		assertEquals("http://somedomain.com/uploads/1/0/2/5/10259653/6199347.jpg?1325154037",
				URLCanonicalizer
						.getCanonicalURL("http://somedomain.com/uploads/1/0/2/5/10259653/6199347.jpg?1325154037"));

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

		assertEquals("http://www.example.com/~username/",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/%7Eusername/"));

		assertEquals("http://www.example.com/A/B/index.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com//A//B/index.html"));

		assertEquals("http://www.example.com/index.html?x=y",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/index.html?&x=y"));

		assertEquals("http://www.example.com/a.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/../../a.html"));

		assertEquals("http://www.example.com/a/c/d.html",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/../a/b/../c/./d.html"));

		assertEquals("http://foo.bar.com/?baz=1", URLCanonicalizer.getCanonicalURL("http://foo.bar.com?baz=1"));

		assertEquals("http://www.example.com/index.html?a=b&c=d&e=f",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/index.html?&c=d&e=f&a=b"));

		assertEquals("http://www.example.com/index.html?q=a%20b",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/index.html?q=a b"));

		assertEquals("http://www.example.com/search?height=100%&width=100%",
				URLCanonicalizer.getCanonicalURL("http://www.example.com/search?width=100%&height=100%"));

		assertEquals("http://foo.bar/mydir/myfile?page=2",
				URLCanonicalizer.getCanonicalURL("?page=2", "http://foo.bar/mydir/myfile"));

	}
}
