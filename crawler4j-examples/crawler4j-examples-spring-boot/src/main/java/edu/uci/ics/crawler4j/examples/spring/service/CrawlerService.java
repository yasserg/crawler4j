package edu.uci.ics.crawler4j.examples.spring.service;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerRequestModel;
import edu.uci.ics.crawler4j.examples.spring.repo.CrawlerRequestRepository;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class CrawlerService {

    @Autowired
    CrawlConfig config;

    @Autowired
    CrawlerRequestRepository crawlerRequestRepository;

    @Autowired
    ModelMapper modelMapper;

    @Async
    public void crawl(CrawlerRequestModel request, int numCrawlers) throws Exception {
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);

        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        // If you want ignore robots.txt
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        controller.addSeed(request.getUrl());

        CrawlerFactory factory = new CrawlerFactory(request, crawlerRequestRepository, modelMapper);

        controller.startNonBlocking(factory, numCrawlers);

        while (!controller.isFinished()) {
            Thread.sleep(10_000);

            log.info("Waiting for crawler request {} to finish."
                , request.getId());
        }

        log.info("Crawling done for {}", 10);

        if ( ! deleteQuietly(new File(config.getCrawlStorageFolder())) ) {
            log.warn("Something wrong deleting {}" + config.getCrawlStorageFolder());
        }
    }
}
