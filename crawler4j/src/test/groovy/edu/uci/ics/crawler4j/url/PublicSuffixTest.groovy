/*
 * Copyright 2018 Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
