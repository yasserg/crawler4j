package edu.uci.ics.crawler4j.url

import spock.lang.*
import edu.uci.ics.crawler4j.crawler.*

class PublicSuffixTest extends Specification {
    
    @Shared internalTldList = new TLDList(new CrawlConfig(
        onlineTldListUpdate:    false
    ))

    @Shared externalTldList = new TLDList(new CrawlConfig(
        onlineTldListUpdate:    true,
        publicSuffixLocalFile:  "src/test/resources/public_suffix_list.dat"
    ))

    def "etld domains (publicsuffix.org) are correctly identified by internal lookup"() {
        def webUrl = new WebURL()
        webUrl.tldList = internalTldList
        
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

    def "etld domains (publicsuffix.org) are correctly identified by external lookup"() {
        def webUrl = new WebURL()
        webUrl.tldList = externalTldList
        
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
