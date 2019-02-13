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

package edu.uci.ics.crawler4j.examples.spring.service;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.examples.spring.SpringWebCrawler;
import edu.uci.ics.crawler4j.examples.spring.model.CrawlerRequestModel;
import edu.uci.ics.crawler4j.examples.spring.repo.CrawlerRequestRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
        WebCrawler webCrawler = new SpringWebCrawler(request, repo, modelMapper);

        return webCrawler;
    }

}
