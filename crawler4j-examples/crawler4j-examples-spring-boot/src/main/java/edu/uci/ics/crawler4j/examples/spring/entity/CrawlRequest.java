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

package edu.uci.ics.crawler4j.examples.spring.entity;

import edu.uci.ics.crawler4j.examples.spring.model.CrawlerStatus;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;

import javax.persistence.*;

@Data
@Entity
@Table(name = "crawl_request")
public class CrawlRequest {
    @Id
    @GeneratedValue
    @Column(name="id")
    private Long id;

    @URL
    private String url;

    @URL
    private String callback;

    private DateTime started = DateTime.now();

    private DateTime finished;

    @Enumerated(EnumType.STRING)
    private CrawlerStatus status;

    public CrawlRequest() {
    }

    public CrawlRequest(Long id) {
        super();
        this.id = id;
    }
}