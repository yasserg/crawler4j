package edu.uci.ics.crawler4j.url

import com.github.tomakehurst.wiremock.junit.WireMockRule
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
                .withBody(
                """
alpha
omega
"""
        )))
        and: "TLDList instance that download fresh list"
        TLDList.setUseOnline(true, "http://localhost:${wireMockRule.port()}/tld-names.txt")
        def tldList = TLDList.getInstance()

        expect:
        assert tldList.contains("alpha")
        assert !tldList.contains("delta")
    }
}