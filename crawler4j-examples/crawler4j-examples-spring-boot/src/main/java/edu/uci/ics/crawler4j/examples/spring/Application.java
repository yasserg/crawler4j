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

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Paths;

@EnableJpaRepositories
@SpringBootApplication
@EnableAsync
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Scope("prototype") // Because at this point crawler4j does not guarantee CrawlConfig to be thread safe.
    public CrawlConfig crawlerConfiguration() {
        // See CrawlConfig for all configuration options.
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(Paths.get(System.getProperty("java.io.tmpdir"), "crawler", RandomStringUtils.randomAlphabetic(5)).toString());
        config.setIncludeBinaryContentInCrawling(false);
        config.setProcessBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        config.setMaxDownloadSize(20 * 1024 * 1024);

        return config;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean(destroyMethod = "stop")
    public HttpClient httpClient() throws Exception {
        HttpClient client = new HttpClient();
        client.setFollowRedirects(true);
        client.start();
        return client;
    }

}