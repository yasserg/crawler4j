package edu.uci.ics.crawler4j.url;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a singleton which obtains a list of TLDs (from online or a local file) in order to compare against
 * those TLDs
 */
public class TLDList {

  private static final String TLD_NAMES_ONLINE_URL = "https://publicsuffix.org/list/effective_tld_names.dat";
  private static final String TLD_NAMES_TXT_FILENAME = "tld-names.txt";
  private static final Logger logger = LoggerFactory.getLogger(TLDList.class);

  private static boolean onlineUpdate = false;
  private final Set<String> tldSet = new HashSet<>(10000);

  private static final TLDList instance = new TLDList(); // Singleton

  private TLDList() {
    if (onlineUpdate) {
      URL url;
      try {
        url = new URL(TLD_NAMES_ONLINE_URL);
      } catch (MalformedURLException e) {
        // This cannot happen... No need to treat it
        logger.error("Invalid URL: {}", TLD_NAMES_ONLINE_URL);
        throw new RuntimeException(e);
      }

      try (InputStream stream = url.openStream()) {
        logger.debug("Fetching the most updated TLD list online");
        int n = readStream(stream);
        logger.info("Obtained {} TLD from URL {}", n, TLD_NAMES_ONLINE_URL);
        return;
      } catch (Exception e) {
        logger.error("Couldn't fetch the online list of TLDs from: {}", TLD_NAMES_ONLINE_URL, e);
      }
    }

    File f = new File(TLD_NAMES_TXT_FILENAME);
    if (f.exists()) {
      logger.debug("Fetching the list from a local file {}", TLD_NAMES_TXT_FILENAME);
      try (InputStream tldFile = new FileInputStream(f)) {
        int n = readStream(tldFile);
        logger.info("Obtained {} TLD from local file {}", n, TLD_NAMES_TXT_FILENAME);
        return;
      } catch (IOException e) {
        logger.error("Couldn't read the TLD list from local file", e);
      }
    }
    try (InputStream tldFile = getClass().getClassLoader().getResourceAsStream(TLD_NAMES_TXT_FILENAME)) {
      int n = readStream(tldFile);
      logger.info("Obtained {} TLD from packaged file {}", n, TLD_NAMES_TXT_FILENAME);
    } catch (IOException e) {
      logger.error("Couldn't read the TLD list from file");
      throw new RuntimeException(e);
    }
  }

  private int readStream(InputStream stream) {
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

  public static TLDList getInstance() {
    return instance;
  }

  /**
   * If {@code online} is set to true, the list of TLD files will be downloaded and refreshed, otherwise the one
   * cached in src/main/resources/tld-names.txt will be used.
   */
  public static void setUseOnline(boolean online) {
    onlineUpdate = online;
  }

  public boolean contains(String str) {
    return tldSet.contains(str);
  }
}
