package edu.uci.ics.crawler4j.examples.spring.repo;

import edu.uci.ics.crawler4j.examples.spring.entity.CrawlRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface CrawlerRequestRepository extends JpaRepository<CrawlRequest, Long> {
}