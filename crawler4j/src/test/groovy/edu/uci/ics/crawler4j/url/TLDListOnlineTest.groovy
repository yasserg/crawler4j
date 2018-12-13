package edu.uci.ics.crawler4j.url

import com.github.tomakehurst.wiremock.junit.WireMockRule
import edu.uci.ics.crawler4j.crawler.CrawlConfig
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

class TLDListOnlineTest extends Specification {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    def "download TLD from url"() {
        given: "an online TLD file list"
        stubFor(get(urlEqualTo("/tld-names.txt"))
            .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/plain")
            .withBody("fakeprovince.ca")
        ))
        and: "TLDList instance that download fresh list"
        def config = new CrawlConfig(onlineTldListUpdate: true, publicSuffixSourceUrl: "http://localhost:${wireMockRule.port()}/tld-names.txt")
        def tldList = new TLDList(config)

        expect:
        assert tldList.contains("fakeprovince.ca")
        assert !tldList.contains("on.ca")
        verify(1, getRequestedFor(urlEqualTo("/tld-names.txt")));
    }
}