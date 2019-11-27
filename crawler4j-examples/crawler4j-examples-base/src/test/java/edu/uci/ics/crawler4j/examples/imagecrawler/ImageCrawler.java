package edu.uci.ics.crawler4j.examples.imagecrawler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * This class shows how you can crawl images on the web and store them in a
 * folder. This is just for demonstration purposes and doesn't scale for large
 * number of images. For crawling millions of images you would need to store
 * downloaded images in a hierarchy of folders
 */
public class ImageCrawler extends WebCrawler {

    private static final Pattern filters = Pattern.compile(
        ".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf" +
        "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private static final Pattern imgPatterns = Pattern.compile(".*(\\.(bmp|gif|jpe?g|png|tiff?))$");

    private final File storageFolder;
    private final List<String> crawlDomains;

    public ImageCrawler(File storageFolder, List<String> crawlDomains) {
        this.storageFolder = storageFolder;
        this.crawlDomains = ImmutableList.copyOf(crawlDomains);
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (filters.matcher(href).matches()) {
            return false;
        }

        if (imgPatterns.matcher(href).matches()) {
            return true;
        }

        for (String domain : crawlDomains) {
            if (href.startsWith(domain)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();

        // We are only interested in processing images which are bigger than 10k
        if (!imgPatterns.matcher(url).matches() ||
            !((page.getParseData() instanceof BinaryParseData) ||
              (page.getContentData().length < (10 * 1024)))) {
            return;
        }

        // Get a unique name for storing this image
        String extension = url.substring(url.lastIndexOf('.'));
        String hashedName = UUID.randomUUID() + extension;

        // Store image
        String filename = storageFolder.getAbsolutePath() + '/' + hashedName;
        try {
            Files.write(page.getContentData(), new File(filename));
            WebCrawler.logger.info("Stored: {}", url);
        } catch (IOException iox) {
            WebCrawler.logger.error("Failed to write file: {}", filename, iox);
        }
    }

}