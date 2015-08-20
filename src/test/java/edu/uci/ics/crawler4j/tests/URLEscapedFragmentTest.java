package edu.uci.ics.crawler4j.tests;

import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.URLEscapedFragment;
import edu.uci.ics.crawler4j.url.URLTransformer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URLEscapedFragmentTest {

    @Test
    public void testCanonizalier() {
        URLTransformer urlCanonicalizer = new URLEscapedFragment();

        assertEquals("http://www.example.com/display?category=foo/bar+baz",
                urlCanonicalizer.getUrl("http://www.example.com/display?category=foo/bar+baz"));

        assertEquals("http://www.example.com/?q=a+b", urlCanonicalizer.getUrl("http://www.example.com/?q=a+b"));

        assertEquals("http://www.example.com/display?category=foo%2Fbar%2Bbaz",
                urlCanonicalizer.getUrl("http://www.example.com/display?category=foo%2Fbar%2Bbaz"));

        assertEquals("http://somedomain.com/uploads/1/0/2/5/10259653/6199347.jpg?1325154037", urlCanonicalizer
                .getUrl("http://somedomain.com/uploads/1/0/2/5/10259653/6199347.jpg?1325154037"));

        assertEquals("http://hostname.com", urlCanonicalizer.getUrl("http://hostname.com"));

        assertEquals("http://hostname.com", urlCanonicalizer.getUrl("http://HOSTNAME.com"));

        assertEquals("http://www.example.com/index.html?&",
                urlCanonicalizer.getUrl("http://www.example.com/index.html?&"));

        assertEquals("http://www.example.com/index.html?",
                urlCanonicalizer.getUrl("http://www.example.com/index.html?"));

        assertEquals("http://www.example.com", urlCanonicalizer.getUrl("http://www.example.com"));

        assertEquals("http://www.example.com/bar.html",
                urlCanonicalizer.getUrl("http://www.example.com:80/bar.html"));

        assertEquals("http://www.example.com/index.html?name=test&rame=base",
                urlCanonicalizer.getUrl("http://www.example.com/index.html?name=test&rame=base#123"));

        assertEquals("http://www.example.com/%7Eusername/",
                urlCanonicalizer.getUrl("http://www.example.com/%7Eusername/"));

        assertEquals("http://www.example.com//A//B/index.html",
                urlCanonicalizer.getUrl("http://www.example.com//A//B/index.html"));

        assertEquals("http://www.example.com/index.html?&x=y",
                urlCanonicalizer.getUrl("http://www.example.com/index.html?&x=y"));

        assertEquals("http://www.example.com/../../a.html",
                urlCanonicalizer.getUrl("http://www.example.com/../../a.html"));

        assertEquals("http://www.example.com/../a/b/../c/./d.html",
                urlCanonicalizer.getUrl("http://www.example.com/../a/b/../c/./d.html"));

        assertEquals("http://foo.bar.com?baz=1", urlCanonicalizer.getUrl("http://foo.bar.com?baz=1"));

        assertEquals("http://www.example.com/index.html?&c=d&e=f&a=b",
                urlCanonicalizer.getUrl("http://www.example.com/index.html?&c=d&e=f&a=b"));

        assertEquals("http://www.example.com/index.html?q=a b",
                urlCanonicalizer.getUrl("http://www.example.com/index.html?q=a b"));

        assertEquals("http://www.example.com/search?width=100%&height=100%",
                urlCanonicalizer.getUrl("http://www.example.com/search?width=100%&height=100%"));

        assertEquals("http://foo.bar/mydir/myfile?page=2",
                urlCanonicalizer.getUrl("?page=2", "http://foo.bar/mydir/myfile"));

        assertEquals("http://www.example.com/index.html?_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue",
                urlCanonicalizer.getUrl("http://www.example.com/index.html#!/test/123?key=value"));
        assertEquals("http://www.example.com/index.html?name=test&rame=base&_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue",
                urlCanonicalizer.getUrl("http://www.example.com/index.html?name=test&rame=base#!/test/123?key=value"));
    }
}