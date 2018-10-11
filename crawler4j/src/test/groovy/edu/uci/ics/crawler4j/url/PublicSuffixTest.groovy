package edu.uci.ics.crawler4j.url

import spock.lang.*
import edu.uci.ics.crawler4j.crawler.*

class PublicSuffixTest extends Specification {
    
    def "etld domains (publicsuffix.org) are correctly identified by internal lookup"() {
        def webUrl = new WebURL()
        webUrl.tldList = new TLDList(new CrawlConfig())
        
        when:
        webUrl.setURL url;
        
        then:
        webUrl.domain == domain;
        webUrl.subDomain == subdomain;
        
        where:
        url                             || domain               | subdomain
        "http://www.example.com"        || "example.com"        | "www"
        "http://dummy.edu.np"           || "dummy.edu.np"       | ""
    }

}
