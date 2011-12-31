package edu.uci.ics.crawler4j.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HtmlContentHandler extends DefaultHandler {

	private enum Element {
		A, AREA, LINK, IFRAME, FRAME, EMBED, IMG, BASE, META, BODY
    }

	private static class HtmlFactory {
		private static Map<String, Element> name2Element;

		static {
			name2Element = new HashMap<String, Element>();
			for (Element element : Element.values()) {
				name2Element.put(element.toString().toLowerCase(), element);
			}
		}

		public static Element getElement(String name) {
			return name2Element.get(name);
		}
	}

	private String base;
	private String metaRefresh;
	private String metaLocation;

	private boolean isWithinBodyElement;
	private StringBuilder bodyText;

	private Set<String> outgoingUrls;

	public HtmlContentHandler() {
		isWithinBodyElement = false;
		bodyText = new StringBuilder();
		outgoingUrls = new HashSet<String>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Element element = HtmlFactory.getElement(localName);

		if (element == Element.A || element == Element.AREA || element == Element.LINK) {
			String href = attributes.getValue("href");
			if (href != null) {
				outgoingUrls.add(href);
			}
			return;
		}

		if (element == Element.IMG) {
			String imgSrc = attributes.getValue("src");
			if (imgSrc != null) {
				outgoingUrls.add(imgSrc);
			}
			return;
		}

		if (element == Element.IFRAME || element == Element.FRAME || element == Element.EMBED) {
			String src = attributes.getValue("src");
			if (src != null) {
				outgoingUrls.add(src);
			}
			return;
		}

		if (element == Element.BASE) {
			if (base != null) { // We only consider the first occurrence of the
								// Base element.
				String href = attributes.getValue("href");
				if (href != null) {
					base = href;
				}
			}
			return;
		}

		if (element == Element.META) {
			String equiv = attributes.getValue("http-equiv");
			String content = attributes.getValue("content");
			if (equiv != null && content != null) {
				equiv = equiv.toLowerCase();

				// http-equiv="refresh" content="0;URL=http://foo.bar/..."
				if (equiv.equals("refresh") && (metaRefresh == null)) {
					int pos = content.toLowerCase().indexOf("url=");
					if (pos != -1) {
						metaRefresh = content.substring(pos + 4);
					}
				}

				// http-equiv="location" content="http://foo.bar/..."
				if (equiv.equals("location") && (metaLocation == null)) {
					metaLocation = content;
				}
			}
			return;
		}

		if (element == Element.BODY) {
			isWithinBodyElement = true;
        }
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Element element = HtmlFactory.getElement(localName);
		if (element == Element.BODY) {
			isWithinBodyElement = false;
		}
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (isWithinBodyElement) {
			bodyText.append(ch, start, length);
		}
	}

	public String getBodyText() {
		return bodyText.toString();
	}
	
	public Set<String> getOutgoingUrls() {
		return outgoingUrls;
	}
	
	public String getBaseUrl() {
		return base;
	}

}
