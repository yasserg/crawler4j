package edu.uci.ics.crawler4j.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Created by Avi Hayun on 9/22/2014.
 * Net related Utils
 *
 * @author Paul Galbraith <paul.d.galbraith@gmail.com>
 */
public class Net {

    private static final Function<Url, WebURL> urlMapper = url -> {
        WebURL webUrl = new WebURL();
        webUrl.setURL(url.getFullUrl());
        return webUrl;
    };

    private CrawlConfig config;

    public Net(CrawlConfig config) {
        this.config = config;
    }

    public Set<WebURL> extractUrls(String input) {
        if (input == null) {
            return Collections.emptySet();
        } else {
            UrlDetector detector = new UrlDetector(input, getOptions());
            List<Url> urls = detector.detect();
            return urls.stream().map(urlMapper).collect(Collectors.toSet());
        }
    }

    private UrlDetectorOptions getOptions() {
        if (config.isAllowSingleLevelDomain()) {
            return UrlDetectorOptions.ALLOW_SINGLE_LEVEL_DOMAIN;
        } else {
            return UrlDetectorOptions.Default;
        }
    }

}