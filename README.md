# crawler4j
[![Build Status](https://travis-ci.org/yasserg/crawler4j.svg?branch=master)](https://travis-ci.org/yasserg/crawler4j)
![Maven Central](https://img.shields.io/maven-central/v/edu.uci.ics/crawler4j.svg?style=flat-square)

crawler4j is an open source web crawler for Java which provides a simple interface for
crawling the Web. Using it, you can setup a multi-threaded web crawler in few minutes.

## Installation

### Using Maven

To use the latest release of crawler4j, please use the following snippet in your pom.xml

```xml
    <dependency>
        <groupId>edu.uci.ics</groupId>
        <artifactId>crawler4j</artifactId>
        <version>4.1</version>
    </dependency>
```

### Without Maven

crawler4j JARs are available on the [releases page](https://github.com/yasserg/crawler4j/releases)
and at [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22edu.uci.ics%22%20AND%20a%3A%22crawler4j%22).

If you use crawler4j without Maven, be aware that crawler4j jar file has a couple of
external dependencies. In [releases page](https://github.com/yasserg/crawler4j/releases), you can find a file named crawler4j-X.Y-with-dependencies.jar that includes crawler4j and all of its dependencies as a bundle.
You can add download it and add it to your classpath to get all the dependencies covered.

## Quickstart
You need to create a crawler class that extends WebCrawler. This class decides which URLs
should be crawled and handles the downloaded page. The following is a sample
implementation:
```java
public class MyCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
                                                           + "|png|mp3|mp3|zip|gz))$");

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
     @Override
     public boolean shouldVisit(Page referringPage, WebURL url) {
         String href = url.getURL().toLowerCase();
         return !FILTERS.matcher(href).matches()
                && href.startsWith("http://www.ics.uci.edu/");
     }

     /**
      * This function is called when a page is fetched and ready
      * to be processed by your program.
      */
     @Override
     public void visit(Page page) {
         String url = page.getWebURL().getURL();
         System.out.println("URL: " + url);

         if (page.getParseData() instanceof HtmlParseData) {
             HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
             String text = htmlParseData.getText();
             String html = htmlParseData.getHtml();
             Set<WebURL> links = htmlParseData.getOutgoingUrls();

             System.out.println("Text length: " + text.length());
             System.out.println("Html length: " + html.length());
             System.out.println("Number of outgoing links: " + links.size());
         }
    }
}
```
As can be seen in the above code, there are two main functions that should be overridden:

- shouldVisit: This function decides whether the given URL should be crawled or not. In
the above example, this example is not allowing .css, .js and media files and only allows
 pages within 'www.ics.uci.edu' domain.
- visit: This function is called after the content of a URL is downloaded successfully.
 You can easily get the url, text, links, html, and unique id of the downloaded page.

You should also implement a controller class which specifies the seeds of the crawl,
the folder in which intermediate crawl data should be stored and the number of concurrent
 threads:

```java
public class Controller {
    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "/data/crawl/root";
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("http://www.ics.uci.edu/~lopes/");
        controller.addSeed("http://www.ics.uci.edu/~welling/");
    	controller.addSeed("http://www.ics.uci.edu/");

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(MyCrawler.class, numberOfCrawlers);
    }
}
```

## More Examples
- [Basic crawler](https://github.com/yasserg/crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/basic): the full source code of the above example with more details.
- [Image crawler](https://github.com/yasserg/crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/imagecrawler): a simple image crawler that downloads image content from the crawling domain and stores them in a folder. This example demonstrates how binary content can be fetched using crawler4j.
- [Collecting data from threads](https://github.com/yasserg/crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/localdata): this example demonstrates how the controller can collect data/statistics from crawling threads.
- [Multiple crawlers](https://github.com/yasserg/crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/multiple): this is a sample that shows how two distinct crawlers can run concurrently. For example, you might want to split your crawling into different domains and then take different crawling policies for each group. Each crawling controller can have its own configurations.
- [Shutdown crawling](https://github.com/yasserg/crawler4j/tree/master/src/test/java/edu/uci/ics/crawler4j/examples/shutdown): this example shows have crawling can be terminated gracefully by sending the 'shutdown' command to the controller.

## Configuration Details
The controller class has a mandatory parameter of type [CrawlConfig](https://github.com/yasserg/crawler4j/blob/master/src/main/java/edu/uci/ics/crawler4j/crawler/CrawlConfig.java).
 Instances of this class can be used for configuring crawler4j. The following sections
describe some details of configurations.

### Crawl depth
By default there is no limit on the depth of crawling. But you can limit the depth of crawling. For example, assume that you have a seed page "A", which links to "B", which links to "C", which links to "D". So, we have the following link structure:

A -> B -> C -> D

Since, "A" is a seed page, it will have a depth of 0. "B" will have depth of 1 and so on. You can set a limit on the depth of pages that crawler4j crawls. For example, if you set this limit to 2, it won't crawl page "D". To set the maximum depth you can use:
```java
crawlConfig.setMaxDepthOfCrawling(maxDepthOfCrawling);
```

### Maximum number of pages to crawl
Although by default there is no limit on the number of pages to crawl, you can set a limit
on this:

```java
crawlConfig.setMaxPagesToFetch(maxPagesToFetch);
```

### Politeness
crawler4j is designed very efficiently and has the ability to crawl domains very fast
(e.g., it has been able to crawl 200 Wikipedia pages per second). However, since this
is against crawling policies and puts huge load on servers (and they might block you!),
since version 1.3, by default crawler4j waits at least 200 milliseconds between requests.
However, this parameter can be tuned:

```java
crawlConfig.setPolitenessDelay(politenessDelay);
```

### Proxy
Should your crawl run behind a proxy? If so, you can use:

```java
crawlConfig.setProxyHost("proxyserver.example.com");
crawlConfig.setProxyPort(8080);
```
If your proxy also needs authentication:
```java
crawlConfig.setProxyUsername(username);
crawlConfig.getProxyPassword(password);
```

### Resumable Crawling
Sometimes you need to run a crawler for a long time. It is possible that the crawler
terminates unexpectedly. In such cases, it might be desirable to resume the crawling.
You would be able to resume a previously stopped/crashed crawl using the following
settings:
```java
crawlConfig.setResumableCrawling(true);
```
However, you should note that it might make the crawling slightly slower.

### User agent string
User-agent string is used for representing your crawler to web servers. See [here](http://en.wikipedia.org/wiki/User_agent)
for more details. By default crawler4j uses the following user agent string:

```
"crawler4j (https://github.com/yasserg/crawler4j/)"
```
However, you can overwrite it:
```java
crawlConfig.setUserAgentString(userAgentString);
```

## License

Copyright (c) 2010-2015 Yasser Ganjisaffar

Published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0), see LICENSE
