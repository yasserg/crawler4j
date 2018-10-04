package edu.uci.ics.crawler4j.tests.fetcher;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageFetcherHtmlTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort());

    @Test
    public void testCustomPageFetcher() throws Exception {

        WireMock.stubFor(WireMock.head(WireMock.urlEqualTo("/some/index.html"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "text/html")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/some/index.html"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "text/html")
                                                     .withHeader("Content-Length", "47")
                                                     .withBody("<html><body><h1>this is " +
                                                               "html</h1></body></html>")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/some/invoice.pdf"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type", "application/pdf")
                                                     .withBody(new byte[] {1, 2, 3, 4})));

        WireMock.stubFor(WireMock.head(WireMock.urlEqualTo("/some/invoice.pdf"))
                                 .willReturn(WireMock.aResponse()
                                                     .withStatus(200)
                                                     .withHeader("Content-Type",
                                                                 "application/pdf")));

        CrawlConfig cfg = new CrawlConfig();
        WebURL url = new WebURL();

        url.setURL("http://localhost:" + wireMockRule.port() + "/some/index.html");
        PageFetcher pf = new PageFetcherHtmlOnly(cfg);
        pf.fetchPage(url).fetchContent(new Page(url), 47);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/some/index.html")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/some/index.html")));

        url.setURL("http://localhost:" + wireMockRule.port() + "/some/invoice.pdf");
        pf = new PageFetcherHtmlOnly(cfg);
        pf.fetchPage(url).fetchContent(new Page(url), 4);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/some/invoice.pdf")));
        WireMock.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/some/invoice.pdf")));
    }
}
