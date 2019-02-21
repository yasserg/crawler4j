package edu.uci.ics.crawler4j.examples.crawler;

import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.examples.db.PostgresDBService;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class PostgresWebCrawler extends WebCrawler {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PostgresWebCrawler.class);

    private static final Pattern FILE_ENDING_EXCLUSION_PATTERN = Pattern.compile(".*(\\.(" +
            "css|js" +
            "|bmp|gif|jpe?g|JPE?G|png|tiff?|ico|nef|raw" +
            "|mid|mp2|mp3|mp4|wav|wma|flv|mpe?g" +
            "|avi|mov|mpeg|ram|m4v|wmv|rm|smil" +
            "|pdf|doc|docx|pub|xls|xlsx|vsd|ppt|pptx" +
            "|swf" +
            "|zip|rar|gz|bz2|7z|bin" +
            "|xml|txt|java|c|cpp|exe" +
            "))$");

    private final PostgresDBService postgresDBService;

    public PostgresWebCrawler(PostgresDBService postgresDBService) {
        this.postgresDBService = postgresDBService;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILE_ENDING_EXCLUSION_PATTERN.matcher(href).matches();
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        logger.info("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            logger.info("Text length: " + text.length());
            logger.info("Html length: " + html.length());
            logger.info("Number of outgoing links: " + links.size());

            try {
                postgresDBService.store(page);
            } catch (RuntimeException e) {
                logger.error("Storing failed", e);
            }
        }
    }

    public void onBeforeExit() {
        if (postgresDBService != null) {
            postgresDBService.close();
        }
    }
}