package edu.uci.ics.crawler4j.url;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

/**
 * This class is a singleton which obtains a list of TLDs (from online or a local file) in order to
 * compare against those TLDs
 */
public class TLDList {

    private final Logger logger = LoggerFactory.getLogger(TLDList.class);

    private static final String TLD_NAMES_TXT_FILENAME = "tld-names.txt";

    private boolean onlineUpdate;
    private String url;

    private final Supplier<Set<String>> memoizer;

    public TLDList(CrawlConfig config) {
        this.onlineUpdate = config.isOnlineTldListUpdate();
        this.url = config.getPublicSuffixSourceUrl();
        memoizer = memoize(this::tldSupplier)::get;
    }

    private int readStream(InputStream stream, Set<String> tldSet) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }
                tldSet.add(line);
            }
        } catch (IOException e) {
            logger.warn("Error while reading TLD-list: {}", e.getMessage());
        }
        return tldSet.size();
    }

    public boolean contains(String str) {
        return memoizer.get().contains(str);
    }

    private Set<String> tldSupplier() {
        final Set<String> tldSet = new HashSet<>(10000);

        if (onlineUpdate) {
            try (InputStream in = new URL(url).openStream()) {
                logger.debug("Fetching the most updated TLD list online");
                int n = readStream(in, tldSet);
                logger.info("Obtained {} TLD from URL {}", n, url);
            } catch (Exception e) {
                logger.error("Couldn't fetch the online list of TLDs from: {}",
                    url, e);
                logger.error("Will try to load from file(s).");
                loadFromFiles(tldSet);
            }
        } else {
            loadFromFiles(tldSet);
        }

        return tldSet;
    }

    private void loadFromFiles(Set<String> tldSet) {
        try (InputStream tldFile = FileUtils.openInputStream(new File(TLD_NAMES_TXT_FILENAME))) {
            logger.debug("Fetching the list from a local file {}", TLD_NAMES_TXT_FILENAME);
            int n = readStream(tldFile, tldSet);
            logger.info("Obtained {} TLD from local file {}", n, TLD_NAMES_TXT_FILENAME);
        } catch (FileNotFoundException e) {
            logger.info("File not found: {}", TLD_NAMES_TXT_FILENAME);
        } catch (IOException e) {
            logger.error("Couldn't read the TLD list from file {}", TLD_NAMES_TXT_FILENAME);
        }

        try (InputStream tldFile = TLDList.class.getClassLoader()
                .getResourceAsStream(TLD_NAMES_TXT_FILENAME)) {
            int n = 0;
            if (tldFile != null) {
                n = readStream(tldFile, tldSet);
            }
            logger.info("Obtained {} TLD from packaged file {}", n, TLD_NAMES_TXT_FILENAME);
        } catch (IOException e) {
            logger.error("Couldn't read the TLD list from file");
            throw new RuntimeException(e);
        }
    }

    // Naive but no need to account for threading in this case.
    private <T> Supplier<T> memoize(Supplier<T> supplier) {
        Map<Object, T> mem = new HashMap<>();
        return () -> mem.computeIfAbsent("memoizeMe", key -> supplier.get());
    }
}
