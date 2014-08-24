package edu.uci.ics.crawler4j.url;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a singleton which obtains a list of TLDs (from online or a zip file) in order to compare against those TLDs
 * */
public class TLDList {

  private final static String TLD_NAMES_ONLINE_URL = "https://publicsuffix.org/list/effective_tld_names.dat";
  private final static String TLD_NAMES_ZIP_FILENAME = "tld-names.zip";
  private final static String TLD_NAMES_TXT_FILENAME = "tld-names.txt";
  private final static Logger logger = LoggerFactory.getLogger(TLDList.class);

  private Set<String> tldSet = new HashSet<>(10000);

  private static TLDList instance = new TLDList(); // Singleton

  private TLDList() {
    try {
      InputStream stream = null;

      try {
        logger.debug("Fetching the most updated TLD list online");
        URL url = new URL(TLD_NAMES_ONLINE_URL);
        stream = url.openStream();
      } catch (Exception ex) {
        logger.warn("Couldn't fetch the online list of TLDs from: {}", TLD_NAMES_ONLINE_URL);
        logger.info("Fetching the list from a local file {}", TLD_NAMES_ZIP_FILENAME);

        ZipFile zipFile = new ZipFile(this.getClass().getClassLoader().getResource(TLD_NAMES_ZIP_FILENAME).getFile());
        ZipArchiveEntry entry = zipFile.getEntry(TLD_NAMES_TXT_FILENAME);
        stream = zipFile.getInputStream(entry);
      }

      if (stream == null) {
        throw new Exception("Couldn't fetch the TLD list online or from a local file");
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("//")) {
          continue;
        }
        tldSet.add(line);
      }
      reader.close();
      stream.close();
    } catch (Exception e) {
      logger.error("Couldn't find " + TLD_NAMES_TXT_FILENAME, e);
      System.exit(-1);
    }
  }

  public static TLDList getInstance() {
    return instance;
  }

  public boolean contains(String str) {
    return tldSet.contains(str);
  }
}