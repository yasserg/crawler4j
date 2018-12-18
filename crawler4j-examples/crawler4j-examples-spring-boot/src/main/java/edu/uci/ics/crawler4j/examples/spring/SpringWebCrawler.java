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

public class SpringWebCrawler extends WebCrawler {

    private final CrawlerRequestModel request;
    private final CrawlerRequestRepository repo;
    private final ModelMapper modelMapper;

    public SpringWebCrawler(CrawlerRequestModel request, CrawlerRequestRepository repo, ModelMapper modelMapper) {
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
