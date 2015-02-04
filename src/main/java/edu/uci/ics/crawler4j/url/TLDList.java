package edu.uci.ics.crawler4j.url;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
  private static final String TLD_NAMES_TXT_FILENAME = "/tld-names.txt";
  private static final Logger logger = LoggerFactory.getLogger(TLDList.class);

  private final Set<String> tldSet = new HashSet<>(10000);

  private static final TLDList instance = new TLDList(); // Singleton

  private TLDList() {
    try {
      URL url = new URL(TLD_NAMES_ONLINE_URL);
      try (InputStream stream = url.openStream(); BufferedReader reader = new BufferedReader(
          new InputStreamReader(stream))) {
        logger.debug("Fetching the most updated TLD list online");

        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("//")) {
            continue;
          }
          tldSet.add(line);
        }
      } catch (Exception ex) {
        throw new Exception("Error while retrieving online TLD List");
      }
    } catch (Exception ex) { // Reverting to offline TLD List
      logger.warn("Couldn't fetch the online list of TLDs from: {}", TLD_NAMES_ONLINE_URL);
      logger.info("Fetching the list from my local file {}", TLD_NAMES_TXT_FILENAME);

      try (InputStream stream = this.getClass().getResourceAsStream(TLD_NAMES_TXT_FILENAME);
           BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("//")) {
            continue;
          }
          tldSet.add(line);
        }
      } catch (Exception ex2) {
        logger.error("Couldn't find " + TLD_NAMES_TXT_FILENAME, ex2);
        logger.error("No TLD List exiting...");
        System.exit(-1);
      }
    }
  }

  public static TLDList getInstance() {
    return instance;
  }

  public boolean contains(String str) {
    return tldSet.contains(str);
  }
}