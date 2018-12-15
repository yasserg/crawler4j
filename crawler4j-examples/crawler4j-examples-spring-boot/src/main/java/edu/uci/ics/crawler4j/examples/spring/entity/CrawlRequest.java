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