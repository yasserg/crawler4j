package edu.uci.ics.crawler4j.crawler

import edu.uci.ics.crawler4j.url.WebURL
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHeader
import spock.lang.Specification

class PageTest extends Specification {

    def "default charset fallback"() {
        given: "http entity with unsupported charset"
        HttpEntity entity = new BasicHttpEntity()
        String content = "The content";
        entity.setContent(IOUtils.toInputStream(content, "UTF-8"))
        entity.setContentLength(content.size())
        entity.setContentType(new BasicHeader("Content-type", "text/html; charset=UNPARSABLE"))

        when: "trying to load the entity"
        WebURL u = new WebURL()
        Page page = new Page(u);
        page.load(entity, 1024)

        then: "charset should fallback to UTF-8"
        "UTF-8".equals(page.getContentCharset())
    }
}
