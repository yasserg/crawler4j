/*
 * Copyright 2018 Federico Tolomei <mail@s17t.net>
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

package edu.uci.ics.crawler4j.examples.spring.controller;

import edu.uci.ics.crawler4j.examples.spring.entity.CrawlRequest;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerRequestModel;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerStatus;
import edu.uci.ics.crawler4j.examples.spring.repo.CrawlerRequestRepository;
import edu.uci.ics.crawler4j.examples.spring.service.CrawlerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@Api(tags = { "crawl" })
@RequestMapping("/api/v1")
@RestController
public class CrawlerController {

    @Autowired
    CrawlerService service;

    @Autowired
    CrawlerRequestRepository crawlerRequestRepository;

    @Autowired
    ModelMapper modelMapper;

    @ApiOperation(value = "Submit a crawl request.", response = CrawlerRequestModel.class, nickname = "submitCrawlRequest")
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Crawl request has been accepted."),
        @ApiResponse(code = 500, message = "Crawl request has not been accepted.")
    })
    @RequestMapping(path = "/crawl", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
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

    @ApiOperation(value = "Status of a crawl request.",
        response = CrawlerRequestModel.class,
        nickname = "statusCrawlRequest")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Crawl request exists. See the 'status' field of the model response " +
            "to get the status of the crawling process."),
        @ApiResponse(code = 404, message = "Crawl request does not exist."),
    })
    @RequestMapping(path = "/crawl/{id}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
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