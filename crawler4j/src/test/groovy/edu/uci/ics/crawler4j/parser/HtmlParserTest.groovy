package edu.uci.ics.crawler4j.parser

import spock.lang.*
import java.nio.charset.*
import edu.uci.ics.crawler4j.crawler.*
import edu.uci.ics.crawler4j.url.*
import org.apache.http.entity.*

class HtmlParserTest extends Specification {
    
    def "can parse html page"() {
        def parser = new TikaHtmlParser(new CrawlConfig(), null)
        def url = new WebURL(url: "http://wiki.c2.com/")
        def file = new File("src/test/resources/html/wiki.c2.com.html")
        def contentType = new ContentType("text/html", Charset.forName("UTF-8"))
        def entity = new FileEntity(file, contentType)
        def page = new Page(url)
        page.load entity, 1000000
        
        when:
        parser.parse page, url.url
        
        then:
        noExceptionThrown()
    }

}
