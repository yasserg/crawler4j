package edu.uci.ics.crawler4j.robotstxt;

public class RobotstxtConfig {

	/**
	 * Should the crawler obey Robots.txt protocol? More info on Robots.txt is
	 * available at http://www.robotstxt.org/
	 */
	private boolean enabled = true;

	/**
	 * user-agent name that will be used to determine whether some servers have
	 * specific rules for this agent name.
	 */
	private String userAgentName = "crawler4j";

	/**
	 * The maximum number of hosts for which their robots.txt is cached.
	 */
	private int cacheSize = 500;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getUserAgentName() {
		return userAgentName;
	}

	public void setUserAgentName(String userAgentName) {
		this.userAgentName = userAgentName;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

}
