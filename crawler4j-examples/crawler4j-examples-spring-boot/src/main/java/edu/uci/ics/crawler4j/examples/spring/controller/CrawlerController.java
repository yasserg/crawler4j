package edu.uci.ics.crawler4j.examples.spring.controller;

import edu.uci.ics.crawler4j.examples.spring.entity.CrawlRequest;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerRequestModel;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerStatus;
import edu.uci.ics.crawler4j.examples.spring.repo.CrawlerRequestRepository;
import edu.uci.ics.crawler4j.examples.spring.service.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequestMapping("/api/v1")
@RestController
public class CrawlerController {

    @Autowired
    CrawlerService service;

    @Autowired
    CrawlerRequestRepository crawlerRequestRepository;

    @Autowired
    ModelMapper modelMapper;

    @RequestMapping(path = "/crawl", method = RequestMethod.POST)
    public ResponseEntity<CrawlerRequestModel> index(@RequestBody CrawlerRequestModel request) {
        final CrawlRequest run = modelMapper.map(request, CrawlRequest.class);
        run.setStarted(DateTime.now());
        run.setStatus(CrawlerStatus.ACCEPTED);
        final CrawlRequest saved = crawlerRequestRepository.save(run);

        final CrawlerRequestModel savedModel = modelMapper.map(saved, CrawlerRequestModel.class);

        try {
            service.crawl(savedModel, 2);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(savedModel);
        } catch (Exception e) {
            log.error("Crawler not started.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CrawlerRequestModel.EMPTY);
        }
    }

    @RequestMapping(path = "/crawl/{id}", method = RequestMethod.GET)
    public ResponseEntity<CrawlerRequestModel> status(@PathVariable(name="id") Long requestId) {

        final Optional<CrawlRequest> byId = crawlerRequestRepository.findById(requestId);

        if ( ! byId.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if ( byId.get().getStatus().equals(CrawlerStatus.WORKING) ) {
            return ResponseEntity.ok(modelMapper.map(byId.get(), CrawlerRequestModel.class));
        }

        return ResponseEntity.ok(modelMapper.map(byId.get(), CrawlerRequestModel.class));
    }

}