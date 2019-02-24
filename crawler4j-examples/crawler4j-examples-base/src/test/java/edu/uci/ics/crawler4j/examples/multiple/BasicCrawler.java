package edu.uci.ics.crawler4j.examples.multiple;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class BasicCrawler extends WebCrawler {
    private static final Logger logger = LoggerFactory.getLogger(BasicCrawler.class);

    private static final Pattern FILTERS = Pattern.compile(
        ".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4" +
        "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private final List<String> myCrawlDomains;

    public BasicCrawler(List<String> myCrawlDomains) {
        this.myCrawlDomains = ImmutableList.copyOf(myCrawlDomains);
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (FILTERS.matcher(href).matches()) {
            return false;
        }

        for (String crawlDomain : myCrawlDomains) {
            if (href.startsWith(crawlDomain)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        int parentDocid = page.getWebURL().getParentDocid();

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Docid of parent page: {}", parentDocid);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            logger.debug("Text length: {}", text.length());
            logger.debug("Html length: {}", html.length());
            logger.debug("Number of outgoing links: {}", links.size());
        }

        logger.debug("=============");
    }
}