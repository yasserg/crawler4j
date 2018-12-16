package edu.uci.ics.crawler4j.parser

import edu.uci.ics.crawler4j.url.WebURL
import spock.lang.Specification

/**
 * Test the CssParseData class.
 *
 * @author Federico Tolomei <mail@s17t.net>
 */
class CssParseDataTest extends Specification {

    def "CSS urls parsing quotes"() {
        given: "css parser"
        CssParseData parseData = new CssParseData()
        parseData.setTextContent(this.getClass().getResource( '/css/quotes.css' ).text)

        and: "configure css parser"
        WebURL webUrl = new WebURL()
        webUrl.setURL("http://example.com/css.css")

        when: "parse css"
        parseData.setOutgoingUrls(webUrl)
        Set<WebURL> urls = parseData.outgoingUrls

        then: "urls from css"
        assert urls.size() == 3
    }

    def "CSS absolute urls paths"() {
        given: "css parser"
        CssParseData parseData = new CssParseData()
        parseData.setTextContent(this.getClass().getResource( '/css/absolute.css' ).text)

        and: "configure css parser"
        WebURL webUrl = new WebURL()
        webUrl.setURL("http://example.com/css.css")

        when: "parse css"
        parseData.setOutgoingUrls(webUrl)
        Set<WebURL> urls = parseData.outgoingUrls

        then: "urls from css"
        assert urls.size() == 3

        and:
        List<String> mapped = urls.collect { x -> x.getURL() }
        assert mapped.contains("http://example.com/css/absolute_no_proto.png")
        assert mapped.contains("http://example.com/css/absolute_path.png")
        assert mapped.contains("http://example.com/css/absolute_with_domain.png")
    }

    def "CSS relative urls paths"() {
        given: "css parser"
        CssParseData parseData = new CssParseData()
        parseData.setTextContent(this.getClass().getResource( '/css/relative.css' ).text)

        and: "configure css parser"
        WebURL webUrl = new WebURL()
        webUrl.setURL("http://example.com/asset/css/css.css")

        when: "parse css"
        parseData.setOutgoingUrls(webUrl)
        Set<WebURL> urls = parseData.outgoingUrls

        then: "urls from css"
        assert urls.size() == 2

        and:
        List<String> mapped = urls.collect { x -> x.getURL() }
        assert mapped.contains("http://example.com/asset/images/backgound_one.jpg")
        assert mapped.contains("http://example.com/backgound_two.jpg")
    }
}
