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

package edu.uci.ics.crawler4j.util

import spock.lang.*
import edu.uci.ics.crawler4j.crawler.*

/**
 * Test the Net utility class.
 *   
 * @author Paul Galbraith <paul.d.galbraith@gmail.com>
 */
class NetTest extends Specification {

    @Shared standard = new Net(new CrawlConfig(), null)
    @Shared allowSingleLevelDomain = new Net(new CrawlConfig(allowSingleLevelDomain: true), null)
    
    def "no scheme specified" () {
        when: def extracted = standard.extractUrls "www.wikipedia.com"
        then: expectMatch extracted, "http://www.wikipedia.com/"
    }
    
    def "localhost" () {
        when: def extracted = allowSingleLevelDomain.extractUrls "http://localhost/page/1"
        then: expectMatch extracted, "http://localhost/page/1"
    }
    
    def "no url found" () {
        when: def extracted = standard.extractUrls "http://localhost"
        then: expectMatch extracted     // no expected URL
    }
    
    def "multiple urls" () {
        when: def extracted = standard.extractUrls " hey com check out host.com/toodles and http://例子.测试 real soon "
        then: expectMatch extracted, "http://host.com/toodles", "http://例子.测试/"
    }
    
    void expectMatch(def extractedUrls, String... expectedUrls) {
        def extracted = extractedUrls.collect { it.URL } as Set
        def expected = expectedUrls as Set
        assert extracted == expected
    }

}
