package edu.uci.ics.crawler4j.examples.spring;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.examples.spring.entity.CrawlRequest;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerRequestModel;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerStatus;
import edu.uci.ics.crawler4j.examples.spring.repo.CrawlerRequestRepository;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;

import java.net.URI;

public class MyWebCrawler extends WebCrawler {

    private final CrawlerRequestModel request;
    private final CrawlerRequestRepository repo;
    private final ModelMapper modelMapper;

    public MyWebCrawler(CrawlerRequestModel request, CrawlerRequestRepository repo, ModelMapper modelMapper) {
        this.request = request;
        this.repo = repo;
        this.modelMapper = modelMapper;
    }

    @Override
    public void onStart() {
        request.setStatus(CrawlerStatus.WORKING);
        repo.save(modelMapper.map(request, CrawlRequest.class));
    }

    public boolean shouldVisit(Page referringPage, WebURL url) {
        final URI uri;
        try {
            uri = URI.create(url.getURL());
        } catch (IllegalArgumentException e) {
            logger.warn("Illegal url {}", url.getURL());
            return false;
        }
        if (StringUtils.startsWithIgnoreCase(referringPage.getWebURL().getURL(), uri.getScheme() + "://" + uri.getHost()) ||
            StringUtils.startsWithIgnoreCase(referringPage.getWebURL().getURL(), "https://" + uri.getHost())) {
            return true;
        }

        logger.debug("Ignoring " + url);
        return false;
    }
    @Override
    public void onBeforeExit() {
        request.setStatus(CrawlerStatus.DONE);
        request.setFinished(DateTime.now());
        repo.save(modelMapper.map(request, CrawlRequest.class));
    }

    @Override
    protected boolean shouldFollowLinksIn(WebURL url) {
        return false;
    }
}
