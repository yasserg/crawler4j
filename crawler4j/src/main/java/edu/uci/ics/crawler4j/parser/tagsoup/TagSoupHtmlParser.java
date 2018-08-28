//***************************************************
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.crawler4j.parser.tagsoup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.tika.config.Field;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractEncodingDetectorParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.DefaultHtmlMapper;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.parser.html.*;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * HTML parser. Uses TagSoup to turn the input document to HTML SAX events,
 * and post-processes the events to produce XHTML and metadata expected by
 * Tika clients.
 */
public class TagSoupHtmlParser extends AbstractEncodingDetectorParser {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 7895315240498733128L;

    private static final Logger LOG = LoggerFactory.getLogger(TagSoupHtmlParser.class);

    private static final MediaType XHTML = MediaType.application("xhtml+xml");
    private static final MediaType WAP_XHTML = MediaType.application("vnd.wap.xhtml+xml");
    private static final MediaType X_ASP = MediaType.application("x-asp");

    private static final Set<MediaType> SUPPORTED_TYPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    MediaType.text("html"),
                    XHTML,
                    WAP_XHTML,
                    X_ASP)));

    /**
     * HTML schema singleton used to amortise the heavy instantiation time.
     */
    private static final Schema HTML_SCHEMA = new HTMLSchema();

    @Field
    private boolean extractScripts = false;

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }

    public TagSoupHtmlParser() {
        super();
    }

    public TagSoupHtmlParser(EncodingDetector encodingDetector) {
        super(encodingDetector);
    }

    public void parse(
            InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {

        // Automatically detect the character encoding
        try (AutoDetectReader reader = new AutoDetectReader(new CloseShieldInputStream(stream),
                metadata, getEncodingDetector(context))) {
            Charset charset = reader.getCharset();
            String previous = metadata.get(Metadata.CONTENT_TYPE);
            MediaType contentType = null;
            if (previous == null || previous.startsWith("text/html")) {
                contentType = new MediaType(MediaType.TEXT_HTML, charset);
            } else if (previous.startsWith("application/xhtml+xml")) {
                contentType = new MediaType(XHTML, charset);
            } else if (previous.startsWith("application/vnd.wap.xhtml+xml")) {
                contentType = new MediaType(WAP_XHTML, charset);
            } else if (previous.startsWith("application/x-asp")) {
                contentType = new MediaType(X_ASP, charset);
            }
            if (contentType != null) {
                metadata.set(Metadata.CONTENT_TYPE, contentType.toString());
            }
            // deprecated, see TIKA-431
            metadata.set(Metadata.CONTENT_ENCODING, charset.name());

            // Get the HTML mapper from the parse context
            HtmlMapper mapper =
                    context.get(HtmlMapper.class, new HtmlParserMapper());

            // Parse the HTML document
            edu.uci.ics.crawler4j.parser.tagsoup.TagSoupParser parser =
                    new edu.uci.ics.crawler4j.parser.tagsoup.TagSoupParser();

            // Use schema from context or default
            Schema schema = context.get(Schema.class, HTML_SCHEMA);

            // TIKA-528: Reuse share schema to avoid heavy instantiation
            parser.setProperty(
            		edu.uci.ics.crawler4j.parser.tagsoup.TagSoupParser.schemaProperty, schema);
            // TIKA-599: Shared schema is thread-safe only if bogons are ignored
            parser.setFeature(
            		edu.uci.ics.crawler4j.parser.tagsoup.TagSoupParser.ignoreBogonsFeature, true);

            parser.setContentHandler(new XHTMLDowngradeHandler(
                    new HtmlHandler(mapper, handler, metadata, context, extractScripts)));

            parser.parse(reader.asInputSource());
        }
    }

    /**
     * Maps "safe" HTML element names to semantic XHTML equivalents. If the
     * given element is unknown or deemed unsafe for inclusion in the parse
     * output, then this method returns <code>null</code> and the element
     * will be ignored but the content inside it is still processed. See
     * the {@link #isDiscardElement(String)} method for a way to discard
     * the entire contents of an element.
     * <p/>
     * Subclasses can override this method to customize the default mapping.
     *
     * @param name HTML element name (upper case)
     * @return XHTML element name (lower case), or
     * <code>null</code> if the element is unsafe
     * @since Apache Tika 0.5
     * @deprecated Use the {@link HtmlMapper} mechanism to customize
     * the HTML mapping. This method will be removed in Tika 1.0.
     */
    protected String mapSafeElement(String name) {
        return DefaultHtmlMapper.INSTANCE.mapSafeElement(name);
    }

    /**
     * Checks whether all content within the given HTML element should be
     * discarded instead of including it in the parse output. Subclasses
     * can override this method to customize the set of discarded elements.
     *
     * @param name HTML element name (upper case)
     * @return <code>true</code> if content inside the named element
     * should be ignored, <code>false</code> otherwise
     * @since Apache Tika 0.5
     * @deprecated Use the {@link HtmlMapper} mechanism to customize
     * the HTML mapping. This method will be removed in Tika 1.0.
     */
    protected boolean isDiscardElement(String name) {
        return DefaultHtmlMapper.INSTANCE.isDiscardElement(name);
    }

    /**
     * @deprecated Use the {@link HtmlMapper} mechanism to customize
     * the HTML mapping. This method will be removed in Tika 1.0.
     */
    public String mapSafeAttribute(String elementName, String attributeName) {
        return DefaultHtmlMapper.INSTANCE.mapSafeAttribute(elementName, attributeName);
    }

    /**
     * Adapter class that maintains backwards compatibility with the
     * protected HtmlParser methods. Making HtmlParser implement HtmlMapper
     * directly would require those methods to be public, which would break
     * backwards compatibility with subclasses.
     *
     * @deprecated Use the {@link HtmlMapper} mechanism to customize
     * the HTML mapping. This class will be removed in Tika 1.0.
     */
    private class HtmlParserMapper implements HtmlMapper {
        public String mapSafeElement(String name) {
            return TagSoupHtmlParser.this.mapSafeElement(name);
        }

        public boolean isDiscardElement(String name) {
            return TagSoupHtmlParser.this.isDiscardElement(name);
        }

        public String mapSafeAttribute(String elementName, String attributeName) {
            return TagSoupHtmlParser.this.mapSafeAttribute(elementName, attributeName);
        }
    }

    /**
     * Whether or not to extract contents in script entities.
     * Default is <code>false</code>
     *
     * @param extractScripts
     */
    @Field
    public void setExtractScripts(boolean extractScripts) {
        this.extractScripts = extractScripts;
    }

    public boolean getExtractScripts() {
        return extractScripts;
    }

}

