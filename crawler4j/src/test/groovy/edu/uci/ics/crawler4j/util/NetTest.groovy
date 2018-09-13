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

import spock.lang.Specification

/**
 * Test the Net utility class.
 *   
 * @author Paul Galbraith <paul.d.galbraith@gmail.com>
 */
class NetTest extends Specification {
    def "FEATURE: correctly identify URLs in a text document" () {
        given: "a test document with embedded URLs"
            def testDocument = '''
                www.wikipedia.com
                https://en.wikipedia.org/wiki/Main_Page
                http://somesite.com:8080/page/1
                http://localhost/page/1
                http://localhost:8080/page/1
            '''
            
            when: "identify and extract URLs"
                def urls = Net.extractUrls(testDocument)
                
            then: "should have found 5 URLs"
                urls.size() == 5
    }

}
