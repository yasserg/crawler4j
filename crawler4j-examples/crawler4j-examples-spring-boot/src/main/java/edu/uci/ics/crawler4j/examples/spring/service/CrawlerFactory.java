package edu.uci.ics.crawler4j.examples.spring.service;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.examples.spring.MyWebCrawler;
import edu.uci.ics.crawler4j.examples.spring.entity.CrawlRequest;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerRequestModel;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerStatus;
import edu.uci.ics.crawler4j.examples.spring.repo.CrawlerRequestRepository;
import edu.uci.ics.crawler4j.url.WebURL;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

public class CrawlerFactory implements CrawlController.WebCrawlerFactory {

    private final CrawlerRequestModel request;
    private final CrawlerRequestRepository repo;
    private final ModelMapper modelMapper;

    public CrawlerFactory(
        CrawlerRequestModel request
        , CrawlerRequestRepository crawlerRequestRepository, ModelMapper modelMapper) {
        this.request = request;
        this.repo = crawlerRequestRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public WebCrawler newInstance() throws Exception {
        WebCrawler webCrawler = new MyWebCrawler(request, repo, modelMapper);

        return webCrawler;
    }

}
