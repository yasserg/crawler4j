package edu.uci.ics.crawler4j.tests;

import edu.uci.ics.crawler4j.url.URLEscapedFragment;
import edu.uci.ics.crawler4j.url.URLTransformer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URLEscapedFragmentTest {

    @Test
    public void testURLEscapedFragment() {
        URLTransformer urlTransformer = new URLEscapedFragment();

        assertEquals("http://www.example.com/display?category=foo/bar+baz",
                urlTransformer.transform("http://www.example.com/display?category=foo/bar+baz"));

        assertEquals("http://www.example.com/?q=a+b", urlTransformer.transform("http://www.example.com/?q=a+b"));

        assertEquals("http://www.example.com/display?category=foo%2Fbar%2Bbaz",
                urlTransformer.transform("http://www.example.com/display?category=foo%2Fbar%2Bbaz"));

        assertEquals("http://somedomain.com/uploads/1/0/2/5/10259653/6199347.jpg?1325154037", urlTransformer
                .transform("http://somedomain.com/uploads/1/0/2/5/10259653/6199347.jpg?1325154037"));

        assertEquals("http://hostname.com", urlTransformer.transform("http://hostname.com"));

        assertEquals("http://hostname.com", urlTransformer.transform("http://HOSTNAME.com"));

        assertEquals("http://www.example.com/index.html?&",
                urlTransformer.transform("http://www.example.com/index.html?&"));

        assertEquals("http://www.example.com/index.html?",
                urlTransformer.transform("http://www.example.com/index.html?"));

        assertEquals("http://www.example.com", urlTransformer.transform("http://www.example.com"));

        assertEquals("http://www.example.com/bar.html",
                urlTransformer.transform("http://www.example.com:80/bar.html"));

        assertEquals("http://www.example.com/index.html?name=test&rame=base",
                urlTransformer.transform("http://www.example.com/index.html?name=test&rame=base#123"));

        assertEquals("http://www.example.com/%7Eusername/",
                urlTransformer.transform("http://www.example.com/%7Eusername/"));

        assertEquals("http://www.example.com//A//B/index.html",
                urlTransformer.transform("http://www.example.com//A//B/index.html"));

        assertEquals("http://www.example.com/index.html?&x=y",
                urlTransformer.transform("http://www.example.com/index.html?&x=y"));

        assertEquals("http://www.example.com/../../a.html",
                urlTransformer.transform("http://www.example.com/../../a.html"));

        assertEquals("http://www.example.com/../a/b/../c/./d.html",
                urlTransformer.transform("http://www.example.com/../a/b/../c/./d.html"));

        assertEquals("http://foo.bar.com?baz=1", urlTransformer.transform("http://foo.bar.com?baz=1"));

        assertEquals("http://www.example.com/index.html?&c=d&e=f&a=b",
                urlTransformer.transform("http://www.example.com/index.html?&c=d&e=f&a=b"));

        assertEquals("http://www.example.com/index.html?q=a b",
                urlTransformer.transform("http://www.example.com/index.html?q=a b"));

        assertEquals("http://www.example.com/search?width=100%&height=100%",
                urlTransformer.transform("http://www.example.com/search?width=100%&height=100%"));

        assertEquals("http://foo.bar/mydir/myfile?page=2",
                urlTransformer.transform("?page=2", "http://foo.bar/mydir/myfile"));

        assertEquals("http://www.example.com/index.html?_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue",
                urlTransformer.transform("http://www.example.com/index.html#!/test/123?key=value"));
        assertEquals("http://www.example.com/index.html?name=test&rame=base&_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue",
                urlTransformer.transform("http://www.example.com/index.html?name=test&rame=base#!/test/123?key=value"));

        assertEquals("http://www.example.com/index.html?name=test&rame=base&_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue",
                urlTransformer.transform("#!/test/123?key=value", "http://www.example.com/index.html?name=test&rame=base&_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue"));

        assertEquals("http://www.example.com/index.html?name=test&rame=base&_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue",
                urlTransformer.transform("#!/test/123?key=value", "http://www.example.com/index.html?name=test&rame=base&_escaped_fragment_"));

        assertEquals("http://www.example.com/index.html?_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue",
                urlTransformer.transform("#!/test/123?key=value", "http://www.example.com/index.html?_escaped_fragment_"));

        assertEquals("http://www.example.com/index.html?_escaped_fragment_=%2Ftest%2F123%3Fkey%3Dvalue",
                urlTransformer.transform("#!/test/123?key=value", "http://www.example.com/index.html?_escaped_fragment_="));
    }
}